@echo off
REM Script para rodar testes (Windows)

echo.
echo 🧪 Executando testes...
echo.

mvn test -P test

if %errorlevel% equ 0 (
    echo.
    echo ✅ Todos os testes passaram!
    echo 📊 Cobertura: mvn jacoco:report
) else (
    echo.
    echo ❌ Alguns testes falharam!
    exit /b 1
)
pause
