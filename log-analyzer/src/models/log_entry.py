from datetime import datetime
from typing import Dict, List, Optional
from pydantic import BaseModel, Field

class LogEntry(BaseModel):
    """Represents a single log entry with parsed components."""
    
    # Core fields
    timestamp: datetime = Field(..., description="Timestamp of the log entry")
    level: str = Field(..., description="Log level (DEBUG, INFO, WARNING, ERROR, CRITICAL)")
    service: Optional[str] = Field(None, description="Service or component that generated the log")
    message: str = Field(..., description="Main log message")
    
    # Additional metadata
    source_file: str = Field(..., description="Source file path of the log")
    line_number: int = Field(..., description="Line number in the source file")
    context: Dict[str, str] = Field(default_factory=dict, description="Additional contextual information")
    
    # Analysis fields
    severity_score: Optional[float] = Field(None, description="Calculated severity score (0-1)")
    category: Optional[str] = Field(None, description="Categorization of the log entry")
    related_entries: List[int] = Field(default_factory=list, description="Line numbers of related log entries")
    
    # Pattern matching
    matched_patterns: List[str] = Field(default_factory=list, description="List of matched error patterns")
    extracted_data: Dict[str, str] = Field(default_factory=dict, description="Data extracted from the message")
    
    class Config:
        json_schema_extra = {
            "example": {
                "timestamp": "2023-11-07T10:30:45.123",
                "level": "ERROR",
                "service": "auth-service",
                "message": "Failed to connect to database: Connection timeout",
                "source_file": "/var/log/app/service.log",
                "line_number": 1234,
                "context": {
                    "environment": "production",
                    "host": "server-01"
                },
                "severity_score": 0.8,
                "category": "database",
                "related_entries": [1230, 1232, 1235],
                "matched_patterns": ["connection_timeout"],
                "extracted_data": {
                    "error_type": "connection_timeout",
                    "service": "database"
                }
            }
        }

class LogAnalysis(BaseModel):
    """Represents the analysis results for a set of log entries."""
    
    # Analysis metadata
    timestamp: datetime = Field(default_factory=datetime.now, description="When the analysis was performed")
    analyzed_files: List[str] = Field(..., description="List of analyzed log files")
    total_entries: int = Field(..., description="Total number of log entries analyzed")
    
    # Summary statistics
    error_count: int = Field(default=0, description="Number of error entries")
    warning_count: int = Field(default=0, description="Number of warning entries")
    critical_patterns: List[str] = Field(default_factory=list, description="Critical patterns identified")
    
    # Detailed analysis
    identified_issues: List[Dict] = Field(default_factory=list, description="List of identified issues")
    patterns: List[Dict] = Field(default_factory=list, description="Detected patterns")
    correlations: List[Dict] = Field(default_factory=list, description="Correlated events")
    
    # AI insights
    root_causes: List[Dict] = Field(default_factory=list, description="Identified root causes")
    recommendations: List[Dict] = Field(default_factory=list, description="Recommended actions")
    
    # Context
    system_context: Dict = Field(default_factory=dict, description="Relevant system context used in analysis")
    
    class Config:
        json_schema_extra = {
            "example": {
                "timestamp": "2023-11-07T10:35:00",
                "analyzed_files": ["/var/log/app/service.log"],
                "total_entries": 1000,
                "error_count": 50,
                "warning_count": 150,
                "critical_patterns": ["database_connection", "memory_usage"],
                "identified_issues": [
                    {
                        "type": "connection_timeout",
                        "frequency": 10,
                        "severity": "high",
                        "affected_services": ["auth-service", "db-service"]
                    }
                ],
                "patterns": [
                    {
                        "pattern": "Connection timeout",
                        "occurrences": 15,
                        "temporal_distribution": "clustered"
                    }
                ],
                "correlations": [
                    {
                        "events": ["high_cpu", "memory_pressure"],
                        "confidence": 0.85
                    }
                ],
                "root_causes": [
                    {
                        "issue": "Database connection timeouts",
                        "cause": "Network congestion",
                        "confidence": 0.9
                    }
                ],
                "recommendations": [
                    {
                        "action": "Increase connection timeout",
                        "priority": "high",
                        "impact": "medium",
                        "implementation": "Update database configuration"
                    }
                ],
                "system_context": {
                    "environment": "production",
                    "system_load": "high",
                    "maintenance_window": "active"
                }
            }
        } 