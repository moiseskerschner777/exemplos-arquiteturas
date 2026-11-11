package procedimento.adaptador.postgres;

import javax.sql.DataSource;

import procedimento.core.porta.out.AgendaRepositoryPortOut;

public class AgendaRepositoryPostgres implements AgendaRepositoryPortOut {

    private final Consulta consulta;

    public AgendaRepositoryPostgres(DataSource dataSource) {
        this.consulta = new Consulta(dataSource);
    }

    @Override
    public boolean existeAgendaAtiva(String procedimentoId) {
        return consulta.existe("""
                        select 1
                        from agenda_procedimento ap
                        join agenda a on a.id = ap.agenda_id
                        where ap.procedimento_id = ? and a.status = 1""",
                procedimentoId);
    }

}