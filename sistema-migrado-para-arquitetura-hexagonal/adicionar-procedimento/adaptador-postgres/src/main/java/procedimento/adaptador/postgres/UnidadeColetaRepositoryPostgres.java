package procedimento.adaptador.postgres;

import java.util.Optional;

import javax.sql.DataSource;

import procedimento.core.dominio.UnidadeColeta;
import procedimento.core.porta.out.UnidadeColetaRepositoryPortOut;

public class UnidadeColetaRepositoryPostgres implements UnidadeColetaRepositoryPortOut {

    private final Consulta consulta;

    public UnidadeColetaRepositoryPostgres(DataSource dataSource) {
        this.consulta = new Consulta(dataSource);
    }

    @Override
    public Optional<UnidadeColeta> buscar(String unidadeColetaId) {
        return consulta.uma("select id, unidade_atendimento_id, amostra_encaminhada from unidade_coleta where id = ?",
                linha -> new UnidadeColeta(Consulta.texto(linha, "id"), Consulta.texto(linha, "unidade_atendimento_id"),
                        Consulta.inteiro(linha, "amostra_encaminhada")),
                Long.valueOf(unidadeColetaId));
    }

}