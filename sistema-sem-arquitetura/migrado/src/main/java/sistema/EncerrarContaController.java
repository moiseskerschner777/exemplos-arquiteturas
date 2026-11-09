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
@RequestMapping("/api/contas/{id}/encerramento")
public class EncerrarContaController {

    private final ContaRepository contas;

    public EncerrarContaController(ContaRepository contas) {
        this.contas = contas;
    }

    @PostMapping
    @Transactional
    public ResponseEntity<Map<String, String>> encerrar(@PathVariable String id) {
        Conta conta = contas.buscarParaAtualizar(id).orElse(null);
        if (conta == null) {
            return erro(HttpStatus.NOT_FOUND, "conta nao encontrada");
        }
        if ("ENCERRADA".equals(conta.getSituacao())) {
            return erro(HttpStatus.UNPROCESSABLE_ENTITY, "conta ja encerrada");
        }
        if (conta.getSaldo().compareTo(BigDecimal.ZERO) != 0) {
            return erro(HttpStatus.UNPROCESSABLE_ENTITY, "conta com saldo nao pode ser encerrada");
        }
        conta.setSituacao("ENCERRADA");
        contas.save(conta);
        return ResponseEntity.ok(Map.of("mensagem", "conta encerrada"));
    }

    private ResponseEntity<Map<String, String>> erro(HttpStatus status, String mensagem) {
        return ResponseEntity.status(status).body(Map.of("erro", mensagem));
    }
}
