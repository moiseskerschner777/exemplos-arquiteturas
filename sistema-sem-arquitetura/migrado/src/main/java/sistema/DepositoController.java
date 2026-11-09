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
@RequestMapping("/api/contas/{id}/depositos")
public class DepositoController {

    private final ContaRepository contas;
    private final MovimentacaoRepository movimentacoes;

    public DepositoController(ContaRepository contas, MovimentacaoRepository movimentacoes) {
        this.contas = contas;
        this.movimentacoes = movimentacoes;
    }

    public record Requisicao(BigDecimal valor, String descricao) {
    }

    @PostMapping
    @Transactional
    public ResponseEntity<Map<String, String>> executar(@PathVariable String id, @RequestBody Requisicao req) {
        if (req.valor() == null || req.valor().compareTo(BigDecimal.ZERO) <= 0) {
            return erro(HttpStatus.UNPROCESSABLE_ENTITY, "valor deve ser maior que zero");
        }
        Conta conta = contas.buscarParaAtualizar(id).orElse(null);
        if (conta == null) {
            return erro(HttpStatus.NOT_FOUND, "conta nao encontrada");
        }
        if ("ENCERRADA".equals(conta.getSituacao())) {
            return erro(HttpStatus.UNPROCESSABLE_ENTITY, "conta encerrada");
        }
        conta.setSaldo(conta.getSaldo().add(req.valor()));
        contas.save(conta);
        movimentacoes.save(new Movimentacao(id, "DEPOSITO", req.valor(), req.descricao()));
        return ResponseEntity.ok(Map.of("mensagem", "operacao realizada"));
    }

    private ResponseEntity<Map<String, String>> erro(HttpStatus status, String mensagem) {
        return ResponseEntity.status(status).body(Map.of("erro", mensagem));
    }
}
