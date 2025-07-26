from fastapi import FastAPI, File, UploadFile, HTTPException, BackgroundTasks
from fastapi.middleware.cors import CORSMiddleware
from pathlib import Path
from typing import List, Optional
import asyncio
import uuid
from datetime import datetime

from ..config import settings
from ..models.log_entry import LogAnalysis
from ..services.log_parser import LogParser
from ..services.ai_analyzer import AIAnalyzer
from ..services.context_manager import ContextManager

app = FastAPI(
    title="Log Analyzer API",
    description="AI-powered log analysis and insights",
    version="1.0.0"
)

# Add CORS middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Initialize services
log_parser = LogParser()
ai_analyzer = AIAnalyzer()
context_manager = ContextManager()

# Store ongoing analysis tasks
analysis_tasks = {}

@app.post("/analyze/files")
async def analyze_files(
    files: List[UploadFile],
    context_query: Optional[str] = None,
    background_tasks: BackgroundTasks = None
) -> dict:
    """Start analysis of uploaded log files."""
    try:
        # Generate task ID
        task_id = str(uuid.uuid4())
        
        # Save uploaded files
        saved_files = []
        for file in files:
            file_path = settings.LOGS_DIR / f"{task_id}_{file.filename}"
            with open(file_path, "wb") as f:
                content = await file.read()
                f.write(content)
            saved_files.append(file_path)
        
        # Start analysis task
        task = asyncio.create_task(
            perform_analysis(task_id, saved_files, context_query)
        )
        analysis_tasks[task_id] = {
            "task": task,
            "status": "running",
            "started_at": datetime.now().isoformat(),
            "files": [f.filename for f in files]
        }
        
        return {
            "task_id": task_id,
            "message": "Analysis started",
            "files": [f.filename for f in files]
        }
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/analyze/status/{task_id}")
async def get_analysis_status(task_id: str) -> dict:
    """Get status of analysis task."""
    if task_id not in analysis_tasks:
        raise HTTPException(status_code=404, detail="Task not found")
    
    task_info = analysis_tasks[task_id]
    task = task_info["task"]
    
    if task.done():
        if task.exception():
            status = "failed"
            result = str(task.exception())
        else:
            status = "completed"
            result = task.result()
    else:
        status = "running"
        result = None
    
    return {
        "task_id": task_id,
        "status": status,
        "started_at": task_info["started_at"],
        "files": task_info["files"],
        "result": result
    }

@app.post("/context/add")
async def add_context(
    content: str,
    doc_type: str,
    title: str,
    tags: Optional[List[str]] = None
) -> dict:
    """Add new system context document."""
    try:
        context_id = context_manager.add_system_documentation(
            doc_type=doc_type,
            content=content,
            title=title,
            tags=tags
        )
        
        return {
            "context_id": context_id,
            "message": "Context added successfully"
        }
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/context/error-solution")
async def add_error_solution(
    error_pattern: str,
    solution: str,
    examples: Optional[List[str]] = None
) -> dict:
    """Add known error solution."""
    try:
        context_id = context_manager.add_error_solution(
            error_pattern=error_pattern,
            solution=solution,
            examples=examples
        )
        
        return {
            "context_id": context_id,
            "message": "Error solution added successfully"
        }
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/context/search")
async def search_context(query: str) -> dict:
    """Search context documents."""
    try:
        results = context_manager.search_context(query)
        return {
            "results": results,
            "count": len(results)
        }
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

async def perform_analysis(
    task_id: str,
    file_paths: List[Path],
    context_query: Optional[str] = None
) -> LogAnalysis:
    """Perform log analysis in background."""
    try:
        # Parse log files
        entries = []
        for file_path in file_paths:
            entries.extend(log_parser.parse_file(file_path))
        
        # Perform AI analysis
        analysis = ai_analyzer.analyze_logs(entries, context_query)
        
        # Clean up temporary files
        for file_path in file_paths:
            file_path.unlink()
        
        return analysis.dict()
        
    except Exception as e:
        # Clean up on error
        for file_path in file_paths:
            if file_path.exists():
                file_path.unlink()
        raise

@app.on_event("startup")
async def startup_event():
    """Initialize services on startup."""
    # Ensure required directories exist
    settings.LOGS_DIR.mkdir(parents=True, exist_ok=True)
    settings.CONTEXT_DIR.mkdir(parents=True, exist_ok=True)
    settings.CACHE_DIR.mkdir(parents=True, exist_ok=True)

@app.on_event("shutdown")
async def shutdown_event():
    """Clean up on shutdown."""
    # Cancel any running analysis tasks
    for task_info in analysis_tasks.values():
        if not task_info["task"].done():
            task_info["task"].cancel()
    
    # Clean up temporary files
    for file in settings.LOGS_DIR.glob(f"*"):
        try:
            file.unlink()
        except Exception:
            pass 