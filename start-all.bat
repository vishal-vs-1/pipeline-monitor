@echo off
echo =========================================
echo Starting Vigilant Ecosystem...
echo =========================================

echo.
echo [1/3] Starting Kafka...
start "Kafka Server" cmd /k "cd /d C:\kafka && .\bin\windows\kafka-server-start.bat .\config\server.properties"

echo Waiting 5 seconds for Kafka to initialize...
timeout /t 5 /nobreak > nul

echo.
echo [2/3] Starting Spring Boot Backend...
start "Vigilant Backend" cmd /k "cd /d C:\Users\Vishal\Documents\vigilant\vigilant-backend && .\mvnw spring-boot:run"

echo Waiting 10 seconds for Backend to initialize...
timeout /t 10 /nobreak > nul

echo.
echo [3/3] Starting Angular Frontend...
start "Vigilant Frontend" cmd /k "cd /d C:\Users\Vishal\Documents\vigilant\vigilant-frontend && npm start"

echo.
echo =========================================
echo All services have been launched in separate windows!
echo - Kafka is running
echo - Backend is running on port 8080
echo - Frontend is running on port 4200
echo.
echo Note: To stop the services, simply close their respective command prompt windows.
echo =========================================
