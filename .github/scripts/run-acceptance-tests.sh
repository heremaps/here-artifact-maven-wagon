#!/bin/bash

# Script to run acceptance tests with different Java and Maven versions
# Usage: ./run-acceptance-tests.sh [java_version] [maven_version]

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

JAVA_VERSION=${1:-"17"}
MAVEN_VERSION=${2:-"default"}

echo "============================================"
echo "Running HERE Artifact Wagon Acceptance Tests"
echo "============================================"
echo "Java Version: $JAVA_VERSION"
echo "Maven Version: $MAVEN_VERSION"
echo "============================================"

# Set Java version if using SDKMAN
if command -v sdk &> /dev/null; then
    echo "Setting Java version using SDKMAN..."
    case $JAVA_VERSION in
        8)
            sdk use java 8.0.392-tem 2>/dev/null || echo "Java 8 not available via SDKMAN, using system default"
            ;;
        17)
            sdk use java 17.0.9-tem 2>/dev/null || echo "Java 17 not available via SDKMAN, using system default"
            ;;
    esac
fi

# Check Java version
echo ""
echo "Current Java version:"
java -version
echo ""

# Run tests
cd "$PROJECT_DIR"

if [ "$MAVEN_VERSION" = "default" ]; then
    echo "Running tests with default Maven..."
    mvn clean test -Pacceptance-tests
else
    echo "Running tests with Maven $MAVEN_VERSION..."
    if [ -d "$HOME/.m2/maven-$MAVEN_VERSION" ]; then
        "$HOME/.m2/maven-$MAVEN_VERSION/bin/mvn" clean test -Pacceptance-tests
    else
        echo "Maven $MAVEN_VERSION not found at $HOME/.m2/maven-$MAVEN_VERSION"
        echo "Using default Maven..."
        mvn clean test -Pacceptance-tests
    fi
fi

echo ""
echo "============================================"
echo "Tests completed!"
echo "============================================"
echo "View reports at: target/cucumber-reports/cucumber.html"
echo ""

