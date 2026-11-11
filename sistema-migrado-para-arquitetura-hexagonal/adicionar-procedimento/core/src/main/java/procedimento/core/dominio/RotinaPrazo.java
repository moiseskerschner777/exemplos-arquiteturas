package procedimento.core.dominio;

public record RotinaPrazo(
        String id,
        ListaPosicional dias,
        ListaPosicional horarioLimite,
        ListaPosicional horarioEntrega,
        ListaPosicional numDiasApos,
        ListaPosicional tipoDias) {

}