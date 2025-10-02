#!/bin/bash

# Weather Info Service - Local Build and Run Script
# This script builds and runs the Vert.x weather information service

set -e  # Exit on any error

echo "Starting Weather Info Service Build and Run..."
echo "=================================================="

# Check if Java is available
if ! command -v java &> /dev/null; then
    echo "Java is not installed or not in PATH"
    echo "Please install Java 17+ to use this script"
    exit 1
fi

# Check if javac is available
if ! command -v javac &> /dev/null; then
    echo "Java compiler (javac) is not installed or not in PATH"
    echo "Please install JDK 17+ to use this script"
    exit 1
fi

# Create target directories if they don't exist
echo "Creating build directories..."
mkdir -p target/classes
mkdir -p target/test-classes

# Clean previous build
echo "Cleaning previous build..."
rm -rf target/classes/*
rm -rf target/test-classes/* 2>/dev/null || true

# Check if Maven repository exists (for dependencies)
if [ ! -d "$HOME/.m2/repository" ]; then
    echo "Maven repository not found at $HOME/.m2/repository"
    echo "Please install Maven and run 'mvn dependency:resolve' first to download dependencies"
    echo "Or ensure dependencies are available in the classpath"
    exit 1
fi

# Compile main classes
echo "Compiling main classes..."
javac -cp "$(find ~/.m2/repository -name "*.jar" | tr '\n' ':')" \
      -d target/classes \
      $(find src/main/java -name "*.java")

echo "Main classes compiled successfully"

# Compile test classes (optional)
echo "Compiling test classes..."
if javac -cp "target/classes:$(find ~/.m2/repository -name "*.jar" | tr '\n' ':')" \
         -d target/test-classes \
         $(find src/test/java -name "*.java") 2>/dev/null; then
    echo "Test classes compiled successfully"
else
    echo "Test compilation skipped (optional)"
fi

# Check if config.json exists
if [ ! -f "config.json" ]; then
    echo "Warning: config.json not found. Using default configuration."
fi

echo ""
echo "Starting Weather Info Service..."
echo "Server will be available at: http://localhost:8080"
echo ""
echo "Available endpoints:"
echo "  GET  /hello                           - Health check"
echo "  GET  /getCurrentAirPollution          - Air pollution data"
echo "  POST /api/v1/weather/multi-city       - Multi-city weather"
echo ""
echo "Press Ctrl+C to stop the server"
echo "=================================================="

# Run the application
java -cp "target/classes:$(find ~/.m2/repository -name "*.jar" | tr '\n' ':')" \
     org.lotlinx.interview.MainServerVerticle