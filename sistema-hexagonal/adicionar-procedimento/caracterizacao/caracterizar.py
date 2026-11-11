import json
import os
import re
import sys
import urllib.error
import urllib.request

import psycopg2

RAIZ = os.path.dirname(os.path.abspath(__file__))
COS = os.path.join(RAIZ, "..", "..", "..", "sistema-sem-arquitetura", "caracterizacao")
MOTIVOS = os.path.join(RAIZ, "..", "core", "src", "main", "java", "procedimento", "core", "erro", "MotivoRecusa.java")
URL = os.environ.get("APP_URL", "http://localhost:8081")

TRADUCAO = {
    "UPDATE dado.TblExame SET Status = 1 WHERE ID = '34'":
        "update procedimento set status = 1 where id = '34'",
    "UPDATE dado.TblExame SET Status = 1 WHERE ID = '3'":
        "update procedimento set status = 1 where id = '3'",
    "UPDATE dado.TblPrecoItemComponentePreco SET Quantidade = 50, UnidadeFinanceiraId = '1' WHERE PrecoItemId = 3":
        "update tabela_preco_item_componente set quantidade = 50, unidade_financeira_id = '1' where preco_item_id = 3",
    "UPDATE dado.TblExame SET Sexo = 'F' WHERE ID = '34'":
        "update procedimento set sexo = 'F' where id = '34'",
    "INSERT INTO dado.TblUnidadeExameRestricao (ProcedimentoId, UnidadeColetaId) VALUES ('34', 2)":
        "insert into unidade_procedimento_restricao (procedimento_id, unidade_coleta_id) values ('34', 2)",
    "UPDATE dado.TblMaterial SET MaterialDiversos = 1 WHERE ID = '00003'":
        "update material set material_diversos = 1 where id = '00003'",
    "UPDATE dado.TblExame SET TipoExame = 3 WHERE ID = '34'":
        "update procedimento set tipo_exame = 3 where id = '34'",
    "INSERT INTO dado.ArqFatura (Id, Situacao, Convenio, Tipo) VALUES ('CARACT01', 'F', 2, 1)":
        "insert into fatura (id, situacao, convenio, tipo, qtd_exame, qtd_os, total_ch) values ('CARACT01', 'F', 2, 1, 0, 0, 0)",
    "UPDATE dado.TblOsCobranca SET Fatura = 'CARACT01' WHERE ID = 72":
        "update atendimento_cobranca set fatura = 'CARACT01' where id = 72",
    "UPDATE dado.ArqOrdemServico SET Agendamento = 1 WHERE ID = 15":
        "update atendimento set agendamento = 1 where id = 15",
    "UPDATE dado.TblExame SET Tsa = '3' WHERE ID = '34'":
        "update procedimento set tsa = '3' where id = '34'",
    "UPDATE dado.TblExame SET Antibiograma = 1 WHERE ID = '34'":
        "update procedimento set antibiograma = 1 where id = '34'",
    "INSERT INTO dado.TblGestaoContrato (FontePagadoraId, Tipo) VALUES (1, 1)":
        "insert into gestao_contrato (fonte_pagadora_id, tipo) values (1, 1)",
    "INSERT INTO dado.TblGestaoContratoExcecao (GestaoContratoId, ProcedimentoId, NaoCoberto, MotivoNaoCobertura) "
    "SELECT MAX(ID), '34', 1, 'Fora do rol' FROM dado.TblGestaoContrato":
        "insert into gestao_contrato_excecao (gestao_contrato_id, procedimento_id, nao_coberto, negociacao_especial, motivo_nao_cobertura) "
        "select max(id), '34', true, false, 'Fora do rol' from gestao_contrato",
    "INSERT INTO dado.TblGestaoContratoExcecao (GestaoContratoId, ProcedimentoId, NaoCoberto, Autorizacao) "
    "SELECT MAX(ID), '34', 0, 'Exige guia médica' FROM dado.TblGestaoContrato":
        "insert into gestao_contrato_excecao (gestao_contrato_id, procedimento_id, nao_coberto, negociacao_especial, autorizacao) "
        "select max(id), '34', false, false, 'Exige guia médica' from gestao_contrato",
    "INSERT INTO dado.TblGestaoContratoExcecao (GestaoContratoId, ProcedimentoId, NaoCoberto, NegociacaoEspecial) "
    "SELECT MAX(ID), '34', 0, 1 FROM dado.TblGestaoContrato":
        "insert into gestao_contrato_excecao (gestao_contrato_id, procedimento_id, nao_coberto, negociacao_especial) "
        "select max(id), '34', false, true from gestao_contrato",
    "UPDATE dado.TblExame SET AlertaCadastro = 'Jejum de 8 horas' WHERE ID = '34'":
        "update procedimento set alerta_cadastro = 'Jejum de 8 horas' where id = '34'",
    "UPDATE dado.TblExame SET UtilizadoMicrobiologia = 1 WHERE ID = '34'":
        "update procedimento set utilizado_microbiologia = 1 where id = '34'",
    "UPDATE dado.TblMaterial SET ComplementoObrigatorio = 1 WHERE ID = '00003'":
        "update material set complemento_obrigatorio = true where id = '00003'",
    "UPDATE dado.TblConvenio SET ValorTetoOs = 10 WHERE ID = 1":
        "update fonte_pagadora set valor_teto_os = 10 where id = 1",
    "UPDATE dado.TblConvenio SET UtilizaControleTetoFaturamento = 1, BloqueiaCadastroTetoFaturamentoAtingido = 1 WHERE ID = 1":
        "update fonte_pagadora set utiliza_controle_teto_faturamento = 1, bloqueia_cadastro_teto_faturamento_atingido = 1 where id = 1",
    "INSERT INTO dado.TblConvenioTetoFaturamento (FontePagadoraId, DiaInicio, DiaFinal, ValorTeto, CalculandoSaldoPeriodo) "
    "VALUES (1, 1, 31, 1000, 1)":
        "insert into fonte_pagadora_teto_faturamento (fonte_pagadora_id, dia_inicio, dia_final, valor_teto, calculando_saldo_periodo) "
        "values (1, 1, 31, 1000, true)",
    "UPDATE dado.TblPosto SET AmostraEncaminhada = 3 WHERE ID = 2":
        "update unidade_coleta set amostra_encaminhada = 3 where id = 2",
}

