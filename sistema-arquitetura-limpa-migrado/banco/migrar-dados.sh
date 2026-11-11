#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "${BASH_SOURCE[0]}")/.."

ORACLE="sistema/sistema@//localhost:1521/FREEPDB1"
LEGADO="../sistema-arquitetura-limpa-legado"

oracle() {
    docker compose --project-directory "$LEGADO" -f "$LEGADO/docker-compose.yml" exec -T oracle sqlplus -s "$ORACLE"
}

exportar() {
    printf 'SET MARKUP CSV ON QUOTE ON\nSET FEEDBACK OFF\nSET HEADING OFF\nSET PAGESIZE 0\n%s\n' "$1" \
        | oracle | sed '/^$/d'
}

importar() {
    docker compose exec -T postgres psql -q -U sistema -d sistema -v ON_ERROR_STOP=1 -c "\\copy $1 FROM STDIN WITH (FORMAT csv)"
}

consultar_oracle() {
    printf 'SET FEEDBACK OFF\nSET HEADING OFF\nSET PAGESIZE 0\n%s\n' "$1" \
        | oracle | tr -d '[:space:]'
}

consultar_postgres() {
    docker compose exec -T postgres psql -At -U sistema -d sistema -c "$1" | tr -d '[:space:]'
}

conferir() {
    local tabela="$1" consulta="$2" oracle postgres
    oracle=$(consultar_oracle "$consulta;")
    postgres=$(consultar_postgres "$consulta")
    if [ "$oracle" = "$postgres" ]; then
        echo "$tabela: igual ($oracle)"
    else
        echo "$tabela: diferente, Oracle $oracle, PostgreSQL $postgres" >&2
        exit 1
    fi
}

tabela_conta() {
    echo "migrando conta"
    exportar "SELECT id, titular, TO_CHAR(saldo, 'FM999999999990.00'), situacao FROM conta ORDER BY id;" | importar conta
    conferir conta "SELECT COUNT(*) || '|' || TO_CHAR(COALESCE(SUM(saldo), 0), 'FM999999999990.00') FROM conta"
}

tabela_movimentacao() {
    echo "migrando movimentacao"
    exportar "SELECT id, conta_id, tipo, TO_CHAR(valor, 'FM999999999990.00'), descricao, TO_CHAR(data_hora, 'YYYY-MM-DD HH24:MI:SS.FF6') FROM movimentacao ORDER BY id;" \
        | importar movimentacao
    conferir movimentacao "SELECT COUNT(*) || '|' || TO_CHAR(COALESCE(SUM(valor), 0), 'FM999999999990.00') || '|' || COUNT(descricao) FROM movimentacao"
    docker compose exec -T postgres psql -q -U sistema -d sistema \
        -c "SELECT setval('seq_movimentacao', COALESCE((SELECT MAX(id) FROM movimentacao), 1))" > /dev/null
    echo "seq_movimentacao ajustada"
}

case "${1:-todas}" in
    conta) tabela_conta ;;
    movimentacao) tabela_movimentacao ;;
    todas) tabela_conta; tabela_movimentacao ;;
    *) echo "uso: banco/migrar-dados.sh [conta|movimentacao|todas]"; exit 1 ;;
esac
