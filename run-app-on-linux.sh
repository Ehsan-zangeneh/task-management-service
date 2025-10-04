#!/bin/bash
set -e  # Exit immediately if a command fails

echo "Shutting down Docker Compose..."
docker compose down || true

echo "Removing containers..."
docker rm -f database adminer task-service 2>/dev/null || true

chmod +x gradlew

echo "Running Gradle build..."
./gradlew clean build -x test

echo "Building Docker image..."
docker rmi task-manager 2>/dev/null || true
docker build -t task-manager .

echo "Checking for Docker network 'application-network'..."
if ! docker network inspect application-network >/dev/null 2>&1; then
    echo "Network 'application-network' not found. Creating it..."
    docker network create application-network
else
    echo "Network 'application-network' already exists."
fi

echo "Starting Docker Compose..."
docker compose up -d

echo "All tasks completed."

