package procedimento.core.dominio;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.Optional;

public final class CalculoDePrazo {

    private CalculoDePrazo() {
    }

    /**
     * @param rotina configuração de prazo da rotina do procedimento; {@code null} quando não houver
     */
    public static Optional<LocalDateTime> calcular(Procedimento procedimento, ConfiguracaoPrazo rotina,
                                                   LocalDateTime inicio) {
        if (procedimento.prazoMinimoSegundos() != null) {
            return Optional.of(inicio.plusSeconds(procedimento.prazoMinimoSegundos()));
        }
        if (rotina == null) {
            return Optional.empty();
        }
        return rotina.dias().stream()
                .map(dia -> proximaEntrega(inicio, dia))
                .min(Comparator.naturalOrder());
    }

    private static LocalDateTime proximaEntrega(LocalDateTime inicio, ConfiguracaoPrazo.DiaDeEntrega dia) {
        LocalDate data = inicio.toLocalDate();
        DayOfWeek alvo = DayOfWeek.of(dia.diaDaSemana() == 0 ? 7 : dia.diaDaSemana());
        while (data.getDayOfWeek() != alvo) {
            data = data.plusDays(1);
        }
        LocalTime hora = dia.horarioEntrega() == null ? LocalTime.MIDNIGHT : dia.horarioEntrega();
        return data.plusDays(dia.diasAposColeta()).atTime(hora);
    }

}