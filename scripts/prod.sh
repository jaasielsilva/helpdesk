#!/bin/bash
# Script para iniciar o backend em PRODUÇÃO

echo "🚀 Iniciando Helpdesk em modo PRODUÇÃO..."
echo ""

# Verificar se .env existe
if [ ! -f .env ]; then
    echo "❌ Arquivo .env não encontrado!"
    echo "Crie o arquivo .env baseado em .env.example"
    exit 1
fi

# Verificar se JAR foi compilado
if [ ! -f target/helpdesk-0.0.1-SNAPSHOT.jar ]; then
    echo "⚠️  JAR não encontrado. Compilando..."
    mvn clean install -P prod -DskipTests
fi

# Source variáveis de ambiente
set -a
source .env
set +a

if [ -z "${JWT_SECRET:-}" ]; then
    echo "❌ JWT_SECRET não definido no .env"
    echo "Execute: make jwt-secret  ou  ./scripts/generate-jwt-secret.sh"
    exit 1
fi

# Rodar aplicação
export JWT_SECRET
java -jar target/helpdesk-0.0.1-SNAPSHOT.jar \
    --spring.profiles.active=prod \
    --server.port=${APP_PORT:-8080} \
    --spring.datasource.url=jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME} \
    --spring.datasource.username=${DB_USER} \
    --spring.datasource.password=${DB_PASSWORD}

echo ""
echo "✅ Aplicação iniciada com sucesso em PRODUÇÃO!"
echo "🌐 Acesse: http://localhost:${APP_PORT:-8080}"
