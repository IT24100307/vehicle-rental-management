#!/bin/bash
# Shell script to run the Spring Boot application

echo "Attempting to run the Spring Boot application..."

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "Java not found. Please install Java to run this application."
    exit 1
fi

# Try to find the JAR file
JAR_FILE="target/project1-0.0.1-SNAPSHOT.jar"
if [ ! -f "$JAR_FILE" ]; then
    echo "JAR file not found: $JAR_FILE"
    echo "Please build the application first."
    exit 1
fi

# Run the application
echo "Starting Spring Boot application..."
java -jar $JAR_FILE
