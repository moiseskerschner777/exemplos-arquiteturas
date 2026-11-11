package procedimento.adaptador.postgres;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import procedimento.core.dominio.RedirecionamentoSetor;
import procedimento.core.porta.out.SetorRepositoryPortOut;

public class SetorRepositoryPostgres implements SetorRepositoryPortOut {

    private final Consulta consulta;

    public SetorRepositoryPostgres(DataSource dataSource) {
        this.consulta = new Consulta(dataSource);
    }

    @Override
    public Optional<String> buscarSetorQueProcessaRotina(String unidadeAtendimentoId, String setorPadraoId) {
        if (unidadeAtendimentoId == null) {
            return Optional.empty();
        }
        return consulta.uma("""
                        select s.id
                        from setor s
                        join setor padrao on padrao.id = ? and padrao.setor_laboratorial_id = s.setor_laboratorial_id
                        where s.unidade_atendimento_id = ? and s.processa_rotina = 1 and s.ativo = 1
                        order by s.id""",
                linha -> linha.getString("id"), setorPadraoId, Long.valueOf(unidadeAtendimentoId));
    }

    @Override
    public List<RedirecionamentoSetor> buscarRedirecionamentos(String setorId, String unidadeColetaId) {
        return consulta.lista("""
                        select dia_semana, hora_inicial, hora_final, setor_destino_id
                        from setor_unidade_processamento
                        where setor_execucao_id = ? and unidade_coleta_id = ?
                        order by id""",
                linha -> new RedirecionamentoSetor(
                        Consulta.listaSeparada(linha.getString("dia_semana")).stream()
                                .filter(dia -> !dia.isEmpty()).map(Integer::valueOf).toList(),
                        linha.getObject("hora_inicial", LocalTime.class),
                        linha.getObject("hora_final", LocalTime.class),
                        linha.getString("setor_destino_id")),
                setorId, Long.valueOf(unidadeColetaId));
    }

}