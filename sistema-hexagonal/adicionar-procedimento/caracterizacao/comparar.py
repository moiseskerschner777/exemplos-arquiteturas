import datetime
import json
import os
import sys

from caracterizar import COS, codigos_cos, como_cos

RAIZ = os.path.dirname(os.path.abspath(__file__))

CAMPOS = ["retorno", "temErroMnemo", "ErroDescricao", "ErroMensagem", "verificaDiversosMaterias", "NaoRealizado",
          "Exame", "IdMaterial", "MaterialDescricao", "Setor", "Recipientes", "Valor", "IsNegociacaoEspecial", "TsaCodigo",
          "AlertaCadastro", "ObsAutorizacao", "RecipienteAmostraEncaminhada", "Mnemonico", "Descricao", "Convenio",
          "MaterialComplementoObrigatorio", "RecipienteNaoPermiteAmostraEncaminhada", "MateriaisPermitidos", "Entrega"]

SO_NO_JAVA = {"Mnemonico": "mnemonico", "Descricao": "descricao", "Convenio": "fontePagadoraId"}


def entrega_cos(texto):
    return texto or ""


def entrega_java(texto):
    if not texto:
        return ""
    return datetime.datetime.fromisoformat(texto[:19]).strftime("%d/%m/%Y - %H:%M")


def lado_java(resultado, codigos):
    status, corpo = resultado["status"], resultado["resposta"]
    valores = como_cos(status, corpo, codigos)
    if status == 200:
        for campo, chave in SO_NO_JAVA.items():
            valores[campo] = corpo[chave]
        valores["MaterialComplementoObrigatorio"] = "1" if corpo["materialComplementoObrigatorio"] else "0"
        valores["RecipienteNaoPermiteAmostraEncaminhada"] = ",".join(corpo["recipientesNaoPermitemAmostraEncaminhada"])
        valores["Entrega"] = entrega_java(corpo["entrega"])
    else:
        valores["MateriaisPermitidos"] = ",".join(corpo.get("materiaisPermitidos") or [])
    return valores


def lado_cos(resultado):
    proxy = resultado.get("proxy") or {}
    valores = {campo: proxy.get(campo, "") for campo in CAMPOS}
    valores["retorno"] = resultado.get("retorno")
    valores["Entrega"] = entrega_cos(proxy.get("Entrega"))
    for campo, padrao in (("temErroMnemo", 0), ("verificaDiversosMaterias", -1), ("IsNegociacaoEspecial", 0)):
        if valores[campo] == "":
            valores[campo] = padrao
    return valores


def igual(a, b):
    if a in ("", None) and b in ("", None):
        return True
    try:
        return float(a) == float(b)
    except (TypeError, ValueError):
        return str(a) == str(b)


def main():
    cos = {r["id"]: r for r in json.load(open(os.path.join(COS, "resultados.json"), encoding="utf-8"))}
    java = {r["id"]: r for r in json.load(open(os.path.join(RAIZ, "resultados.json"), encoding="utf-8"))}
    codigos = codigos_cos()
    diferencas = []
    for cenario_id in cos:
        a = lado_cos(cos[cenario_id])
        b = lado_java(java[cenario_id], codigos)
        for campo in CAMPOS:
            if not igual(a.get(campo), b.get(campo)):
                diferencas.append({"cenario": cenario_id, "campo": campo, "cos": a.get(campo), "java": b.get(campo)})
    campos_cos = sorted({c for r in cos.values() for c in (r.get("proxy") or {})})
    sem_correspondente = [c for c in campos_cos if c not in CAMPOS and c != "exameId"]
    json.dump({"diferencas": diferencas, "camposDoCosSemCorrespondente": sem_correspondente},
              open(os.path.join(RAIZ, "comparacao.json"), "w", encoding="utf-8"), ensure_ascii=False, indent=1)
    por_campo = {}
    for d in diferencas:
        por_campo.setdefault(d["campo"], []).append(d)
    for campo, lista in por_campo.items():
        print(f"{campo}: {len(lista)} cenários")
        for d in lista[:3]:
            print(f"    {d['cenario']}: COS {d['cos']!r}, Java {d['java']!r}")
    print(f"campos do COS sem correspondente no contrato Java ({len(sem_correspondente)}): {', '.join(sem_correspondente)}")
    print(f"{len(cos)} cenários, {len(diferencas)} diferenças em {len(por_campo)} campos")
    return 0


if __name__ == "__main__":
    sys.exit(main())
