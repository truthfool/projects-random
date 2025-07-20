#!/bin/bash

# RabbitMQ Demo Runner Script

echo "RabbitMQ Demo Runner"
echo "===================="
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

# Function to check if RabbitMQ is running
check_rabbitmq() {
    echo "Checking if RabbitMQ is running on localhost:5672..."
    if nc -z localhost 5672 2>/dev/null; then
        echo "✓ RabbitMQ is running"
        return 0
    else
        echo "✗ RabbitMQ is not running on localhost:5672"
        echo ""
        echo "To start RabbitMQ using Docker Compose:"
        echo "  docker-compose up -d"
        echo ""
        echo "Or start RabbitMQ manually and ensure it's running on localhost:5672"
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
    echo "1) Setup queues and exchanges only"
    echo "2) Run complete demo (producer + consumer)"
    echo "3) Run producer only"
    echo "4) Run consumer only"
    echo "5) Start RabbitMQ with Docker Compose"
    echo "6) Stop RabbitMQ Docker containers"
    echo "7) Open RabbitMQ Management UI"
    echo "8) Exit"
    echo ""
    read -p "Enter your choice (1-8): " choice
}

# Handle menu choice
handle_choice() {
    case $choice in
        1)
            echo "Setting up queues and exchanges..."
            mvn exec:java -Dexec.mainClass="com.example.rabbitmq.QueueSetup"
            ;;
        2)
            echo "Running complete demo..."
            mvn exec:java -Dexec.mainClass="com.example.rabbitmq.RabbitMQDemo"
            ;;
        3)
            echo "Running producer only..."
            mvn exec:java -Dexec.mainClass="com.example.rabbitmq.RabbitMQProducerDemo"
            ;;
        4)
            echo "Running consumer only..."
            mvn exec:java -Dexec.mainClass="com.example.rabbitmq.RabbitMQConsumerDemo"
            ;;
        5)
            echo "Starting RabbitMQ with Docker Compose..."
            docker-compose up -d
            echo "✓ RabbitMQ started. Management UI available at http://localhost:15672"
            echo "  Username: guest, Password: guest"
            ;;
        6)
            echo "Stopping RabbitMQ Docker containers..."
            docker-compose down
            echo "✓ RabbitMQ stopped"
            ;;
        7)
            echo "Opening RabbitMQ Management UI..."
            if command -v open &> /dev/null; then
                open http://localhost:15672
            elif command -v xdg-open &> /dev/null; then
                xdg-open http://localhost:15672
            else
                echo "Please open http://localhost:15672 in your browser"
                echo "Username: guest, Password: guest"
            fi
            ;;
        8)
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
        
        if [ "$choice" != "5" ] && [ "$choice" != "6" ] && [ "$choice" != "7" ] && [ "$choice" != "8" ]; then
            check_rabbitmq
        fi
        
        echo ""
        read -p "Press Enter to continue..."
    done
}

# Run main function
main 