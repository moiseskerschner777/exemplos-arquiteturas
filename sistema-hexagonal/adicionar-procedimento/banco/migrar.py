import datetime
import decimal
import os
import re
import sys

import iris

from tabelas import TABELAS, conectar_iris

RAIZ = os.path.dirname(os.path.abspath(__file__))

RESERVADAS = {
    "all", "analyse", "analyze", "and", "any", "array", "as", "asc", "both", "case", "cast", "check", "collate", "column",
    "constraint", "create", "current_date", "current_time", "current_timestamp", "current_user", "default", "deferrable",
    "desc", "distinct", "do", "else", "end", "except", "false", "fetch", "for", "foreign", "from", "grant", "group", "having",
    "in", "initially", "intersect", "into", "lateral", "leading", "limit", "localtime", "localtimestamp", "not", "null",
    "offset", "on", "only", "or", "order", "placing", "primary", "references", "returning", "select", "session_user", "some",
    "symmetric", "table", "then", "to", "trailing", "true", "union", "unique", "user", "using", "variadic", "when", "where",
    "window", "with",
}


def snake(nome):
    nome = re.sub(r"([a-z0-9])([A-Z])", r"\1_\2", nome)
    nome = re.sub(r"([A-Z]+)([A-Z][a-z])", r"\1_\2", nome)
    return nome.lower()


def ident(nome):
    return f'"{nome}"' if nome in RESERVADAS else nome


def tipo_postgres(tipo, tamanho, precisao, escala):
    tipo = tipo.lower()
    if tipo == "varchar":
        return "text" if tamanho is None or tamanho < 0 or tamanho > 10000 else f"varchar({tamanho})"
    if tipo in ("longvarchar",):
        return "text"
    if tipo in ("bigint",):
        return "bigint"
    if tipo in ("integer", "smallint", "tinyint"):
        return "integer"
    if tipo == "bit":
        return "boolean"
    if tipo in ("double", "float", "real"):
        return "double precision"
    if tipo == "numeric":
        return f"numeric({precisao},{escala})" if precisao else "numeric"
    if tipo in ("date", "time", "timestamp"):
        return tipo
    if tipo in ("longvarbinary", "varbinary", "binary"):
        return "bytea"
    raise ValueError(f"tipo sem mapeamento: {tipo}")


def ler_colunas(cursor, tabela_iris):
    cursor.execute(
        "SELECT COLUMN_NAME, DATA_TYPE, CHARACTER_MAXIMUM_LENGTH, NUMERIC_PRECISION, NUMERIC_SCALE, IS_NULLABLE "
        "FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = 'dado' AND TABLE_NAME = ? ORDER BY ORDINAL_POSITION",
        [tabela_iris],
    )
    return cursor.fetchall()


def ler_indices(cursor, tabela_iris):
    cursor.execute(
        "SELECT INDEX_NAME, COLUMN_NAME, NON_UNIQUE FROM INFORMATION_SCHEMA.INDEXES "
        "WHERE TABLE_SCHEMA = 'dado' AND TABLE_NAME = ? AND PRIMARY_KEY = 0 ORDER BY INDEX_NAME, ORDINAL_POSITION",
        [tabela_iris],
    )
    indices = {}
    for nome, coluna, nao_unico in cursor.fetchall():
        indices.setdefault(nome, {"colunas": [], "unico": not nao_unico})["colunas"].append(coluna)
    return indices


def coluna_do_id(colunas):
    nomes = [c[0] for c in colunas]
    for candidato in ("ID", "ID1", "ID2"):
        if candidato in nomes:
            return candidato
    raise ValueError("tabela sem coluna de id")


def montar_tabela(cursor, tabela_iris, tabela_pg):
    colunas = ler_colunas(cursor, tabela_iris)
    coluna_id = coluna_do_id(colunas)
    mapa = []
    usados = set()
    for nome, tipo, tamanho, precisao, escala, nulo in colunas:
        if nome == coluna_id:
            destino = "id"
        else:
            destino = snake(nome)
            if destino == "id":
                destino = "codigo"
        if destino in usados:
            destino = destino + "_2"
        usados.add(destino)
        mapa.append({
            "iris": nome,
            "pg": destino,
            "tipo": tipo_postgres(tipo, tamanho, precisao, escala),
            "obrigatorio": nulo == "NO" and nome != coluna_id,
            "texto": tipo.lower() in ("varchar", "longvarchar"),
        })
    return {"iris": tabela_iris, "pg": tabela_pg, "id": coluna_id, "colunas": mapa, "indices": ler_indices(cursor, tabela_iris)}


