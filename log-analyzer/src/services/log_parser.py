import re
from datetime import datetime
from pathlib import Path
from typing import Dict, List, Generator, Optional
from loguru import logger

from ..config import settings
from ..models.log_entry import LogEntry

class LogParser:
    """Service for parsing log files and extracting structured data."""
    
    def __init__(self):
        self.patterns = settings.LOG_PATTERNS
        self.compiled_patterns = {
            name: re.compile(pattern) for name, pattern in self.patterns.items()
        }
        
        # Compile error patterns
        self.error_patterns = {
            name: re.compile(data["pattern"]) 
            for name, data in settings.ERROR_PATTERNS.items()
        }
    
    def parse_file(self, file_path: Path) -> Generator[LogEntry, None, None]:
        """Parse a log file and yield LogEntry objects."""
        logger.info(f"Parsing log file: {file_path}")
        
        try:
            with open(file_path, 'r') as f:
                for line_number, line in enumerate(f, 1):
                    try:
                        entry = self._parse_line(line.strip(), file_path, line_number)
                        if entry:
                            yield entry
                    except Exception as e:
                        logger.warning(f"Failed to parse line {line_number} in {file_path}: {e}")
                        continue
        except Exception as e:
            logger.error(f"Error reading file {file_path}: {e}")
            raise
    
    def _parse_line(self, line: str, file_path: Path, line_number: int) -> Optional[LogEntry]:
        """Parse a single log line into a LogEntry object."""
        if not line:
            return None
            
        try:
            # Extract components using regex patterns
            timestamp_match = re.search(self.patterns["timestamp"], line)
            level_match = re.search(self.patterns["level"], line)
            service_match = re.search(self.patterns["service"], line)
            
            if not timestamp_match or not level_match:
                return None
            
            # Extract message (everything after the metadata)
            metadata_end = max(m.end() for m in [timestamp_match, level_match, service_match] if m)
            message = line[metadata_end:].strip()
            
            # Create log entry
            entry = LogEntry(
                timestamp=datetime.strptime(timestamp_match.group(), "%Y-%m-%d %H:%M:%S.%f"),
                level=level_match.group(),
                service=service_match.group().strip("[]()") if service_match else None,
                message=message,
                source_file=str(file_path),
                line_number=line_number
            )
            
            # Enrich entry with pattern matching
            self._enrich_entry(entry)
            
            return entry
            
        except Exception as e:
            logger.debug(f"Failed to parse line: {line}\nError: {e}")
            return None
    
    def _enrich_entry(self, entry: LogEntry) -> None:
        """Enrich log entry with pattern matching and metadata."""
        # Match known error patterns
        for pattern_name, pattern in self.error_patterns.items():
            if pattern.search(entry.message):
                entry.matched_patterns.append(pattern_name)
                
                # Add error metadata
                error_data = settings.ERROR_PATTERNS[pattern_name]
                entry.category = error_data["category"]
                entry.severity_score = self._calculate_severity(error_data["severity"])
                
                # Extract additional context
                entry.context.update({
                    "error_type": pattern_name,
                    "description": error_data["description"],
                    "common_causes": error_data["common_causes"]
                })
        
        # Extract structured data from message
        self._extract_data(entry)
    
    def _extract_data(self, entry: LogEntry) -> None:
        """Extract structured data from log message."""
        # Extract key-value pairs
        kv_pattern = re.compile(r'(\w+)=([^,\s]+)')
        entry.extracted_data.update(dict(kv_pattern.findall(entry.message)))
        
        # Extract quoted strings
        quoted_pattern = re.compile(r'"([^"]*)"')
        quoted_strings = quoted_pattern.findall(entry.message)
        if quoted_strings:
            entry.extracted_data["quoted_strings"] = quoted_strings
        
        # Extract IP addresses
        ip_pattern = re.compile(r'\b(?:\d{1,3}\.){3}\d{1,3}\b')
        ip_addresses = ip_pattern.findall(entry.message)
        if ip_addresses:
            entry.extracted_data["ip_addresses"] = ip_addresses
    
    def _calculate_severity(self, severity: str) -> float:
        """Calculate numerical severity score."""
        severity_scores = {
            "low": 0.3,
            "medium": 0.5,
            "high": 0.8,
            "critical": 1.0
        }
        return severity_scores.get(severity.lower(), 0.5)
    
    @staticmethod
    def get_supported_formats() -> List[Dict[str, str]]:
        """Return list of supported log formats with examples."""
        return [
            {
                "name": "Standard Format",
                "pattern": "YYYY-MM-DD HH:MM:SS.mmm LEVEL [SERVICE] Message",
                "example": "2023-11-07 10:30:45.123 ERROR [auth-service] Failed to connect"
            },
            {
                "name": "Simple Format",
                "pattern": "YYYY-MM-DD HH:MM:SS LEVEL Message",
                "example": "2023-11-07 10:30:45 ERROR Database connection failed"
            },
            {
                "name": "Extended Format",
                "pattern": "YYYY-MM-DD HH:MM:SS.mmm LEVEL [SERVICE] (COMPONENT) Message",
                "example": "2023-11-07 10:30:45.123 ERROR [web-server] (request-handler) Invalid request"
            }
        ] 