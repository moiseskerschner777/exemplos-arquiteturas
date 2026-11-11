#!/usr/bin/env bash
set -euo pipefail

RAIZ="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
IRIS="${IRIS_CONTAINER:-lis-iris}"
NAMESPACE="${IRIS_NAMESPACE:-LIS}"
DESTINO=/tmp/caracterizacao

docker exec -u root "$IRIS" rm -rf "$DESTINO"
docker exec -u root "$IRIS" mkdir -p "$DESTINO"
docker cp "$RAIZ/src/." "$IRIS:$DESTINO/src/"
docker cp "$RAIZ/cenarios.json" "$IRIS:$DESTINO/cenarios.json"
docker exec -u root "$IRIS" chmod -R a+rwX "$DESTINO"

docker exec -i "$IRIS" iris session IRIS -U "$NAMESPACE" > "$RAIZ/ultima-execucao.log" 2>&1 <<COS
set sc=\$system.OBJ.LoadDir("$DESTINO/src","ck-d",,1) if 'sc do \$system.Status.DisplayError(sc)
set sc=##class(caracterizacao.Executor).Rodar("$DESTINO/cenarios.json","$DESTINO/resultados.json") if 'sc do \$system.Status.DisplayError(sc)
halt
COS

docker cp "$IRIS:$DESTINO/resultados.json" "$RAIZ/resultados.json"
python3 -I "$RAIZ/relatorio.py" "$RAIZ/cenarios.json" "$RAIZ/resultados.json"
