package procedimento.adaptador.postgres;

import java.util.Optional;

import javax.sql.DataSource;

import procedimento.core.porta.out.AutorizacaoRepositoryPortOut;

public class AutorizacaoRepositoryPostgres implements AutorizacaoRepositoryPortOut {

    private final Consulta consulta;

    public AutorizacaoRepositoryPostgres(DataSource dataSource) {
        this.consulta = new Consulta(dataSource);
    }

    @Override
    public Optional<String> buscarCodigoAmb(String procedimentoId, String tabelaPrecoId) {
        return consulta.uma("select codigo from tabela_preco_item where procedimento_id = ? and tabela_preco_id = ? order by id",
                linha -> linha.getString("codigo"), procedimentoId, Long.valueOf(tabelaPrecoId));
    }

}