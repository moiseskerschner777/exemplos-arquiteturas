import os
import re
import sys

RAIZ = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
COS = os.environ.get("COS_RAIZ", os.path.expanduser("~/Documents/git/sistema-legado"))

GRUPOS = [
    ("core, domínio", "core/src/main/java/procedimento/core/dominio", (".java",)),
    ("core, caso de uso", "core/src/main/java/procedimento/core/casodeuso", (".java",)),
    ("core, portas", "core/src/main/java/procedimento/core/porta", (".java",)),
    ("core, erros", "core/src/main/java/procedimento/core/erro", (".java",)),
    ("core, testes", "core/src/test/java", (".java",)),
    ("adaptador-postgres", "adaptador-postgres/src/main/java", (".java",)),
    ("rest", "rest/src/main/java", (".java",)),
    ("rest, testes", "rest/src/test/java", (".java",)),
    ("frontend", "frontend/src/app", (".ts", ".html", ".css")),
    ("banco: migração e conferência", "banco", (".py",)),
    ("caracterização e comparação", "caracterizacao", (".py",)),
]

METODOS_DE_PRAZO = {
    "s01/s01.util.PrazoExames.cls.xml": [
        "RetornaPrazo", "ControlePrazo", "RetornaHoraEntregaRotina", "VerificaPrazoPorPosto", "VerificaUtilizaRotinaPrazo",
        "VerificaRotinaPrazo", "IsDiaDeRotina", "VerificaS01Parametro", "RecuperaPrazoExame", "RecuperaPrazoMinimoExame",
        "RecuperaTipoPrazoExame", "VerificaPrazoTipoHoras", "VerificaDiasUteisCorridos", "VerificaDataEntrega", "VerificaFeriado",
        "GetListaDiasProducao", "GetHorarioLimite", "GetHorarioEntrega", "ListaContemValores", "VerificarDataProducao",
        "ExcedeuNumeroDeDiasSemana", "HasDiasProducaoDefinido", "IsDiaProdutivo", "GetDiasProducaoByObjPrazoExames",
    ],
    "s01/s01.modelo.calculoPrazo.CalculoPrazo.cls.xml": ["RetornarPrazoAntigo"],
    "s01/s01.iu.codigo.atendimento.cadastro.OsProcedimento.cls.xml": ["GetPrazoExames"],
}


def contar(caminho, extensoes):
    arquivos = linhas = codigo = 0
    for pasta, subpastas, nomes in os.walk(caminho):
        subpastas[:] = [s for s in subpastas if s not in ("node_modules", "__pycache__", "target", "dist")]
        for nome in nomes:
            if not nome.endswith(extensoes):
                continue
            texto = open(os.path.join(pasta, nome), encoding="utf-8").read().split("\n")
            arquivos += 1
            linhas += len(texto)
            codigo += sum(1 for linha in texto if linha.strip())
    return arquivos, linhas, codigo


def linhas_do_metodo(texto, metodo):
    inicio = texto.index(f'<Method name="{metodo}">')
    fim = texto.index("</Method>", inicio)
    return texto[inicio:fim].count("\n") + 1


def main():
    print("código do fluxo migrado, por módulo: arquivos, linhas, linhas não vazias")
    for nome, pasta, extensoes in GRUPOS:
        arquivos, linhas, codigo = contar(os.path.join(RAIZ, pasta), extensoes)
        print(f"    {nome}: {arquivos} arquivos, {linhas} linhas, {codigo} não vazias")
    prazo = open(os.path.join(RAIZ, "core/src/main/java/procedimento/core/dominio/CalculoDePrazo.java"), encoding="utf-8").read()
    print(f"    CalculoDePrazo.java: {prazo.count(chr(10)) + 1} linhas")
    total_metodos = total_linhas = 0
    for arquivo, metodos in METODOS_DE_PRAZO.items():
        texto = open(os.path.join(COS, arquivo), encoding="utf-8").read()
        for metodo in metodos:
            total_metodos += 1
            total_linhas += linhas_do_metodo(texto, metodo)
    print(f"métodos do COS lidos para portar o prazo de entrega: {total_metodos} métodos, {total_linhas} linhas")
    return 0


if __name__ == "__main__":
    sys.exit(main())
