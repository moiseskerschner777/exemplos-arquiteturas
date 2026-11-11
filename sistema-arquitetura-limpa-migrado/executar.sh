#!/usr/bin/env bash
set -euo pipefail

RAIZ="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$RAIZ"

compilar() {
    echo "compilando e testando"
    mvn -q package
    echo "compilando o frontend"
    (cd frontend && npx ng build > /dev/null)
}

subir() {
    compilar
    docker compose up -d --force-recreate postgres web-spring roteador
    echo "sistema em http://localhost:8090"
}

parar() {
    docker compose down
}

limpar() {
    docker compose down -v
}

logs() {
    docker compose logs -f "${1:-web-spring}"
}

migrar() {
    docker compose stop web-spring
    banco/migrar-dados.sh "${1:-todas}"
    docker compose start web-spring
}

sql() {
    docker compose exec postgres psql -U sistema -d sistema
}

uso() {
    echo "uso: ./executar.sh [subir|parar|limpar|logs [servico]|migrar [tabela]|sql]"
    echo "  subir   compila, testa e sobe o PostgreSQL, o web-spring e o roteador (padrao)"
    echo "  parar   para os containers e mantem os dados"
    echo "  limpar  para os containers e apaga os dados"
    echo "  logs    acompanha os logs do web-spring, ou do servico informado"
    echo "  migrar  copia os dados do Oracle do sistema-arquitetura-limpa-legado para o PostgreSQL, tabela por tabela"
    echo "  sql     abre o psql no banco do sistema"
}

case "${1:-subir}" in
    subir) subir ;;
    parar) parar ;;
    limpar) limpar ;;
    logs) logs "${2:-web-spring}" ;;
    migrar) migrar "${2:-todas}" ;;
    sql) sql ;;
    *) uso; exit 1 ;;
esac
