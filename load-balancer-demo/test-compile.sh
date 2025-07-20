#!/bin/bash

echo "Testing Load Balancer Compilation"
echo "================================="

# Create target directory
mkdir -p target/classes

# Compile all Java files
echo "Compiling Java files..."
javac -d target/classes -cp ".:target/classes" src/main/java/com/example/loadbalancer/*.java

if [ $? -eq 0 ]; then
    echo "✅ Compilation successful!"
    echo ""
    echo "Testing SimpleExample..."
    java -cp target/classes com.example.loadbalancer.SimpleExample
else
    echo "❌ Compilation failed!"
    exit 1
fi 