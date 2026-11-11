package procedimento.adaptador.postgres;

import javax.sql.DataSource;

import procedimento.core.porta.out.RestricaoUnidadeRepositoryPortOut;

public class RestricaoUnidadeRepositoryPostgres implements RestricaoUnidadeRepositoryPortOut {

    private final Consulta consulta;

    public RestricaoUnidadeRepositoryPostgres(DataSource dataSource) {
        this.consulta = new Consulta(dataSource);
    }

    @Override
    public boolean existeRestricao(String procedimentoId, String unidadeColetaId, String unidadeAtendimentoId) {
        return consulta.existe("""
                        select 1 from unidade_procedimento_restricao
                        where procedimento_id = ?
                          and (unidade_coleta_id = ? or (unidade_coleta_id is null and unidade_atendimento_id = ?))""",
                procedimentoId, numero(unidadeColetaId), numero(unidadeAtendimentoId));
    }

    private static Long numero(String valor) {
        return valor == null ? null : Long.valueOf(valor);
    }

}