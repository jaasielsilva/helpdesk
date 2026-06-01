#!/bin/bash
# Script para rodar testes

echo "🧪 Executando testes..."
echo ""

# Rodar testes
mvn test -P test

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Todos os testes passaram!"
    echo "📊 Cobertura: mvn jacoco:report"
else
    echo ""
    echo "❌ Alguns testes falharam!"
    exit 1
fi
