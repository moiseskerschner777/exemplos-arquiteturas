package procedimento.core.dominio;

import java.math.BigDecimal;
import java.util.Objects;

public record TetoFaturamento(boolean calculandoSaldo, BigDecimal valorTeto, BigDecimal saldoUtilizado) {

    public static final BigDecimal SEM_PERIODO = BigDecimal.valueOf(-1);
    public static final BigDecimal EM_RECALCULO = BigDecimal.valueOf(-2);

    public BigDecimal saldoDisponivel() {
        if (calculandoSaldo) {
            return EM_RECALCULO;
        }
        return valorTeto.subtract(Objects.requireNonNullElse(saldoUtilizado, BigDecimal.ZERO));
    }

}