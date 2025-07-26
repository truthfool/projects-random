from typing import List, Dict, Optional
from pathlib import Path
import chromadb
from transformers import pipeline
from sentence_transformers import SentenceTransformer
from loguru import logger

from ..config import settings
from ..models.log_entry import LogEntry, LogAnalysis

class AIAnalyzer:
    """Service for AI-powered log analysis using transformers and vector similarity."""
    
    def __init__(self):
        # Initialize embedding model
        self.embedding_model = SentenceTransformer(settings.EMBEDDING_MODEL)
        
        # Initialize vector database
        self.vector_db = chromadb.PersistentClient(path=str(settings.VECTOR_DB_PATH))
        self.context_collection = self.vector_db.get_or_create_collection(
            name="system_context",
            metadata={"description": "System context and documentation"}
        )
        
        # Initialize zero-shot classifier for categorization
        self.classifier = pipeline(
            "zero-shot-classification",
            model="facebook/bart-large-mnli",
            device=-1  # CPU
        )
        
        # Load system context
        self._load_system_context()
    
    def analyze_logs(self, entries: List[LogEntry], context_query: Optional[str] = None) -> LogAnalysis:
        """Perform AI analysis on log entries."""
        logger.info(f"Starting AI analysis of {len(entries)} log entries")
        
        # Initialize analysis
        analysis = LogAnalysis(
            analyzed_files=list(set(entry.source_file for entry in entries)),
            total_entries=len(entries)
        )
        
        try:
            # Get relevant system context
            context = self._get_relevant_context(entries, context_query)
            analysis.system_context = context
            
            # Analyze patterns and anomalies
            self._analyze_patterns(entries, analysis)
            
            # Identify root causes
            self._identify_root_causes(entries, analysis, context)
            
            # Generate recommendations
            self._generate_recommendations(analysis)
            
            logger.info("AI analysis completed successfully")
            return analysis
            
        except Exception as e:
            logger.error(f"Error during AI analysis: {e}")
            raise
    
    def add_system_context(self, content: str, metadata: Dict) -> None:
        """Add new system context to the vector database."""
        try:
            # Generate embedding
            embedding = self.embedding_model.encode(content)
            
            # Add to vector database
            self.context_collection.add(
                documents=[content],
                metadatas=[metadata],
                embeddings=[embedding.tolist()]
            )
            
            logger.info(f"Added new context: {metadata.get('title', 'Untitled')}")
            
        except Exception as e:
            logger.error(f"Error adding context: {e}")
            raise
    
    def _load_system_context(self) -> None:
        """Load system context from files."""
        context_dir = settings.CONTEXT_DIR
        if not context_dir.exists():
            return
            
        for file in context_dir.glob("*.txt"):
            try:
                content = file.read_text()
                metadata = {
                    "title": file.stem,
                    "type": "documentation",
                    "source": str(file)
                }
                self.add_system_context(content, metadata)
            except Exception as e:
                logger.warning(f"Failed to load context from {file}: {e}")
    
    def _get_relevant_context(self, entries: List[LogEntry], context_query: Optional[str] = None) -> Dict:
        """Get relevant system context for the log entries."""
        # Prepare query from entries and explicit query
        query_parts = []
        if context_query:
            query_parts.append(context_query)
        
        # Add important log messages
        query_parts.extend(
            entry.message for entry in entries 
            if entry.level in ["ERROR", "CRITICAL"] 
            or entry.severity_score and entry.severity_score > 0.7
        )
        
        if not query_parts:
            return {}
        
        # Get combined query embedding
        query_embedding = self.embedding_model.encode(" ".join(query_parts))
        
        # Query vector database
        results = self.context_collection.query(
            query_embeddings=[query_embedding.tolist()],
            n_results=settings.MAX_CONTEXT_DOCS
        )
        
        # Process results
        context = {}
        for doc, metadata in zip(results.get("documents", []), results.get("metadatas", [])):
            if doc and metadata:
                context[metadata[0].get("title", "unknown")] = {
                    "content": doc[0],
                    "metadata": metadata[0]
                }
        
        return context
    
    def _analyze_patterns(self, entries: List[LogEntry], analysis: LogAnalysis) -> None:
        """Analyze patterns and anomalies in log entries."""
        # Count by level
        for entry in entries:
            if entry.level == "ERROR":
                analysis.error_count += 1
            elif entry.level == "WARNING":
                analysis.warning_count += 1
        
        # Identify patterns
        pattern_counts = {}
        for entry in entries:
            for pattern in entry.matched_patterns:
                if pattern not in pattern_counts:
                    pattern_counts[pattern] = {
                        "count": 0,
                        "entries": [],
                        "severity_scores": []
                    }
                pattern_counts[pattern]["count"] += 1
                pattern_counts[pattern]["entries"].append(entry)
                if entry.severity_score:
                    pattern_counts[pattern]["severity_scores"].append(entry.severity_score)
        
        # Add patterns to analysis
        for pattern, data in pattern_counts.items():
            avg_severity = (
                sum(data["severity_scores"]) / len(data["severity_scores"])
                if data["severity_scores"]
                else 0.5
            )
            
            analysis.patterns.append({
                "pattern": pattern,
                "occurrences": data["count"],
                "average_severity": avg_severity,
                "example_entries": [
                    {"message": e.message, "timestamp": e.timestamp}
                    for e in data["entries"][:3]  # First 3 examples
                ]
            })
            
            # Add to critical patterns if severe enough
            if avg_severity > 0.7 or data["count"] > len(entries) * 0.1:  # 10% threshold
                analysis.critical_patterns.append(pattern)
    
    def _identify_root_causes(self, entries: List[LogEntry], analysis: LogAnalysis, context: Dict) -> None:
        """Identify root causes using AI analysis."""
        # Prepare input for analysis
        critical_entries = [
            entry for entry in entries
            if entry.level in ["ERROR", "CRITICAL"] 
            or entry.severity_score and entry.severity_score > 0.7
        ]
        
        if not critical_entries:
            return
        
        # Group related entries
        entry_groups = self._group_related_entries(critical_entries)
        
        # Analyze each group
        for group in entry_groups:
            # Prepare analysis prompt
            prompt = settings.AI_ANALYSIS_PROMPTS["error_analysis"].format(
                log_entries="\n".join(f"[{e.timestamp}] {e.message}" for e in group),
                context="\n".join(f"{k}: {v['content']}" for k, v in context.items())
            )
            
            # Use zero-shot classification for root cause
            cause_labels = [
                "configuration_error",
                "resource_exhaustion",
                "network_issue",
                "software_bug",
                "external_dependency",
                "security_issue"
            ]
            
            result = self.classifier(
                sequences=prompt,
                candidate_labels=cause_labels,
                hypothesis_template="This issue is caused by a {}."
            )
            
            # Add root cause to analysis
            analysis.root_causes.append({
                "issue": group[0].message,  # Use first entry as representative
                "cause": result["labels"][0],  # Most likely cause
                "confidence": result["scores"][0],
                "related_entries": len(group),
                "timespan": {
                    "start": min(e.timestamp for e in group),
                    "end": max(e.timestamp for e in group)
                }
            })
    
    def _generate_recommendations(self, analysis: LogAnalysis) -> None:
        """Generate recommendations based on analysis."""
        for root_cause in analysis.root_causes:
            cause = root_cause["cause"]
            confidence = root_cause["confidence"]
            
            if cause == "configuration_error" and confidence > 0.7:
                analysis.recommendations.append({
                    "action": "Review and update configuration",
                    "priority": "high" if confidence > 0.9 else "medium",
                    "impact": "medium",
                    "implementation": "Review configuration files and update parameters"
                })
            elif cause == "resource_exhaustion" and confidence > 0.7:
                analysis.recommendations.append({
                    "action": "Scale resources",
                    "priority": "high",
                    "impact": "high",
                    "implementation": "Increase system resources or optimize resource usage"
                })
            elif cause == "network_issue" and confidence > 0.7:
                analysis.recommendations.append({
                    "action": "Investigate network configuration",
                    "priority": "high" if confidence > 0.9 else "medium",
                    "impact": "high",
                    "implementation": "Review network settings and connectivity"
                })
    
    def _group_related_entries(self, entries: List[LogEntry]) -> List[List[LogEntry]]:
        """Group related log entries together."""
        groups = []
        current_group = []
        
        for entry in sorted(entries, key=lambda x: x.timestamp):
            if not current_group:
                current_group.append(entry)
                continue
            
            # Check if entry is related to current group
            is_related = (
                any(pattern in entry.matched_patterns for pattern in current_group[0].matched_patterns)
                or (entry.timestamp - current_group[-1].timestamp).total_seconds() < 300  # 5 min window
            )
            
            if is_related:
                current_group.append(entry)
            else:
                groups.append(current_group)
                current_group = [entry]
        
        if current_group:
            groups.append(current_group)
        
        return groups 