package procedimento.adaptador.postgres;

import java.time.LocalDate;
import java.util.Optional;

import javax.sql.DataSource;

import procedimento.core.dominio.TetoFaturamento;
import procedimento.core.porta.out.TetoFaturamentoRepositoryPortOut;

public class TetoFaturamentoRepositoryPostgres implements TetoFaturamentoRepositoryPortOut {

    private final Consulta consulta;

    public TetoFaturamentoRepositoryPostgres(DataSource dataSource) {
        this.consulta = new Consulta(dataSource);
    }

    @Override
    public Optional<TetoFaturamento> buscarPeriodo(long fontePagadoraId, LocalDate data) {
        return consulta.uma("""
                        select ct.calculando_saldo_periodo, ct.valor_teto,
                               (select t.saldo from teto_faturamento t
                                where t.fonte_pagadora_id = ct.fonte_pagadora_id and ? between t.data_inicio and t.data_final
                                order by t.id desc limit 1) as saldo
                        from fonte_pagadora_teto_faturamento ct
                        where ct.fonte_pagadora_id = ? and ? between ct.dia_inicio and ct.dia_final
                        order by ct.id""",
                linha -> new TetoFaturamento(Consulta.ligado(linha, "calculando_saldo_periodo"), Consulta.decimal(linha, "valor_teto"),
                        Consulta.decimal(linha, "saldo")),
                data, fontePagadoraId, data.getDayOfMonth());
    }

}