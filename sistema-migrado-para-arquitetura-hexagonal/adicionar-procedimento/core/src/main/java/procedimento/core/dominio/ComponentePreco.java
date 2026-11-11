package procedimento.core.dominio;

import java.math.BigDecimal;

public record ComponentePreco(
        BigDecimal quantidade,
        BigDecimal porcentagem,
        boolean unidadeDefinidaNaFontePagadora,
        BigDecimal valorUnidadeTabela,
        BigDecimal valorUnidadeFontePagadora) {

    public BigDecimal valorUnidade() {
        return unidadeDefinidaNaFontePagadora ? valorUnidadeFontePagadora : valorUnidadeTabela;
    }

    public boolean definido() {
        return quantidade != null && porcentagem != null && valorUnidade() != null;
    }

}