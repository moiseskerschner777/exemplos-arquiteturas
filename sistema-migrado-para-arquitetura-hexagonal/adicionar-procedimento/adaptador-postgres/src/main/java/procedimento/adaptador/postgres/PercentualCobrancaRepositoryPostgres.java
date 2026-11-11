package procedimento.adaptador.postgres;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import procedimento.core.dominio.CobrancaDoDia;
import procedimento.core.porta.out.PercentualCobrancaRepositoryPortOut;

public class PercentualCobrancaRepositoryPostgres implements PercentualCobrancaRepositoryPortOut {

    private final Consulta consulta;

    public PercentualCobrancaRepositoryPostgres(DataSource dataSource) {
        this.consulta = new Consulta(dataSource);
    }

    @Override
    public Optional<String> buscarAgrupamento(long fontePagadoraId, String procedimentoId) {
        return consulta.uma("""
                        select pc.agrupamento
                        from percentual_cobranca pc
                        join percentual_cobranca_fonte_pagadora pf on pf.percentual_cobranca_id = pc.id
                        join percentual_cobranca_procedimento pp on pp.percentual_cobranca_id = pc.id
                        where pc.status and pf.fonte_pagadora_id = ? and pp.procedimento_id = ?
                        order by pc.id""",
                linha -> linha.getString("agrupamento"), fontePagadoraId, procedimentoId);
    }

    @Override
    public List<CobrancaDoDia> buscarCobrancasDoDia(LocalDate data, long fontePagadoraId, String procedimentoId) {
        return consulta.lista("""
                        select a.codigo_os, f.situacao
                        from atendimento_cobranca c
                        join atendimento a on a.id = c.os
                        left join fatura f on f.id = c.fatura
                        where c.data_os = ? and c.fonte_pagadora = ? and c.procedimento = ?
                        order by c.id""",
                linha -> new CobrancaDoDia(linha.getString("codigo_os"), linha.getString("situacao")),
                data, fontePagadoraId, procedimentoId);
    }

}