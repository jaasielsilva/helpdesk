@echo off
REM Script para iniciar o backend em PRODUÇÃO (Windows)

echo.
echo 🚀 Iniciando Helpdesk em modo PRODUÇÃO...
echo.

if not exist .env (
    echo ❌ Arquivo .env não encontrado!
    echo Crie o arquivo .env baseado em .env.example
    echo Depois execute: powershell -ExecutionPolicy Bypass -File scripts\generate-jwt-secret.ps1
    exit /b 1
)

for /f "usebackq tokens=1,* delims==" %%A in (".env") do (
    if /i "%%A"=="JWT_SECRET" set "JWT_SECRET=%%B"
)

if "%JWT_SECRET%"=="" (
    echo ❌ JWT_SECRET não definido no .env
    echo Execute: powershell -ExecutionPolicy Bypass -File scripts\generate-jwt-secret.ps1
    exit /b 1
)

if not exist "target\helpdesk-0.0.1-SNAPSHOT.jar" (
    echo ⚠️  JAR não encontrado. Compilando...
    mvn clean install -P prod -DskipTests
)

set JWT_SECRET=%JWT_SECRET%
java -jar target\helpdesk-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod

echo.
echo ✅ Aplicação iniciada com sucesso em PRODUÇÃO!
echo 🌐 Acesse: http://localhost:8080
pause
