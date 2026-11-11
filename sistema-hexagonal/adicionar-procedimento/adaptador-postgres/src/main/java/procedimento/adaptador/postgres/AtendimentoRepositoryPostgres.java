package procedimento.adaptador.postgres;

import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import procedimento.core.dominio.Atendimento;
import procedimento.core.dominio.CategoriaAtendimento;
import procedimento.core.dominio.ItemAtendimento;
import procedimento.core.dominio.Sexo;
import procedimento.core.porta.out.AtendimentoRepositoryPortOut;

public class AtendimentoRepositoryPostgres implements AtendimentoRepositoryPortOut {

    private final Consulta consulta;

    public AtendimentoRepositoryPostgres(DataSource dataSource) {
        this.consulta = new Consulta(dataSource);
    }

    @Override
    public Optional<Atendimento> buscar(long atendimentoId) {
        List<ItemAtendimento> itens = consulta.lista(
                "select procedimento, perfil, fonte_pagadora_id from atendimento_procedimento where os = ? order by id",
                linha -> new ItemAtendimento(linha.getString("procedimento"), linha.getString("perfil"),
                        (Long) linha.getObject("fonte_pagadora_id")),
                atendimentoId);
        return consulta.uma("""
                        select a.id, a.data, a.categoria, a.posto, a.agendamento, an.sexo as sexo_animal
                        from atendimento a
                        left join animal an on an.id = a.animal
                        where a.id = ?""",
                linha -> new Atendimento(
                        linha.getLong("id"),
                        linha.getObject("data", java.time.LocalDate.class),
                        CategoriaAtendimento.deCodigo(Consulta.inteiro(linha, "categoria")),
                        Sexo.deCodigo(linha.getString("sexo_animal")),
                        Consulta.texto(linha, "posto"),
                        (Long) linha.getObject("agendamento"),
                        itens),
                atendimentoId);
    }

}