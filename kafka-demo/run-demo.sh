#!/bin/bash

# Kafka Demo Runner Script

echo "Kafka Demo Runner"
echo "================="
echo ""

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "Error: Maven is not installed. Please install Maven first."
    exit 1
fi

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "Error: Java is not installed. Please install Java 11 or higher."
    exit 1
fi

# Function to check if Kafka is running
check_kafka() {
    echo "Checking if Kafka is running on localhost:9092..."
    if nc -z localhost 9092 2>/dev/null; then
        echo "✓ Kafka is running"
        return 0
    else
        echo "✗ Kafka is not running on localhost:9092"
        echo ""
        echo "To start Kafka using Docker Compose:"
        echo "  docker-compose up -d"
        echo ""
        echo "Or start Kafka manually and ensure it's running on localhost:9092"
        return 1
    fi
}

# Function to build the project
build_project() {
    echo "Building the project..."
    mvn clean compile
    if [ $? -eq 0 ]; then
        echo "✓ Project built successfully"
    else
        echo "✗ Build failed"
        exit 1
    fi
}

# Main menu
show_menu() {
    echo ""
    echo "Choose an option:"
    echo "1) Setup topics only"
    echo "2) Run complete demo (producer + consumer)"
    echo "3) Run producer only"
    echo "4) Run consumer only"
    echo "5) Start Kafka with Docker Compose"
    echo "6) Stop Kafka Docker containers"
    echo "7) Exit"
    echo ""
    read -p "Enter your choice (1-7): " choice
}

# Handle menu choice
handle_choice() {
    case $choice in
        1)
            echo "Setting up topics..."
            mvn exec:java -Dexec.mainClass="com.example.kafka.TopicSetup"
            ;;
        2)
            echo "Running complete demo..."
            mvn exec:java -Dexec.mainClass="com.example.kafka.KafkaDemo"
            ;;
        3)
            echo "Running producer only..."
            mvn exec:java -Dexec.mainClass="com.example.kafka.KafkaProducerDemo"
            ;;
        4)
            echo "Running consumer only..."
            mvn exec:java -Dexec.mainClass="com.example.kafka.KafkaConsumerDemo"
            ;;
        5)
            echo "Starting Kafka with Docker Compose..."
            docker-compose up -d
            echo "✓ Kafka started. UI available at http://localhost:8080"
            ;;
        6)
            echo "Stopping Kafka Docker containers..."
            docker-compose down
            echo "✓ Kafka stopped"
            ;;
        7)
            echo "Goodbye!"
            exit 0
            ;;
        *)
            echo "Invalid choice. Please try again."
            ;;
    esac
}

# Main execution
main() {
    build_project
    
    while true; do
        show_menu
        handle_choice
        
        if [ "$choice" != "5" ] && [ "$choice" != "6" ] && [ "$choice" != "7" ]; then
            check_kafka
        fi
        
        echo ""
        read -p "Press Enter to continue..."
    done
}

# Run main function
main 