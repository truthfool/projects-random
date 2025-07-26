#!/bin/bash

# Consistent Hashing Demo Runner Script
# This script builds and runs the consistent hashing demo application

set -e

echo "🚀 Starting Consistent Hashing Demo..."

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
JAR_FILE="target/consistent-hashing-demo-1.0.0.jar"
if [ ! -f "$JAR_FILE" ]; then
    echo "❌ JAR file not found: $JAR_FILE"
    exit 1
fi

echo ""
echo "🎯 Starting the application..."
echo "📡 API will be available at: http://localhost:8080"
echo "🔍 Health check: http://localhost:8080/api/v1/consistent-hashing/health"
echo "📊 Statistics: http://localhost:8080/api/v1/consistent-hashing/stats"
echo ""
echo "Press Ctrl+C to stop the application"
echo ""

# Run the application
java -jar "$JAR_FILE" 