@echo off
echo Shutting down Docker Compose...
docker compose down

echo Removing containers...
docker rm -f database adminer task-service

echo Running Gradle build...
call gradle clean build

echo Building Docker image...
docker rmi task-manager
docker build -t task-manager .

echo Checking for Docker network 'application-network'...
docker network inspect application-network >nul 2>&1
IF %ERRORLEVEL% NEQ 0 (
    echo Network 'application-network' not found. Creating it...
    docker network create application-network
) ELSE (
    echo Network 'application-network' already exists.
)

echo Starting Docker Compose...
docker compose up -d

echo All tasks completed.
pause