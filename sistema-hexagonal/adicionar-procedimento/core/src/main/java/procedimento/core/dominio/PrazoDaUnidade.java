package procedimento.core.dominio;

public record PrazoDaUnidade(
        String id,
        String descricao,
        String unidadeAtendimentoId,
        String tipoLiberacao,
        ListaPosicional diaProducao,
        ListaPosicional diaEntrega,
        Integer horarioLimiteRotina,
        Integer horarioEntrega) {

    public boolean liberaPorProcedimento() {
        return "P".equals(tipoLiberacao);
    }

}