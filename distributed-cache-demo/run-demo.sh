#!/bin/bash

# Distributed Cache Demo Runner Script
# This script builds and runs the distributed cache demo application

set -e

echo "🚀 Starting Distributed Cache Demo..."

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "❌ Java is not installed. Please install Java 17 or higher."
    exit 1
fi

# Check Java version
JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    echo "❌ Java 17 or higher is required. Current version: $JAVA_VERSION"
    exit 1
fi

echo "✅ Java version: $(java -version 2>&1 | head -n 1)"

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven is not installed. Please install Maven 3.6 or higher."
    exit 1
fi

echo "✅ Maven version: $(mvn -version | head -n 1)"

# Check if Redis is running
echo "🔍 Checking Redis connection..."
if command -v redis-cli &> /dev/null; then
    if redis-cli ping &> /dev/null; then
        echo "✅ Redis is running"
    else
        echo "⚠️  Redis is not running. Starting Redis with Docker..."
        if command -v docker &> /dev/null; then
            # Check if redis-cache container exists
            if docker ps -a --format "table {{.Names}}" | grep -q "redis-cache"; then
                echo "Starting existing Redis container..."
                docker start redis-cache
            else
                echo "Creating new Redis container..."
                docker run -d --name redis-cache -p 6379:6379 redis:7-alpine
            fi
            sleep 3
            if redis-cli ping &> /dev/null; then
                echo "✅ Redis is now running"
            else
                echo "❌ Failed to start Redis. Please start Redis manually."
                exit 1
            fi
        else
            echo "❌ Docker is not installed. Please install Redis manually."
            echo "   macOS: brew install redis && brew services start redis"
            echo "   Ubuntu: sudo apt-get install redis-server && sudo systemctl start redis"
            exit 1
        fi
    fi
else
    echo "⚠️  redis-cli not found. Please ensure Redis is running on localhost:6379"
fi

# Clean and build the project
echo "🔨 Building the project..."
mvn clean compile

# Run tests
echo "🧪 Running tests..."
mvn test

# Package the application
echo "📦 Packaging the application..."
mvn package -DskipTests

echo "✅ Build completed successfully!"

# Check if the JAR file exists
JAR_FILE="target/distributed-cache-demo-1.0.0.jar"
if [ ! -f "$JAR_FILE" ]; then
    echo "❌ JAR file not found: $JAR_FILE"
    exit 1
fi

echo ""
echo "🎯 Starting the distributed cache application..."
echo "📡 API will be available at: http://localhost:8081"
echo "🔍 Health check: http://localhost:8081/api/v1/cache/health"
echo "📊 Statistics: http://localhost:8081/api/v1/cache/stats"
echo "🏥 Actuator health: http://localhost:8081/actuator/health"
echo ""
echo "📋 Available API endpoints:"
echo "   POST   /api/v1/cache/put              - Store a value"
echo "   GET    /api/v1/cache/get/{key}        - Retrieve a value"
echo "   GET    /api/v1/cache/get/{key}/async  - Async retrieval"
echo "   DELETE /api/v1/cache/remove/{key}     - Remove a value"
echo "   GET    /api/v1/cache/contains/{key}   - Check if key exists"
echo "   POST   /api/v1/cache/batch/put        - Store multiple values"
echo "   POST   /api/v1/cache/batch/get        - Retrieve multiple values"
echo "   DELETE /api/v1/cache/clear            - Clear all data"
echo "   GET    /api/v1/cache/stats            - Get statistics"
echo "   GET    /api/v1/cache/health           - Health check"
echo ""
echo "🔧 Example usage:"
echo "   # Store a value"
echo "   curl -X POST http://localhost:8081/api/v1/cache/put \\"
echo "     -H \"Content-Type: application/json\" \\"
echo "     -d '{\"key\": \"test\", \"value\": \"hello world\", \"ttlSeconds\": 3600}'"
echo ""
echo "   # Retrieve a value"
echo "   curl http://localhost:8081/api/v1/cache/get/test"
echo ""
echo "   # Get statistics"
echo "   curl http://localhost:8081/api/v1/cache/stats | jq"
echo ""
echo "Press Ctrl+C to stop the application"
echo ""

# Run the application
java -jar "$JAR_FILE" 