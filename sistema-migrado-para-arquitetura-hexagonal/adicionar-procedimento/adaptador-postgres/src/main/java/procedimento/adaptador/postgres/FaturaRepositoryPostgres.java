package procedimento.adaptador.postgres;

import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import procedimento.core.porta.out.FaturaRepositoryPortOut;

public class FaturaRepositoryPostgres implements FaturaRepositoryPortOut {

    private final Consulta consulta;

    public FaturaRepositoryPostgres(DataSource dataSource) {
        this.consulta = new Consulta(dataSource);
    }

    @Override
    public List<String> buscarSituacoesDeFatura(long atendimentoId, long fontePagadoraId) {
        return consulta.lista("""
                        select f.situacao
                        from atendimento_cobranca c
                        join fatura f on f.id = c.fatura
                        where c.os = ? and c.fonte_pagadora = ?""",
                linha -> linha.getString("situacao"), atendimentoId, fontePagadoraId);
    }

    @Override
    public boolean existeCobrancaSemFatura(long atendimentoId, long fontePagadoraId) {
        return consulta.existe("select 1 from atendimento_cobranca where os = ? and fonte_pagadora = ? and fatura is null",
                atendimentoId, fontePagadoraId);
    }

    @Override
    public Optional<Long> buscarFontePagadoraComplementar(long atendimentoId) {
        return consulta.uma("select fonte_pagadora_complementar_id from franquia_compartilhada where os_id = ? order by id",
                        linha -> (Long) linha.getObject("fonte_pagadora_complementar_id"), atendimentoId)
                .filter(java.util.Objects::nonNull);
    }

}