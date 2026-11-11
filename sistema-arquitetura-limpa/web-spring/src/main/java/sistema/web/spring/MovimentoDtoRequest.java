package sistema.web.spring;

import java.math.BigDecimal;

public record MovimentoDtoRequest(BigDecimal valor, String descricao) {

}