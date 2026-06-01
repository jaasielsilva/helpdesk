@echo off
REM Script para iniciar o backend em DESENVOLVIMENTO (Windows)

echo.
echo 🚀 Iniciando Helpdesk em modo DESENVOLVIMENTO...
echo.

REM Verificar se .env existe
if not exist .env (
    echo ⚠️  Arquivo .env não encontrado!
    echo Criando a partir de .env.example...
    copy .env.example .env
    echo ✅ .env criado. Verifique as configurações antes de rodar.
    exit /b 1
)

REM Compilar e rodar
mvn clean install -DskipTests && mvn spring-boot:run

echo.
echo ✅ Aplicação iniciada com sucesso!
echo 🌐 Acesse: http://localhost:8080
pause
