package sistema.core.porta.in;

import java.math.BigDecimal;

public interface DepositarPortIn {

    void executar(String contaId, BigDecimal valor, String descricao);

}