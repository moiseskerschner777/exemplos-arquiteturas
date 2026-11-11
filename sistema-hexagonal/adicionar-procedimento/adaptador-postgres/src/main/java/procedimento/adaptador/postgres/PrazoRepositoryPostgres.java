package procedimento.adaptador.postgres;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import procedimento.core.dominio.ConfiguracaoPrazo;
import procedimento.core.porta.out.PrazoRepositoryPortOut;

public class PrazoRepositoryPostgres implements PrazoRepositoryPortOut {

    private final Consulta consulta;

    public PrazoRepositoryPostgres(DataSource dataSource) {
        this.consulta = new Consulta(dataSource);
    }

    @Override
    public Optional<ConfiguracaoPrazo> buscarRotina(String rotinaPrazoId) {
        return consulta.uma("select dias, horario_entrega, num_dias_apos from rotina_prazo where id = ?",
                linha -> configuracao(linha.getString("dias"), linha.getString("horario_entrega"), linha.getString("num_dias_apos")),
                rotinaPrazoId);
    }

    private static ConfiguracaoPrazo configuracao(String dias, String horarios, String diasApos) {
        List<String> listaDias = Consulta.listaSeparada(dias);
        List<String> listaHorarios = Consulta.listaSeparada(horarios);
        List<String> listaDiasApos = Consulta.listaSeparada(diasApos);
        List<ConfiguracaoPrazo.DiaDeEntrega> entregas = new ArrayList<>();
        for (int i = 0; i < listaDias.size(); i++) {
            if (listaDias.get(i).isEmpty()) {
                continue;
            }
            entregas.add(new ConfiguracaoPrazo.DiaDeEntrega(
                    Integer.parseInt(listaDias.get(i)),
                    i < listaDiasApos.size() && !listaDiasApos.get(i).isEmpty() ? Integer.parseInt(listaDiasApos.get(i)) : 0,
                    i < listaHorarios.size() && !listaHorarios.get(i).isEmpty()
                            ? LocalTime.ofSecondOfDay(Long.parseLong(listaHorarios.get(i))) : null));
        }
        return new ConfiguracaoPrazo(entregas);
    }

}