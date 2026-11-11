package procedimento.core.casodeuso;

import procedimento.core.porta.out.AgendaRepositoryPortOut;
import procedimento.core.porta.out.AtendimentoRepositoryPortOut;
import procedimento.core.porta.out.AutorizacaoRepositoryPortOut;
import procedimento.core.porta.out.ContratoRepositoryPortOut;
import procedimento.core.porta.out.FaturaRepositoryPortOut;
import procedimento.core.porta.out.FontePagadoraRepositoryPortOut;
import procedimento.core.porta.out.MaterialRepositoryPortOut;
import procedimento.core.porta.out.PercentualCobrancaRepositoryPortOut;
import procedimento.core.porta.out.PrazoRepositoryPortOut;
import procedimento.core.porta.out.PrecoRepositoryPortOut;
import procedimento.core.porta.out.ProcedimentoRepositoryPortOut;
import procedimento.core.porta.out.RestricaoUnidadeRepositoryPortOut;
import procedimento.core.porta.out.SetorRepositoryPortOut;
import procedimento.core.porta.out.TetoFaturamentoRepositoryPortOut;
import procedimento.core.porta.out.UnidadeColetaRepositoryPortOut;

public record Repositorios(
        ProcedimentoRepositoryPortOut procedimentos,
        AtendimentoRepositoryPortOut atendimentos,
        UnidadeColetaRepositoryPortOut unidadesColeta,
        RestricaoUnidadeRepositoryPortOut restricoes,
        MaterialRepositoryPortOut materiais,
        FontePagadoraRepositoryPortOut fontesPagadoras,
        FaturaRepositoryPortOut faturas,
        PercentualCobrancaRepositoryPortOut percentuaisCobranca,
        AgendaRepositoryPortOut agendas,
        ContratoRepositoryPortOut contratos,
        AutorizacaoRepositoryPortOut autorizacoes,
        PrazoRepositoryPortOut prazos,
        PrecoRepositoryPortOut precos,
        SetorRepositoryPortOut setores,
        TetoFaturamentoRepositoryPortOut tetosFaturamento) {

}