def ddl(tabela):
    linhas = []
    for c in tabela["colunas"]:
        tipo = c["tipo"]
        if c["pg"] == "id":
            linhas.append(f"    id {tipo} primary key")
        else:
            linhas.append(f"    {ident(c['pg'])} {tipo}" + (" not null" if c["obrigatorio"] else ""))
    sql = [f"create table {tabela['pg']} (\n" + ",\n".join(linhas) + "\n);"]
    pg_de = {c["iris"]: c for c in tabela["colunas"]}
    for nome, indice in sorted(tabela["indices"].items()):
        partes = []
        for coluna in indice["colunas"]:
            if coluna not in pg_de:
                partes = []
                break
            c = pg_de[coluna]
            partes.append(f"upper({ident(c['pg'])})" if c["texto"] else ident(c["pg"]))
        if not partes:
            continue
        unico = "unique " if indice["unico"] else ""
        sql.append(f"create {unico}index {tabela['pg']}_{snake(nome)} on {tabela['pg']} ({', '.join(partes)});")
    return "\n".join(sql)


def valor_copy(valor):
    if valor is None:
        return r"\N"
    if isinstance(valor, bool):
        return "t" if valor else "f"
    if isinstance(valor, (bytes, bytearray)):
        return "\\\\x" + bytes(valor).hex()
    if isinstance(valor, (datetime.date, datetime.time, datetime.datetime)):
        return valor.isoformat()
    if isinstance(valor, decimal.Decimal):
        return format(valor, "f")
    texto = str(valor)
    return texto.replace("\\", "\\\\").replace("\t", "\\t").replace("\n", "\\n").replace("\r", "\\r")


def ler_linhas(cursor, tabela):
    colunas = ", ".join(f'"{c["iris"]}"' for c in tabela["colunas"])
    cursor.execute(f"SELECT {colunas} FROM dado.{tabela['iris']} ORDER BY \"{tabela['id']}\"")
    return cursor.fetchall()


def converter(tabela, linha):
    saida = []
    for c, valor in zip(tabela["colunas"], linha):
        if c["tipo"] == "boolean" and valor is not None and not isinstance(valor, bool):
            valor = bool(int(valor))
        saida.append(valor)
    return saida


def sequencia(tabela):
    tipo_id = next(c["tipo"] for c in tabela["colunas"] if c["pg"] == "id")
    if tipo_id not in ("bigint", "integer"):
        return ""
    nome = f"{tabela['pg']}_id_seq"
    return (
        f"create sequence {nome} owned by {tabela['pg']}.id;\n"
        f"alter table {tabela['pg']} alter column id set default nextval('{nome}');\n"
        f"select setval('{nome}', coalesce((select max(id) from {tabela['pg']}), 0) + 1, false);\n"
    )


def main():
    conexao = conectar_iris()
    cursor = conexao.cursor()
    tabelas = [montar_tabela(cursor, iris_nome, pg_nome) for iris_nome, pg_nome in TABELAS]

    with open(os.path.join(RAIZ, "schema.sql"), "w", encoding="utf-8") as schema:
        schema.write("\n\n".join(ddl(t) for t in tabelas))

    vazias = 0
    nulas = 0
    with open(os.path.join(RAIZ, "dados.sql"), "w", encoding="utf-8") as dados:
        for tabela in tabelas:
            linhas = ler_linhas(cursor, tabela)
            colunas = ", ".join(ident(c["pg"]) for c in tabela["colunas"])
            dados.write(f"copy {tabela['pg']} ({colunas}) from stdin;\n")
            for linha in linhas:
                valores = converter(tabela, linha)
                vazias += sum(1 for v in valores if v == "")
                nulas += sum(1 for v in valores if v is None)
                dados.write("\t".join(valor_copy(v) for v in valores) + "\n")
            dados.write("\\.\n\n")
            print(f"{tabela['iris']:40} -> {tabela['pg']:40} {len(linhas):6} linhas")
        dados.write("".join(sequencia(t) for t in tabelas))

    print(f"{len(tabelas)} tabelas; campos com string vazia: {vazias}; campos nulos: {nulas}")


if __name__ == "__main__":
    sys.exit(main())