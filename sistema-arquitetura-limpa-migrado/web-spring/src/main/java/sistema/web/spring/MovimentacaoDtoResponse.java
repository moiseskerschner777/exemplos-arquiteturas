package sistema.web.spring;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MovimentacaoDtoResponse(LocalDateTime dataHora, String tipo, BigDecimal valor, String descricao) {

}