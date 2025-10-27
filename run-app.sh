#!/bin/bash

# Spring Boot Application Startup Script
# This script sets up the environment and runs the Spring Boot application

echo "Setting up Java 17 environment..."
export JAVA_HOME=$HOME/.java/jdk-17.0.2.jdk/Contents/Home
export PATH=$JAVA_HOME/bin:$PATH

echo "Java version:"
java -version

echo ""
echo "Starting Spring Boot application..."
echo "The application will be available at: http://localhost:8585"
echo "Press Ctrl+C to stop the application"
echo ""

# Add JVM arguments for Java 25 compatibility
export MAVEN_OPTS="--add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED --add-opens java.base/java.lang.reflect=ALL-UNNAMED --add-opens java.base/java.text=ALL-UNNAMED --add-opens java.desktop/java.awt.font=ALL-UNNAMED --add-opens jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED --add-opens jdk.compiler/com.sun.tools.javac.comp=ALL-UNNAMED --add-opens jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED --add-opens jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED --add-opens jdk.compiler/com.sun.tools.javac.model=ALL-UNNAMED --add-opens jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED --add-opens jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED --add-opens jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED --add-opens jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED --add-opens jdk.compiler/com.sun.tools.javac.jvm=ALL-UNNAMED"

./mvnw spring-boot:run -DskipTests
