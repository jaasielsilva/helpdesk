#!/usr/bin/env bash
# Gera JWT_SECRET criptograficamente seguro (256 bits) e grava/substitui no .env
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
ENV_FILE="${1:-.env}"
ENV_PATH="$ROOT/$ENV_FILE"
EXAMPLE_PATH="$ROOT/.env.example"

if [[ ! -f "$ENV_PATH" ]]; then
  if [[ -f "$EXAMPLE_PATH" ]]; then
    cp "$EXAMPLE_PATH" "$ENV_PATH"
    echo "Arquivo $ENV_FILE criado a partir de .env.example"
  else
    touch "$ENV_PATH"
    echo "Arquivo $ENV_FILE criado"
  fi
fi

if command -v openssl >/dev/null 2>&1; then
  SECRET="$(openssl rand -base64 32 | tr -d '\n')"
else
  SECRET="$(python -c "import secrets, base64; print(base64.b64encode(secrets.token_bytes(32)).decode())")"
fi

TMP_FILE="$(mktemp)"
grep -v '^\s*JWT_SECRET=' "$ENV_PATH" > "$TMP_FILE" || true
echo "JWT_SECRET=$SECRET" >> "$TMP_FILE"
mv "$TMP_FILE" "$ENV_PATH"

echo ""
echo "JWT_SECRET gerado com sucesso (256 bits / 32 bytes)"
echo "Arquivo: $ENV_PATH"
echo "Valor anterior substituído."
echo ""
echo "Produção: reinicie o backend com profile prod após gerar o secret."
echo ""
