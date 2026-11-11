import datetime
import decimal
import os
import psycopg2
import sys

from migrar import converter_valor, ident, montar_tabela, nome_completo
from tabelas import TABELAS, conectar_iris


def conectar_postgres():
    return psycopg2.connect(
        host=os.environ.get("PG_HOST", "localhost"),
        port=int(os.environ.get("PG_PORT", "5434")),
        dbname="procedimento",
        user="procedimento",
        password="procedimento",
    )


def normalizar(valor):
    if valor is None:
        return None
    if isinstance(valor, bool):
        return valor
    if isinstance(valor, (bytes, bytearray, memoryview)):
        return bytes(valor)
    if isinstance(valor, datetime.datetime):
        return valor.replace(microsecond=0).isoformat(sep=" ")
    if isinstance(valor, (datetime.date, datetime.time)):
        return valor.isoformat()
    if isinstance(valor, (int, float, decimal.Decimal)):
        return decimal.Decimal(str(valor)).normalize()
    return str(valor)


def normalizar_iris(coluna, valor):
    return normalizar(converter_valor(coluna, valor))


def ler_iris(cursor, tabela):
    colunas = ", ".join(f'"{c["iris"]}"' for c in tabela["colunas"])
    cursor.execute(f"SELECT {colunas} FROM {nome_completo(tabela['iris'])}")
    linhas = {}
    for linha in cursor.fetchall():
        valores = tuple(normalizar_iris(c, v) for c, v in zip(tabela["colunas"], linha))
        linhas[str(linha[indice_id(tabela)])] = valores
    return linhas


def ler_postgres(cursor, tabela):
    colunas = ", ".join(ident(c["pg"]) for c in tabela["colunas"])
    cursor.execute(f"select {colunas} from {tabela['pg']}")
    linhas = {}
    for linha in cursor.fetchall():
        valores = tuple(normalizar(v) for v in linha)
        linhas[str(linha[indice_id(tabela)])] = valores
    return linhas


def indice_id(tabela):
    return next(i for i, c in enumerate(tabela["colunas"]) if c["pg"] == "id")


def comparar(tabela, iris_linhas, pg_linhas):
    diferencas = []
    for chave in sorted(set(iris_linhas) | set(pg_linhas)):
        a = iris_linhas.get(chave)
        b = pg_linhas.get(chave)
        if a is None or b is None:
            diferencas.append(f"id {chave}: {'falta no PostgreSQL' if b is None else 'sobra no PostgreSQL'}")
            continue
        for coluna, va, vb in zip(tabela["colunas"], a, b):
            if va != vb:
                diferencas.append(f"id {chave}, {coluna['pg']}: IRIS {va!r}, PostgreSQL {vb!r}")
    return diferencas


def main():
    iris_cursor = conectar_iris().cursor()
    pg_cursor = conectar_postgres().cursor()
    total = 0
    tabelas_com_diferenca = 0
    for iris_nome, pg_nome in TABELAS:
        tabela = montar_tabela(iris_cursor, iris_nome, pg_nome)
        iris_linhas = ler_iris(iris_cursor, tabela)
        pg_linhas = ler_postgres(pg_cursor, tabela)
        diferencas = comparar(tabela, iris_linhas, pg_linhas)
        total += len(iris_linhas)
        situacao = "igual" if not diferencas else f"{len(diferencas)} diferenças"
        print(f"{pg_nome:40} IRIS {len(iris_linhas):6}  PostgreSQL {len(pg_linhas):6}  {situacao}")
        for diferenca in diferencas[:5]:
            print(f"    {diferenca}")
        if diferencas:
            tabelas_com_diferenca += 1
    print(f"{len(TABELAS)} tabelas, {total} linhas, {tabelas_com_diferenca} tabelas com diferença")
    return 1 if tabelas_com_diferenca else 0


if __name__ == "__main__":
    sys.exit(main())
