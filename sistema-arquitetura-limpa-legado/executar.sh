#!/usr/bin/env bash
set -euo pipefail

RAIZ="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$RAIZ"

SCHEMA="adaptador-jdbc-oracle/src/main/resources/schema.sql"
CONEXAO="sistema/sistema@//localhost:1521/FREEPDB1"
PORTA="${PORTA:-8080}"

compilar() {
    echo "compilando e testando"
    mvn -q package
}

aguardar_oracle() {
    echo "aguardando o Oracle"
    for _ in $(seq 1 60); do
        if [ "$(docker compose ps oracle --format '{{.Health}}')" = "healthy" ]; then
            return 0
        fi
        sleep 5
    done
    echo "o Oracle nao ficou pronto" >&2
    exit 1
}

aplicar_schema() {
    local existe
    existe=$(echo "select count(*) from user_tables where table_name = 'CONTA';" \
        | docker compose exec -T oracle sqlplus -s "$CONEXAO" | tr -d '[:space:]' | sed -n 's/.*-\{3,\}\([0-9][0-9]*\)$/\1/p')
    if [ "${existe:-0}" = "0" ]; then
        echo "criando as tabelas e os dados de exemplo"
        docker compose exec -T oracle sqlplus -s "$CONEXAO" < "$SCHEMA" > /dev/null
    fi
}

subir() {
    compilar
    docker compose up -d oracle
    aguardar_oracle
    aplicar_schema
    docker compose up -d --force-recreate tomcat
    echo "sistema em http://localhost:${PORTA}"
}

parar() {
    docker compose down
}

limpar() {
    docker compose down -v
}

logs() {
    docker compose logs -f "${1:-tomcat}"
}

sql() {
    docker compose exec oracle sqlplus "$CONEXAO"
}

uso() {
    echo "uso: ./executar.sh [subir|parar|limpar|logs [servico]|sql]"
    echo "  subir   compila, testa, sobe o Oracle e o Tomcat e cria as tabelas (padrao)"
    echo "  parar   para os containers e mantem os dados"
    echo "  limpar  para os containers e apaga os dados"
    echo "  logs    acompanha os logs do Tomcat, ou do servico informado"
    echo "  sql     abre o sqlplus no banco do sistema"
}

case "${1:-subir}" in
    subir) subir ;;
    parar) parar ;;
    limpar) limpar ;;
    logs) logs "${2:-tomcat}" ;;
    sql) sql ;;
    *) uso; exit 1 ;;
esac
