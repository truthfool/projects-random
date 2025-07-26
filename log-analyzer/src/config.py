import os
from pathlib import Path
from typing import Dict, List, Optional
from pydantic_settings import BaseSettings
from dotenv import load_dotenv

# Load environment variables from .env file
load_dotenv()

class Settings(BaseSettings):
    # Base paths
    PROJECT_ROOT: Path = Path(__file__).parent.parent
    DATA_DIR: Path = PROJECT_ROOT / "data"
    LOGS_DIR: Path = PROJECT_ROOT / "logs"
    CONTEXT_DIR: Path = PROJECT_ROOT / "context"
    CACHE_DIR: Path = PROJECT_ROOT / "cache"
    
    # Vector DB settings
    VECTOR_DB_PATH: Path = PROJECT_ROOT / "vectordb"
    EMBEDDING_MODEL: str = "sentence-transformers/all-mpnet-base-v2"
    CHUNK_SIZE: int = 1000
    CHUNK_OVERLAP: int = 200
    
    # Analysis settings
    MAX_LOG_SIZE: int = 100 * 1024 * 1024  # 100MB
    MAX_CONTEXT_DOCS: int = 5
    SIMILARITY_THRESHOLD: float = 0.75
    
    # Log patterns
    LOG_PATTERNS: Dict[str, str] = {
        "timestamp": r"\d{4}-\d{2}-\d{2}\s\d{2}:\d{2}:\d{2}(?:\.\d{3})?",
        "level": r"(?:DEBUG|INFO|WARNING|ERROR|CRITICAL)",
        "service": r"(?:\[[\w-]+\]|\([\w-]+\))",
        "message": r".*$"
    }
    
    # Known error patterns and solutions
    ERROR_PATTERNS: Dict[str, Dict] = {
        "connection_timeout": {
            "pattern": r"(?i)connection.*timeout",
            "severity": "high",
            "category": "network",
            "description": "Connection timeout error indicating network issues",
            "common_causes": [
                "Network congestion",
                "Service unavailable",
                "Firewall blocking"
            ]
        },
        "out_of_memory": {
            "pattern": r"(?i)out.*of.*memory|oom",
            "severity": "critical",
            "category": "resource",
            "description": "Process terminated due to memory exhaustion",
            "common_causes": [
                "Memory leak",
                "Insufficient system resources",
                "Large data processing"
            ]
        }
    }
    
    # AI Analysis settings
    AI_ANALYSIS_PROMPTS: Dict[str, str] = {
        "error_analysis": """
        Analyze the following log entries and identify:
        1. Root cause of the error
        2. Potential impact on the system
        3. Recommended solutions
        4. Prevention measures
        
        Log entries:
        {log_entries}
        
        Context information:
        {context}
        """,
        
        "pattern_detection": """
        Review these log entries and identify:
        1. Recurring patterns or anomalies
        2. Temporal correlations
        3. Related events or cascading failures
        4. System health indicators
        
        Log entries:
        {log_entries}
        
        Previous patterns:
        {patterns}
        """
    }
    
    # Output formatting
    OUTPUT_FORMATS: Dict[str, Dict] = {
        "console": {
            "error": "red",
            "warning": "yellow",
            "info": "green",
            "debug": "blue"
        },
        "html": {
            "error": "#ff0000",
            "warning": "#ffa500",
            "info": "#00ff00",
            "debug": "#0000ff"
        }
    }
    
    # API settings
    API_HOST: str = os.getenv("API_HOST", "0.0.0.0")
    API_PORT: int = int(os.getenv("API_PORT", "8000"))
    API_WORKERS: int = int(os.getenv("API_WORKERS", "4"))
    
    class Config:
        env_file = ".env"
        case_sensitive = True

# Create global settings instance
settings = Settings()

# Ensure required directories exist
for directory in [settings.DATA_DIR, settings.LOGS_DIR, settings.CONTEXT_DIR, 
                 settings.CACHE_DIR, settings.VECTOR_DB_PATH]:
    directory.mkdir(parents=True, exist_ok=True) 