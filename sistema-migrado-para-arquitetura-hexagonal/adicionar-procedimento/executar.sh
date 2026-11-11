#!/usr/bin/env bash
set -euo pipefail

RAIZ="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$RAIZ"

PORTA=8081
IRIS="${IRIS_CONTAINER:-lis-iris}"

compilar() {
    echo "compilando"
    mvn -q package -DskipTests
    echo "compilando o frontend"
    if [ ! -d frontend/node_modules ]; then
        (cd frontend && npm ci)
    fi
    (cd frontend && npx ng build)
}

verificar_iris() {
    if [ "$(docker inspect -f '{{.State.Running}}' "$IRIS" 2>/dev/null)" = "true" ]; then
        echo "IRIS local no ar: container $IRIS"
    else
        echo "IRIS local fora do ar: suba o container $IRIS antes dos testes de caracterizacao" >&2
    fi
}

aguardar_app() {
    echo "aguardando a aplicacao"
    for _ in $(seq 1 30); do
        if curl -fs "http://localhost:${PORTA}/health" > /dev/null; then
            curl -s "http://localhost:${PORTA}/health"; echo
            return 0
        fi
        sleep 2
    done
    echo "a aplicacao nao respondeu" >&2
    docker compose logs --tail 40 app >&2
    exit 1
}

subir() {
    compilar
    verificar_iris
    docker compose up -d --wait postgres
    docker compose up -d --force-recreate app
    aguardar_app
    docker compose up -d --force-recreate frontend
    echo "aplicacao em http://localhost:${PORTA}"
    echo "frontend em http://localhost:4201"
}

parar() {
    docker compose down
}

limpar() {
    docker compose down -v
}

logs() {
    docker compose logs -f "${1:-app}"
}

migrar() {
    echo "lendo o IRIS local e gerando banco/schema.sql e banco/dados.sql"
    timeout 300 python3 banco/migrar.py
    docker compose rm -sfv postgres
    docker volume rm -f "$(basename "$RAIZ")_dados-postgres" > /dev/null
    docker compose up -d --wait postgres
    conferir
}

conferir() {
    timeout 300 python3 banco/conferir.py
}

caracterizar() {
    timeout 300 python3 caracterizacao/caracterizar.py
}

sql() {
    docker compose exec postgres psql -U procedimento -d procedimento
}

case "${1:-subir}" in
    subir) subir ;;
    parar) parar ;;
    limpar) limpar ;;
    logs) logs "${2:-}" ;;
    sql) sql ;;
    migrar) migrar ;;
    conferir) conferir ;;
    caracterizar) caracterizar ;;
    *) echo "uso: ./executar.sh [subir|parar|limpar|logs [servico]|sql|migrar|conferir|caracterizar]" >&2; exit 1 ;;
esac