TABELAS_ALTERADAS = sorted({re.search(r"(?:update|into) (\w+)", sql).group(1) for sql in TRADUCAO.values()})

CAMPOS_DO_ITEM = ["Exame", "IdMaterial", "MaterialDescricao", "Setor", "Recipientes", "Valor", "IsNegociacaoEspecial",
                  "TsaCodigo", "AlertaCadastro", "ObsAutorizacao", "RecipienteAmostraEncaminhada"]


def conectar():
    return psycopg2.connect(host=os.environ.get("PG_HOST", "localhost"), port=int(os.environ.get("PG_PORT", "5434")),
                            dbname="procedimento", user="procedimento", password="procedimento")


def guardar_base(cursor):
    cursor.execute("drop schema if exists caracterizacao_base cascade; create schema caracterizacao_base")
    for tabela in TABELAS_ALTERADAS:
        cursor.execute(f"create table caracterizacao_base.{tabela} as table {tabela}")


def restaurar_base(cursor):
    for tabela in TABELAS_ALTERADAS:
        cursor.execute(f"delete from {tabela}; insert into {tabela} select * from caracterizacao_base.{tabela}")


def codigos_cos():
    codigos = {}
    for nome, codigo, retorno in re.findall(r'(\w+)\("([^"]*)", (\d)\)', open(MOTIVOS, encoding="utf-8").read()):
        codigos[nome] = (codigo, int(retorno))
    return codigos


def chamar(cenario):
    corpo = {
        "mnemonico": cenario["mnemonico"],
        "sexoPaciente": cenario["sexoPaciente"],
        "fontePagadoraId": cenario["convenio"],
        "materialId": cenario.get("material"),
        "materialDescricao": cenario.get("materialDescricao"),
        "complementoMaterial": cenario.get("proxy", {}).get("MaterialComplemento"),
        "confirmouAgendamento": cenario.get("proxy", {}).get("ConfirmouInclusaoAgendamento") == 1,
        "ignorarValidacoes": cenario.get("ignorarValidacoes") == 1,
    }
    pedido = urllib.request.Request(f"{URL}/attendances/{cenario['osId']}/procedures", json.dumps(corpo).encode(),
                                    {"Content-Type": "application/json"})
    try:
        with urllib.request.urlopen(pedido, timeout=30) as resposta:
            return resposta.status, json.load(resposta)
    except urllib.error.HTTPError as erro:
        return erro.code, json.load(erro)


def texto_cos(texto):
    return (texto or "").replace("\n", "#1#")


def lista_cos(valores):
    return ",".join(valores or [])


