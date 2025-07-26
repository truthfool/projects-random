#!/bin/bash

echo "🚀 Setting up PDF Editor Project"
echo "================================="

# Check if Python is installed
if ! command -v python3 &> /dev/null; then
    echo "❌ Python 3 is not installed. Please install Python 3.8 or higher."
    exit 1
fi

# Check if Node.js is installed
if ! command -v node &> /dev/null; then
    echo "❌ Node.js is not installed. Please install Node.js 16 or higher."
    exit 1
fi

# Check if npm is installed
if ! command -v npm &> /dev/null; then
    echo "❌ npm is not installed. Please install npm."
    exit 1
fi

echo "✅ Prerequisites check passed"

# Setup Backend
echo ""
echo "🔧 Setting up Backend (Flask)"
echo "-----------------------------"

cd backend

# Create virtual environment
echo "Creating virtual environment..."
python3 -m venv venv

# Activate virtual environment
echo "Activating virtual environment..."
source venv/bin/activate

# Install Python dependencies
echo "Installing Python dependencies..."
pip install -r requirements.txt

# Create necessary directories
echo "Creating storage directories..."
mkdir -p uploads processed temp

echo "✅ Backend setup completed"

# Setup Frontend
echo ""
echo "🔧 Setting up Frontend (Angular)"
echo "--------------------------------"

cd ../frontend

# Install Node.js dependencies
echo "Installing Node.js dependencies..."
npm install

# Install Angular CLI globally if not already installed
if ! command -v ng &> /dev/null; then
    echo "Installing Angular CLI globally..."
    npm install -g @angular/cli
fi

echo "✅ Frontend setup completed"

# Create start script
echo ""
echo "📝 Creating start script..."
cd ..

cat > start.sh << 'EOF'
#!/bin/bash

echo "🚀 Starting PDF Editor"
echo "======================"

# Start Backend
echo "Starting Flask backend..."
cd backend
source venv/bin/activate
python app.py &
BACKEND_PID=$!

# Wait a moment for backend to start
sleep 3

# Start Frontend
echo "Starting Angular frontend..."
cd ../frontend
npm start &
FRONTEND_PID=$!

echo ""
echo "✅ PDF Editor is starting..."
echo "📱 Frontend: http://localhost:4200"
echo "🔧 Backend:  http://localhost:5000"
echo ""
echo "Press Ctrl+C to stop both services"

# Wait for user to stop
wait

# Cleanup
echo ""
echo "🛑 Stopping services..."
kill $BACKEND_PID $FRONTEND_PID 2>/dev/null
echo "✅ Services stopped"
EOF

chmod +x start.sh

echo ""
echo "🎉 Setup completed successfully!"
echo "================================"
echo ""
echo "To start the application, run:"
echo "  ./start.sh"
echo ""
echo "Or start services individually:"
echo "  Backend:  cd backend && source venv/bin/activate && python app.py"
echo "  Frontend: cd frontend && npm start"
echo ""
echo "📱 Frontend will be available at: http://localhost:4200"
echo "🔧 Backend will be available at:  http://localhost:5000" 