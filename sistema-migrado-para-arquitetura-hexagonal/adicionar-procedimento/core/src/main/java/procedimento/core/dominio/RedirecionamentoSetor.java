package procedimento.core.dominio;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public record RedirecionamentoSetor(List<Integer> diasDaSemana, LocalTime horaInicial, LocalTime horaFinal, String setorDestinoId) {

    public boolean valeEm(LocalDateTime momento) {
        int dia = momento.getDayOfWeek() == DayOfWeek.SUNDAY ? 0 : momento.getDayOfWeek().getValue();
        LocalTime hora = momento.toLocalTime();
        boolean diaConfere = diasDaSemana.isEmpty() || diasDaSemana.contains(dia);
        boolean depoisDoInicio = horaInicial == null || !hora.isBefore(horaInicial);
        boolean antesDoFim = horaFinal == null || !hora.isAfter(horaFinal);
        return diaConfere && depoisDoInicio && antesDoFim;
    }

    public static Optional<String> destino(List<RedirecionamentoSetor> redirecionamentos, LocalDateTime momento) {
        return redirecionamentos.stream().filter(r -> r.valeEm(momento)).map(RedirecionamentoSetor::setorDestinoId).findFirst();
    }

}