package procedimento.core.dominio;

import java.time.LocalTime;
import java.util.List;

public record ConfiguracaoPrazo(List<DiaDeEntrega> dias) {

    public record DiaDeEntrega(int diaDaSemana, int diasAposColeta, LocalTime horarioEntrega) {

    }

}