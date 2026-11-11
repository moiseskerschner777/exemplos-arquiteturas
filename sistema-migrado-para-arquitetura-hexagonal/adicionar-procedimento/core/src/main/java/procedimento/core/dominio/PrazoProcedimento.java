package procedimento.core.dominio;

public record PrazoProcedimento(
        String prazoCodigoRotina,
        String prazoMinimo,
        Integer prazoMinimoTipo,
        Integer prazoRotinaTipo,
        boolean prazoRotinaUtilizaRotina,
        ListaPosicional prazoCodigoPosto,
        ListaPosicional prazoCodigoRotinaPosto,
        ListaPosicional prazoMinimoPosto,
        ListaPosicional prazoMinimoPostoTipo,
        ListaPosicional prazoRotinaPostoTipo,
        ListaPosicional prazoRotinaPostoUtilizaRotina) {

}