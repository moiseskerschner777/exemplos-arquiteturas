package sistema.core.porta.in;

import java.math.BigDecimal;

public interface SacarPortIn {

    void executar(String contaId, BigDecimal valor, String descricao);

}