#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

VERSION="$(grep -m1 '<version>' pom.xml | sed 's/.*<version>\(.*\)<\/version>.*/\1/')"
JAR_NAME="helpdesk-${VERSION}.jar"
OUT_DIR="${ROOT}/release-artifacts/v${VERSION}"

echo "Building Helpdesk Pro v${VERSION} ..."

if [[ "${1:-}" == "--skip-tests" ]]; then
  mvn -q clean package -DskipTests
else
  mvn -q clean package
fi

BUILT_JAR="${ROOT}/target/${JAR_NAME}"
[[ -f "$BUILT_JAR" ]] || { echo "JAR not found: $BUILT_JAR"; exit 1; }

mkdir -p "$OUT_DIR"
cp -f "$BUILT_JAR" "${OUT_DIR}/${JAR_NAME}"

(
  cd "$OUT_DIR"
  sha256sum "$JAR_NAME" > "${JAR_NAME}.sha256"
)

echo ""
echo "Release artifact ready:"
echo "  ${OUT_DIR}/${JAR_NAME}"
echo "  ${OUT_DIR}/${JAR_NAME}.sha256"
