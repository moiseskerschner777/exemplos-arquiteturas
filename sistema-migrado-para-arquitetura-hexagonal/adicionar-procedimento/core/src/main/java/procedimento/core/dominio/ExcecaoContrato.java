package procedimento.core.dominio;

public record ExcecaoContrato(
        boolean naoCoberto,
        String motivoNaoCobertura,
        boolean negociacaoEspecial,
        String autorizacao,
        String tabelaPrecoId) {

}