package sistema.web.spring;

import java.math.BigDecimal;

public record TransferenciaDtoRequest(String origem, String destino, BigDecimal valor, String descricao) {

}