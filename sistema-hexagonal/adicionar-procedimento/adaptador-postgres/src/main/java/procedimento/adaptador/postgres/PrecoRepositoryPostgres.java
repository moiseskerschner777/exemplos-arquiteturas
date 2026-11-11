package procedimento.adaptador.postgres;

import java.util.List;

import javax.sql.DataSource;

import procedimento.core.dominio.ComponentePreco;
import procedimento.core.porta.out.PrecoRepositoryPortOut;

public class PrecoRepositoryPostgres implements PrecoRepositoryPortOut {

    private final Consulta consulta;

    public PrecoRepositoryPostgres(DataSource dataSource) {
        this.consulta = new Consulta(dataSource);
    }

    @Override
    public List<ComponentePreco> buscarComponentes(String tabelaPrecoId, String procedimentoId, long fontePagadoraId) {
        return consulta.lista("""
                        select ic.quantidade, ic.porcentagem, cp.local_definicao,
                               ut.valor_atual as valor_unidade_tabela, uf.valor_atual as valor_unidade_fonte
                        from componente_preco_tabela ct
                        join componente_preco cp on cp.id = ct.componente_preco_id
                        left join tabela_preco_item ti on ti.tabela_preco_id = ct.tabela_preco_id and ti.procedimento_id = ?
                        left join tabela_preco_item_componente ic
                               on ic.preco_item_id = ti.id and ic.componente_preco_id = ct.componente_preco_id
                        left join unidade_financeira ut on ut.id = ic.unidade_financeira_id
                        left join componente_preco_fonte_pagadora cf
                               on cf.componente_preco_id = ct.componente_preco_id and cf.fonte_pagadora_id = ?
                        left join unidade_financeira uf on uf.id = cf.unidade_id
                        where ct.tabela_preco_id = ?
                        order by ct.id""",
                linha -> new ComponentePreco(
                        Consulta.decimal(linha, "quantidade"),
                        Consulta.decimal(linha, "porcentagem"),
                        Integer.valueOf(1).equals(Consulta.inteiro(linha, "local_definicao")),
                        Consulta.decimal(linha, "valor_unidade_tabela"),
                        Consulta.decimal(linha, "valor_unidade_fonte")),
                procedimentoId, fontePagadoraId, Long.valueOf(tabelaPrecoId));
    }

}