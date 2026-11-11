package procedimento.adaptador.postgres;

import java.util.Optional;

import javax.sql.DataSource;

import procedimento.core.dominio.FontePagadora;
import procedimento.core.porta.out.FontePagadoraRepositoryPortOut;

public class FontePagadoraRepositoryPostgres implements FontePagadoraRepositoryPortOut {

    private final Consulta consulta;

    public FontePagadoraRepositoryPostgres(DataSource dataSource) {
        this.consulta = new Consulta(dataSource);
    }

    @Override
    public Optional<FontePagadora> buscar(long fontePagadoraId) {
        return consulta.uma("""
                        select f.*, ao.layout, ao.permite_autorizacao_manual
                        from fonte_pagadora f
                        left join autorizacao_online ao on ao.id = f.parametros_autorizacao_online
                        where f.id = ?""",
                linha -> new FontePagadora(
                        linha.getLong("id"),
                        linha.getString("descricao"),
                        linha.getString("apelido"),
                        linha.getString("tipo_convenio"),
                        Consulta.texto(linha, "tabela"),
                        Consulta.decimal(linha, "valor_teto_os"),
                        Consulta.ligado(linha, "fatura_parcial"),
                        Consulta.ligado(linha, "utiliza_controle_teto_faturamento"),
                        Consulta.ligado(linha, "bloqueia_cadastro_teto_faturamento_atingido"),
                        linha.getObject("faturamento_eletronico_autorizacao_online_id") != null,
                        Consulta.inteiro(linha, "layout"),
                        Consulta.ligado(linha, "permite_autorizacao_manual"),
                        linha.getString("primeiro_isolado")),
                fontePagadoraId);
    }

}