@echo off
echo 🚀 Starting Log Analyzer
echo =======================

REM Check if virtual environment exists
if not exist venv (
    echo Creating virtual environment...
    python -m venv venv
)

REM Activate virtual environment
echo Activating virtual environment...
call venv\Scripts\activate.bat

REM Install dependencies if needed
if not exist venv\Lib\site-packages\fastapi (
    echo Installing dependencies...
    pip install -r requirements.txt
)

REM Create required directories
echo Setting up directories...
if not exist data mkdir data
if not exist logs mkdir logs
if not exist context mkdir context
if not exist vectordb mkdir vectordb
if not exist cache mkdir cache
if not exist uploads mkdir uploads
if not exist processed mkdir processed
if not exist temp mkdir temp

REM Start the application
echo.
echo Starting FastAPI server...
uvicorn src.api.main:app --reload --host 0.0.0.0 --port 8000

REM Cleanup on exit
echo Cleaning up...
deactivate 