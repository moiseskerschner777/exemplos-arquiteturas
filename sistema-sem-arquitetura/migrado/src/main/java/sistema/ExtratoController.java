package sistema;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contas/{id}/extrato")
public class ExtratoController {

    private final MovimentacaoRepository movimentacoes;

    public ExtratoController(MovimentacaoRepository movimentacoes) {
        this.movimentacoes = movimentacoes;
    }

    public record Linha(Long id, String tipo, BigDecimal valor, String descricao) {
    }

    @GetMapping
    public ResponseEntity<?> consultar(@PathVariable String id, @RequestParam int pagina, @RequestParam int tamanho) {
        if (pagina < 0 || tamanho <= 0) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(Map.of("erro", "pagina e tamanho invalidos"));
        }
        List<Linha> linhas = movimentacoes.findByContaIdOrderByIdAsc(id, PageRequest.of(pagina, tamanho)).stream()
                .map(m -> new Linha(m.getId(), m.getTipo(), m.getValor(), m.getDescricao()))
                .toList();
        return ResponseEntity.ok(linhas);
    }
}
