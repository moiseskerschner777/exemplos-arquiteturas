import json
import sys

cenarios = {c["id"]: c for c in json.load(open(sys.argv[1], encoding="utf-8"))}
resultados = json.load(open(sys.argv[2], encoding="utf-8"))
falhas = 0
for r in resultados:
    c = cenarios[r["id"]]
    situacao = r.get("situacao", "")
    if situacao != "passou":
        falhas += 1
    proxy = r.get("proxy") or {}
    resumo = r.get("excecao") or "retorno=%s temErroMnemo=%s ErroDescricao=%s" % (
        r.get("retorno"), proxy.get("temErroMnemo"), proxy.get("ErroDescricao"))
    print("%-8s %-6s %-12s %s" % (r["id"], c.get("rn", ""), situacao, resumo[:110]))
    for d in r.get("diferencas", []):
        print("         %s: esperado %r, obtido %r" % (d["campo"], d["esperado"], d["obtido"]))
print("%d cenarios, %d fora do esperado" % (len(resultados), falhas))
sys.exit(1 if falhas else 0)
