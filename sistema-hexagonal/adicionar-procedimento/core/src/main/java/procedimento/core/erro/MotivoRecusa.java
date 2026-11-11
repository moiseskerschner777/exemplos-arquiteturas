package procedimento.core.erro;

public enum MotivoRecusa {

    NAO_ENCONTRADO("temErroMnemo=2", 1),
    SEXO_INCOMPATIVEL("temErroMnemo=5", 1),
    RESTRITO_NA_UNIDADE("temErroMnemo=4", 1),
    INATIVO("temErroMnemo=1", 1),
    MATERIAL_DIVERSOS_PENDENTE("verificaDiversosMaterias", 1),
    CATEGORIA_INCOMPATIVEL("temErroMnemo=3", 1),
    FATURA_FECHADA("FaturaFechada", 1),
    FATURA_COMPLEMENTAR_FECHADA("FaturaFechadaComplementar", 1),
    PERCENTUAL_COBRANCA("RegrasPercentualCobranca", 1),
    AGENDA_ESPECIFICA("ProcedimentoAgendaEspecifica", 1),
    ISOLADO_SEM_PRECO("IsoladoSemDefPreco", 1),
    INATIVO_SEGUNDA_VERIFICACAO("procedimentoInativo", 0),
    ANTIBIOGRAMA_SEM_CULTURA("semCulturaAntibiograma", 1),
    CULTURA_SEM_ANTIBIOGRAMA("antibiogramaPrecisaCultura", 0),
    SEM_COBERTURA("fontePagadoraNaoCobreProcedimento", 0),
    CODIGO_AMB_NAO_DEFINIDO("codigoAmbNaoDefinido", 0),
    JA_INCLUIDO("exameJaIncluido", 0),
    JA_INCLUIDO_POR_PERFIL("exameInclusoPerfil", 0),
    ERRO_PRAZO("erroPrazo", 1),
    SEM_PRECO("CalcularValorExame", 0),
    TETO_ATENDIMENTO_EXCEDIDO("ValorTetoExcedido", 0),
    TETO_FATURAMENTO_EXCEDIDO("ValorTetoExcedido", 1),
    SALDO_EM_RECALCULO("ValorTetoExcedido", 1),
    REPETIDO_NO_RECIPIENTE("procedimentoRepetidoEmRecipiente", 0),
    FORA_DO_RECORTE("", 0);

    private final String codigoCos;
    private final int retornoCos;

    MotivoRecusa(String codigoCos, int retornoCos) {
        this.codigoCos = codigoCos;
        this.retornoCos = retornoCos;
    }

    public String codigoCos() {
        return codigoCos;
    }

    public int retornoCos() {
        return retornoCos;
    }

}