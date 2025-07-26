#!/bin/bash

echo "🚀 Starting Log Analyzer"
echo "======================="

# Check if virtual environment exists
if [ ! -d "venv" ]; then
    echo "Creating virtual environment..."
    python3 -m venv venv
fi

# Activate virtual environment
echo "Activating virtual environment..."
source venv/bin/activate

# Install dependencies if needed
if [ ! -f "venv/lib/python3.8/site-packages/fastapi/__init__.py" ]; then
    echo "Installing dependencies..."
    pip install -r requirements.txt
fi

# Create required directories
echo "Setting up directories..."
mkdir -p data logs context vectordb cache uploads processed temp

# Start the application
echo ""
echo "Starting FastAPI server..."
uvicorn src.api.main:app --reload --host 0.0.0.0 --port 8000

# Cleanup on exit
trap 'echo "Cleaning up..." && deactivate' EXIT 