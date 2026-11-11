package sistema.core.porta.in;

import java.math.BigDecimal;

public interface TransferirPortIn {

    void executar(String origemId, String destinoId, BigDecimal valor, String descricao);

}