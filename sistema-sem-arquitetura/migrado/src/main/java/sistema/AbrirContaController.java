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
@RequestMapping("/api/contas")
public class AbrirContaController {

    private final ContaRepository contas;

    public AbrirContaController(ContaRepository contas) {
        this.contas = contas;
    }

    public record Requisicao(String titular) {
    }

    @PostMapping
    @Transactional
    public ResponseEntity<Map<String, String>> abrir(@RequestBody Requisicao req) {
        if (req.titular() == null || req.titular().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(Map.of("erro", "titular obrigatorio"));
        }
        String id = UUID.randomUUID().toString();
        contas.save(new Conta(id, req.titular().trim(), BigDecimal.ZERO, "ATIVA"));
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", id));
    }
}
