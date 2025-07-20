#!/bin/bash

echo "Load Balancer Demo"
echo "=================="

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "Error: Maven is not installed. Please install Maven first."
    exit 1
fi

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "Error: Java is not installed. Please install Java 11 or higher first."
    exit 1
fi

echo "Building project..."
mvn clean compile

if [ $? -ne 0 ]; then
    echo "Error: Build failed."
    exit 1
fi

echo ""
echo "Choose an option:"
echo "1. Run comprehensive demo (all algorithms)"
echo "2. Run simple example"
echo "3. Run both"

read -p "Enter your choice (1-3): " choice

case $choice in
    1)
        echo "Running comprehensive demo..."
        mvn exec:java -Dexec.mainClass="com.example.loadbalancer.LoadBalancerDemo"
        ;;
    2)
        echo "Running simple example..."
        mvn exec:java -Dexec.mainClass="com.example.loadbalancer.SimpleExample"
        ;;
    3)
        echo "Running simple example first..."
        mvn exec:java -Dexec.mainClass="com.example.loadbalancer.SimpleExample"
        echo ""
        echo "Running comprehensive demo..."
        mvn exec:java -Dexec.mainClass="com.example.loadbalancer.LoadBalancerDemo"
        ;;
    *)
        echo "Invalid choice. Exiting."
        exit 1
        ;;
esac 