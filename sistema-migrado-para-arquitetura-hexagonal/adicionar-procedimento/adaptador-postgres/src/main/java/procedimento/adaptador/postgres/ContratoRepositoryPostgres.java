package procedimento.adaptador.postgres;

import java.util.Optional;

import javax.sql.DataSource;

import procedimento.core.dominio.ExcecaoContrato;
import procedimento.core.porta.out.ContratoRepositoryPortOut;

public class ContratoRepositoryPostgres implements ContratoRepositoryPortOut {

    private final Consulta consulta;

    public ContratoRepositoryPostgres(DataSource dataSource) {
        this.consulta = new Consulta(dataSource);
    }

    @Override
    public Optional<ExcecaoContrato> buscarExcecao(long fontePagadoraId, String procedimentoId) {
        return consulta.uma("""
                        select e.*
                        from gestao_contrato_excecao e
                        join gestao_contrato g on g.id = e.gestao_contrato_id
                        where g.fonte_pagadora_id = ? and e.procedimento_id = ? and e.gestao_contrato_dado_cadastral_id is null
                        order by e.id desc""",
                linha -> new ExcecaoContrato(
                        Consulta.ligado(linha, "nao_coberto"),
                        linha.getString("motivo_nao_cobertura"),
                        Consulta.ligado(linha, "negociacao_especial"),
                        linha.getString("autorizacao"),
                        Consulta.texto(linha, "tabela_preco_id")),
                fontePagadoraId, procedimentoId);
    }

    @Override
    public Optional<String> buscarIsolado(long fontePagadoraId, String procedimentoId) {
        return consulta.uma("""
                        select primeiro_isolado_id from fonte_pagadora_procedimento_isolado
                        where convenio_id = ? and procedimento_id = ? order by id""",
                        linha -> linha.getString("primeiro_isolado_id"), fontePagadoraId, procedimentoId)
                .filter(isolado -> !isolado.isBlank());
    }

}