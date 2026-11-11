import iris
import os

TABELAS = [
    ("TblExame", "procedimento"),
    ("TblExame_Recipientes", "procedimento_recipiente"),
    ("TblExameMateriaisPermitidos", "procedimento_material_permitido"),
    ("TblStatusExame", "status_procedimento"),
    ("TblMaterial", "material"),
    ("TblRecipiente", "recipiente"),
    ("ArqOrdemServico", "atendimento"),
    ("TblOsProcedimento", "atendimento_procedimento"),
    ("TblOsCobranca", "atendimento_cobranca"),
    ("TblOsDadoCadastral", "atendimento_dado_cadastral"),
    ("ArqPaciente", "paciente"),
    ("ArqAnimal", "animal"),
    ("ArqFatura", "fatura"),
    ("TblConvenio", "fonte_pagadora"),
    ("TblDadoConvenio", "fonte_pagadora_dado_cadastral"),
    ("TblConvenioTetoFaturamento", "fonte_pagadora_teto_faturamento"),
    ("TblTetoFaturamento", "teto_faturamento"),
    ("TblFontePagadoraProcedimentoIsolado", "fonte_pagadora_procedimento_isolado"),
    ("TblGestaoContrato", "gestao_contrato"),
    ("TblGestaoContratoDadoCadastral", "gestao_contrato_dado_cadastral"),
    ("TblGestaoContratoExcecao", "gestao_contrato_excecao"),
    ("TblPosto", "unidade_coleta"),
    ("TblUnidadeExameRestricao", "unidade_procedimento_restricao"),
    ("TblAgenda", "agenda"),
    ("TblAgendaProcedimento", "agenda_procedimento"),
    ("TblPreco", "tabela_preco"),
    ("TblPrecoItem", "tabela_preco_item"),
    ("TblPrecoItemComponentePreco", "tabela_preco_item_componente"),
    ("TblPrecoItemMatMed", "tabela_preco_item_mat_med"),
    ("TblCodigoCobrancaNegociacaoEspecial", "codigo_cobranca_negociacao_especial"),
    ("TblCruzadoItem", "cruzado_item"),
    ("TblAdicional", "adicional"),
    ("TblUnidadeFinanceira", "unidade_financeira"),
    ("TblRotinaPrazo", "rotina_prazo"),
    ("TblSecao", "setor"),
    ("TblSecaoUnidadeProcessamento", "setor_unidade_processamento"),
    ("TblRegiao", "regiao"),
    ("TblRegiaoItem", "regiao_item"),
    ("TblHorarioVerao", "horario_verao"),
    ("TblTuss", "tuss"),
    ("TblTussCorrelacao", "tuss_correlacao"),
    ("TblTabelaTiss", "tabela_tiss"),
    ("TblParametrosTiss", "parametros_tiss"),
    ("TblParametrosGeraisTiss", "parametros_gerais_tiss"),
    ("TblFaturamentoEletronico", "faturamento_eletronico"),
    ("TblAutorizacaoOnline", "autorizacao_online"),
    ("TblComponentePreco", "componente_preco"),
    ("TblComponentePrecoTabela", "componente_preco_tabela"),
    ("TblComponentePrecoFontePagadora", "componente_preco_fonte_pagadora"),
    ("TblGestaoContratoExcecaoComponente", "gestao_contrato_excecao_componente"),
    ("s01_dado_ordemServico.FranquiaCompartilhada", "franquia_compartilhada"),
    ("s01_dado_percentualCobranca.PercentualCobranca", "percentual_cobranca"),
    ("s01_dado_percentualCobranca.PercentualCobrancaFontePagadora", "percentual_cobranca_fonte_pagadora"),
    ("s01_dado_percentualCobranca.PercentualCobrancaProcedimento", "percentual_cobranca_procedimento"),
]


def esquema_e_tabela(nome_iris):
    if "." in nome_iris:
        return nome_iris.split(".", 1)
    return "dado", nome_iris


def conectar_iris():
    return iris.connect(
        os.environ.get("IRIS_LOCAL_HOST", "localhost"),
        int(os.environ.get("IRIS_LOCAL_PORT", "56770")),
        os.environ.get("IRIS_LOCAL_DATABASE", "LIS"),
        os.environ["IRIS_LOCAL_USERNAME"],
        os.environ["IRIS_LOCAL_PASSWORD"],
    )
