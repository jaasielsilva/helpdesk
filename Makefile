# Makefile - Helpdesk Build & Development

.PHONY: help build dev prod test clean install run lint format jwt-secret release

help:
	@echo "📋 Helpdesk - Comandos Disponíveis"
	@echo ""
	@echo "🔐 Segurança:"
	@echo "  make jwt-secret    - Gerar JWT_SECRET (256 bits) no .env"
	@echo ""
	@echo "🔨 Build:"
	@echo "  make build         - Compilar projeto (DEV)"
	@echo "  make build-prod    - Compilar para PRODUÇÃO"
	@echo "  make release       - Build JAR v1.x + checksum em release-artifacts/"
	@echo ""
	@echo "🚀 Execução:"
	@echo "  make dev           - Rodar em DESENVOLVIMENTO"
	@echo "  make prod          - Rodar em PRODUÇÃO"
	@echo "  make test          - Executar testes"
	@echo ""
	@echo "🧹 Limpeza:"
	@echo "  make clean         - Limpar build"
	@echo "  make clean-all     - Limpar build + cache Maven"
	@echo ""
	@echo "🔧 Frontend:"
	@echo "  make install-fe    - Instalar dependências Angular"
	@echo "  make dev-fe        - Rodar frontend em DEV"
	@echo ""
	@echo "📝 Código:"
	@echo "  make lint          - Executar validações"
	@echo "  make format        - Formatar código"

build:
	@echo "🔨 Compilando projeto (DEV)..."
	mvn clean install -DskipTests

build-prod:
	@echo "🔨 Compilando para PRODUÇÃO..."
	mvn clean install -P prod -DskipTests

dev:
	@echo "🚀 Iniciando em DESENVOLVIMENTO..."
	mvn spring-boot:run

prod:
	@echo "🚀 Iniciando em PRODUÇÃO..."
	@if [ -f scripts/prod.sh ]; then bash scripts/prod.sh; else java -jar release-artifacts/v1.0.0/helpdesk-1.0.0.jar --spring.profiles.active=prod; fi

release:
	@powershell -ExecutionPolicy Bypass -File scripts/build-release.ps1 -SkipTests

jwt-secret:
	@powershell -ExecutionPolicy Bypass -File scripts/generate-jwt-secret.ps1

test:
	@echo "🧪 Executando testes..."
	mvn test -P test

test-coverage:
	@echo "📊 Executando testes com cobertura..."
	mvn test jacoco:report
	@echo "✅ Relatório em: target/site/jacoco/index.html"

clean:
	@echo "🧹 Limpando build..."
	mvn clean

clean-all: clean
	@echo "🧹 Limpando cache Maven..."
	rm -rf ~/.m2/repository/com/jaasielsilva

install-fe:
	@echo "📦 Instalando dependências Angular..."
	cd frontend && npm install

dev-fe:
	@echo "🚀 Iniciando frontend..."
	cd frontend && npm start

lint:
	@echo "🔍 Validando código..."
	mvn verify

format:
	@echo "📝 Formatando código..."
	mvn spotless:apply

setup: build install-fe
	@echo "✅ Ambiente configurado com sucesso!"

all: build test
	@echo "✅ Build completo com sucesso!"

# Aliases úteis
start: dev
run: dev
server: prod
unit-test: test

.DEFAULT_GOAL := help
