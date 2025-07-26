@echo off
echo 🚀 Setting up PDF Editor Project
echo ================================

REM Check if Python is installed
python --version >nul 2>&1
if errorlevel 1 (
    echo ❌ Python is not installed. Please install Python 3.8 or higher.
    pause
    exit /b 1
)

REM Check if Node.js is installed
node --version >nul 2>&1
if errorlevel 1 (
    echo ❌ Node.js is not installed. Please install Node.js 16 or higher.
    pause
    exit /b 1
)

REM Check if npm is installed
npm --version >nul 2>&1
if errorlevel 1 (
    echo ❌ npm is not installed. Please install npm.
    pause
    exit /b 1
)

echo ✅ Prerequisites check passed

REM Setup Backend
echo.
echo 🔧 Setting up Backend (Flask)
echo -----------------------------

cd backend

REM Create virtual environment
echo Creating virtual environment...
python -m venv venv

REM Activate virtual environment
echo Activating virtual environment...
call venv\Scripts\activate.bat

REM Install Python dependencies
echo Installing Python dependencies...
pip install -r requirements.txt

REM Create necessary directories
echo Creating storage directories...
if not exist uploads mkdir uploads
if not exist processed mkdir processed
if not exist temp mkdir temp

echo ✅ Backend setup completed

REM Setup Frontend
echo.
echo 🔧 Setting up Frontend (Angular)
echo --------------------------------

cd ..\frontend

REM Install Node.js dependencies
echo Installing Node.js dependencies...
npm install

REM Install Angular CLI globally if not already installed
ng version >nul 2>&1
if errorlevel 1 (
    echo Installing Angular CLI globally...
    npm install -g @angular/cli
)

echo ✅ Frontend setup completed

REM Create start script
echo.
echo 📝 Creating start script...
cd ..

echo @echo off > start.bat
echo echo 🚀 Starting PDF Editor >> start.bat
echo echo ====================== >> start.bat
echo. >> start.bat
echo echo Starting Flask backend... >> start.bat
echo cd backend >> start.bat
echo call venv\Scripts\activate.bat >> start.bat
echo start python app.py >> start.bat
echo. >> start.bat
echo echo Starting Angular frontend... >> start.bat
echo cd ..\frontend >> start.bat
echo start npm start >> start.bat
echo. >> start.bat
echo echo ✅ PDF Editor is starting... >> start.bat
echo echo 📱 Frontend: http://localhost:4200 >> start.bat
echo echo 🔧 Backend:  http://localhost:5000 >> start.bat
echo echo. >> start.bat
echo pause >> start.bat

echo.
echo 🎉 Setup completed successfully!
echo ================================
echo.
echo To start the application, run:
echo   start.bat
echo.
echo Or start services individually:
echo   Backend:  cd backend ^&^& venv\Scripts\activate.bat ^&^& python app.py
echo   Frontend: cd frontend ^&^& npm start
echo.
echo 📱 Frontend will be available at: http://localhost:4200
echo 🔧 Backend will be available at:  http://localhost:5000
pause 