package procedimento.core.dominio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

public final class CalculoDePreco {

    public enum Situacao { CALCULADO, SEM_COBERTURA, NEGOCIACAO_ESPECIAL, SEM_DEFINICAO }

    public record Resultado(Situacao situacao, BigDecimal valor) {

    }

    private CalculoDePreco() {
    }

    /**
     * @param excecao exceção de contrato do procedimento; {@code null} quando não houver
     */
    public static Resultado calcular(FontePagadora fontePagadora, ExcecaoContrato excecao,
                                     List<ComponentePreco> componentes) {
        if (fontePagadora.cortesia()) {
            return new Resultado(Situacao.CALCULADO, BigDecimal.ZERO);
        }
        if (excecao != null && excecao.naoCoberto()) {
            return new Resultado(Situacao.SEM_COBERTURA, null);
        }
        if (excecao != null && excecao.negociacaoEspecial()) {
            return new Resultado(Situacao.NEGOCIACAO_ESPECIAL, BigDecimal.ZERO);
        }
        if (componentes.isEmpty() || componentes.stream().anyMatch(c -> !c.definido())) {
            return new Resultado(Situacao.SEM_DEFINICAO, null);
        }
        BigDecimal valor = componentes.stream()
                .map(c -> c.quantidade().multiply(c.valorUnidade()).multiply(c.porcentagem())
                        .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new Resultado(Situacao.CALCULADO, valor.setScale(2, RoundingMode.HALF_UP));
    }

    /**
     * @param excecao exceção de contrato do procedimento; {@code null} quando não houver
     */
    public static String tabelaDePreco(FontePagadora fontePagadora, ExcecaoContrato excecao) {
        return Optional.ofNullable(excecao).map(ExcecaoContrato::tabelaPrecoId).filter(id -> !id.isBlank())
                .orElse(fontePagadora.tabelaPrecoId());
    }

}