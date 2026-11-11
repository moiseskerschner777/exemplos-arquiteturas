import datetime
import json
import os
import sys

from caracterizar import COS, codigos_cos, como_cos

RAIZ = os.path.dirname(os.path.abspath(__file__))

CAMPOS = ["retorno", "temErroMnemo", "ErroTipo", "ErroDescricao", "ErroMensagem", "verificaDiversosMaterias", "NaoRealizado",
          "Exame", "IdMaterial", "MaterialDescricao", "Setor", "Recipientes", "Valor", "IsNegociacaoEspecial", "TsaCodigo",
          "AlertaCadastro", "ObsAutorizacao", "RecipienteAmostraEncaminhada", "Mnemonico", "Descricao", "Convenio",
          "MaterialComplementoObrigatorio", "RecipienteNaoPermiteAmostraEncaminhada", "MateriaisPermitidos", "Entrega",
          "PrazoMinimoAposInicio"]

FORMATO_ENTREGA = "%d/%m/%Y - %H:%M"

SO_NO_JAVA = {"Mnemonico": "mnemonico", "Descricao": "descricao", "Convenio": "fontePagadoraId"}


def entrega_cos(texto):
    return texto or ""


def entrega_java(texto):
    if not texto:
        return ""
    return datetime.datetime.fromisoformat(texto[:19]).strftime(FORMATO_ENTREGA)


def segundos_entre(inicio, fim):
    return int((fim - inicio).total_seconds())


def lado_java(resultado, codigos):
    status, corpo = resultado["status"], resultado["resposta"]
    valores = como_cos(status, corpo, codigos)
    if status == 200:
        for campo, chave in SO_NO_JAVA.items():
            valores[campo] = corpo[chave]
        valores["MaterialComplementoObrigatorio"] = "1" if corpo["materialComplementoObrigatorio"] else "0"
        valores["RecipienteNaoPermiteAmostraEncaminhada"] = ",".join(corpo["recipientesNaoPermitemAmostraEncaminhada"])
        valores["Entrega"] = entrega_java(corpo["entrega"])
        inicio = datetime.datetime.fromisoformat(resultado["momento"])
        valores["EntregaAposInicio"] = segundos_entre(inicio, datetime.datetime.strptime(valores["Entrega"], FORMATO_ENTREGA))
        valores["PrazoMinimoAposInicio"] = segundos_entre(inicio, datetime.datetime.fromisoformat(corpo["prazoMinimo"][:19]))
    else:
        valores["MateriaisPermitidos"] = ",".join(corpo.get("materiaisPermitidos") or [])
    return valores


def lado_cos(resultado, data_execucao):
    proxy = resultado.get("proxy") or {}
    valores = {campo: proxy.get(campo, "") for campo in CAMPOS}
    valores["retorno"] = resultado.get("retorno")
    valores["Entrega"] = entrega_cos(proxy.get("Entrega"))
    valores["PrazoMinimoAposInicio"] = ""
    if proxy.get("PrazoMinimoDisplay") and proxy.get("HoraCadastro"):
        inicio = datetime.datetime.combine(data_execucao, datetime.time()) + datetime.timedelta(seconds=int(proxy["HoraCadastro"]))
        prazo_minimo = datetime.datetime.strptime(proxy["PrazoMinimoDisplay"], "%d/%m/%Y - %H:%M:%S")
        valores["PrazoMinimoAposInicio"] = segundos_entre(inicio, prazo_minimo)
        valores["EntregaAposInicio"] = segundos_entre(inicio, datetime.datetime.strptime(proxy["Entrega"], FORMATO_ENTREGA))
    for campo, padrao in (("temErroMnemo", 0), ("verificaDiversosMaterias", -1), ("IsNegociacaoEspecial", 0)):
        if valores[campo] == "":
            valores[campo] = padrao
    return valores


def igual(a, b, campo=None):
    if campo == "PrazoMinimoAposInicio" and isinstance(a, int) and isinstance(b, int):
        return abs(a - b) <= 1
    if a in ("", None) and b in ("", None):
        return True
    try:
        return float(a) == float(b)
    except (TypeError, ValueError):
        return str(a) == str(b)


def entrega_equivalente(cos, java):
    if "EntregaAposInicio" not in cos or "EntregaAposInicio" not in java:
        return False
    return abs(cos["EntregaAposInicio"] - java["EntregaAposInicio"]) <= 60


def main():
    arquivo_cos = os.path.join(COS, "resultados.json")
    data_execucao = datetime.date.fromtimestamp(os.path.getmtime(arquivo_cos))
    cos = {r["id"]: r for r in json.load(open(arquivo_cos, encoding="utf-8"))}
    java = {r["id"]: r for r in json.load(open(os.path.join(RAIZ, "resultados.json"), encoding="utf-8"))}
    codigos = codigos_cos()
    diferencas = []
    for cenario_id in cos:
        a = lado_cos(cos[cenario_id], data_execucao)
        b = lado_java(java[cenario_id], codigos)
        for campo in CAMPOS:
            if campo == "Entrega" and a.get(campo) != b.get(campo) and entrega_equivalente(a, b):
                continue
            if not igual(a.get(campo), b.get(campo), campo):
                diferencas.append({"cenario": cenario_id, "campo": campo, "cos": a.get(campo), "java": b.get(campo)})
    campos_cos = sorted({c for r in cos.values() for c in (r.get("proxy") or {})})
    comparados_por_outro_nome = {"exameId", "PrazoMinimoDisplay", "HoraCadastro"}
    sem_correspondente = [c for c in campos_cos if c not in CAMPOS and c not in comparados_por_outro_nome]
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
