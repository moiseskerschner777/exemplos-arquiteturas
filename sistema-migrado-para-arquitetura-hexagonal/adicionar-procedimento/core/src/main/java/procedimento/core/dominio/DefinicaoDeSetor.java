package procedimento.core.dominio;

public final class DefinicaoDeSetor {

    private DefinicaoDeSetor() {
    }

    public static String definir(Procedimento procedimento, String unidadeColetaId, String setorQueProcessaRotina) {
        if (procedimento.setorPorUnidade().containsKey(unidadeColetaId)) {
            return procedimento.setorPorUnidade().get(unidadeColetaId);
        }
        if (procedimento.setorExclusivoId() != null) {
            return procedimento.setorExclusivoId();
        }
        return setorQueProcessaRotina == null ? procedimento.setorPadraoId() : setorQueProcessaRotina;
    }

}