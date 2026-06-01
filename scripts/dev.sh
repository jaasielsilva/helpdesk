#!/bin/bash
# Script para iniciar o backend em DESENVOLVIMENTO

echo "🚀 Iniciando Helpdesk em modo DESENVOLVIMENTO..."
echo ""

# Verificar se Maven está instalado
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven não está instalado. Instale em: https://maven.apache.org"
    exit 1
fi

# Verificar se .env existe
if [ ! -f .env ]; then
    echo "⚠️  Arquivo .env não encontrado!"
    echo "Criando a partir de .env.example..."
    cp .env.example .env
    echo "✅ .env criado. Verifique as configurações antes de rodar."
    exit 1
fi

# Compilar e rodar
mvn clean install -DskipTests && mvn spring-boot:run

echo ""
echo "✅ Aplicação iniciada com sucesso!"
echo "🌐 Acesse: http://localhost:8080"