def como_cos(status, corpo, codigos):
    obtido = {"retorno": 1, "temErroMnemo": 0, "ErroDescricao": "", "ErroTipo": "", "ErroMensagem": "",
              "verificaDiversosMaterias": -1, "NaoRealizado": ""}
    obtido.update({campo: "" for campo in CAMPOS_DO_ITEM})
    obtido["IsNegociacaoEspecial"] = 0
    if status == 422:
        codigo, retorno = corpo["codigoCos"], corpo["retornoCos"]
        obtido["retorno"] = retorno
        if codigo.startswith("temErroMnemo="):
            obtido["temErroMnemo"] = int(codigo.split("=")[1])
        elif corpo["motivo"] == "MATERIAL_DIVERSOS_PENDENTE":
            obtido["verificaDiversosMaterias"] = corpo["materialId"]
            obtido["MaterialDescricao"] = corpo["materialDescricao"]
        else:
            obtido["ErroDescricao"] = codigo
        if corpo["motivo"] == "SEM_COBERTURA":
            obtido["NaoRealizado"] = texto_cos(corpo["mensagem"])
        return obtido
    if status != 200:
        raise RuntimeError(f"HTTP {status}: {corpo}")
    obtido.update({
        "Exame": corpo["procedimentoId"],
        "IdMaterial": corpo["materialId"],
        "MaterialDescricao": corpo["materialDescricao"],
        "Setor": corpo["setorId"],
        "Recipientes": lista_cos(corpo["recipientes"]),
        "Valor": corpo["valor"],
        "IsNegociacaoEspecial": 1 if corpo["negociacaoEspecial"] else 0,
        "TsaCodigo": corpo["tsaId"] or "",
        "AlertaCadastro": texto_cos(corpo["alertaCadastro"]),
        "ObsAutorizacao": texto_cos(corpo["fraseAutorizacao"]),
        "RecipienteAmostraEncaminhada": lista_cos(corpo["recipientesAmostraEncaminhada"]),
    })
    for pendencia in corpo["pendencias"]:
        codigo, retorno = codigos[pendencia["motivo"]]
        obtido["retorno"] = min(obtido["retorno"], retorno)
        obtido["ErroDescricao"] = codigo
        if codigo == "ValorTetoExcedido":
            obtido["ErroMensagem"] = pendencia["mensagem"]
    return obtido


def igual(esperado, obtido):
    if isinstance(esperado, (int, float)) and not isinstance(esperado, bool) and obtido not in ("", None):
        return float(esperado) == float(obtido)
    return esperado == obtido


def main():
    cenarios = json.load(open(os.path.join(COS, "cenarios.json"), encoding="utf-8"))
    codigos = codigos_cos()
    conexao = conectar()
    conexao.autocommit = True
    cursor = conexao.cursor()
    guardar_base(cursor)
    resultados = []
    try:
        for cenario in cenarios:
            for ajuste in cenario["ajustes"]:
                cursor.execute(TRADUCAO[ajuste])
            try:
                status, corpo = chamar(cenario)
                obtido = como_cos(status, corpo, codigos)
                diferencas = [{"campo": campo, "esperado": valor, "obtido": obtido.get(campo)}
                              for campo, valor in cenario["esperado"].items() if not igual(valor, obtido.get(campo))]
                resultados.append({"id": cenario["id"], "status": status, "resposta": corpo,
                                   "situacao": "passou" if not diferencas else "diferente", "diferencas": diferencas})
            except Exception as erro:
                resultados.append({"id": cenario["id"], "situacao": "erro", "excecao": str(erro), "diferencas": []})
            finally:
                restaurar_base(cursor)
    finally:
        cursor.execute("drop schema caracterizacao_base cascade")
    json.dump(resultados, open(os.path.join(RAIZ, "resultados.json"), "w", encoding="utf-8"), ensure_ascii=False, indent=1)
    falhas = 0
    for cenario, resultado in zip(cenarios, resultados):
        if resultado["situacao"] != "passou":
            falhas += 1
        resumo = resultado.get("excecao") or f"HTTP {resultado['status']}"
        if resultado.get("resposta") and resultado["status"] == 422:
            resumo += " " + resultado["resposta"]["motivo"]
        elif resultado.get("resposta"):
            resumo += " " + ",".join(p["motivo"] for p in resultado["resposta"]["pendencias"])
        print(f"{cenario['id']:8} {cenario['rn']:6} {resultado['situacao']:10} {resumo}")
        for d in resultado["diferencas"]:
            print(f"         {d['campo']}: esperado {d['esperado']!r}, obtido {d['obtido']!r}")
    print(f"{len(resultados)} cenarios, {falhas} fora do esperado")
    return 1 if falhas else 0


if __name__ == "__main__":
    sys.exit(main())
