package sistema;

import java.math.BigDecimal;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transferencias")
public class TransferenciaController {

    private final ContaRepository contas;
    private final MovimentacaoRepository movimentacoes;

    public TransferenciaController(ContaRepository contas, MovimentacaoRepository movimentacoes) {
        this.contas = contas;
        this.movimentacoes = movimentacoes;
    }

    public record TransferenciaRequest(String origem, String destino, BigDecimal valor, String descricao) {
    }

    @PostMapping
    @Transactional
    public ResponseEntity<Map<String, String>> transferir(@RequestBody TransferenciaRequest req) {
        if (req.valor() == null || req.valor().compareTo(BigDecimal.ZERO) <= 0) {
            return erro(HttpStatus.UNPROCESSABLE_ENTITY, "valor deve ser maior que zero");
        }
        if (req.origem() == null || req.origem().equals(req.destino())) {
            return erro(HttpStatus.UNPROCESSABLE_ENTITY, "origem deve ser diferente do destino");
        }
        Conta origem = contas.buscarParaAtualizar(req.origem()).orElse(null);
        if (origem == null) {
            return erro(HttpStatus.NOT_FOUND, "conta de origem nao encontrada");
        }
        Conta destino = contas.buscarParaAtualizar(req.destino()).orElse(null);
        if (destino == null) {
            return erro(HttpStatus.NOT_FOUND, "conta de destino nao encontrada");
        }
        if ("ENCERRADA".equals(origem.getSituacao()) || "ENCERRADA".equals(destino.getSituacao())) {
            return erro(HttpStatus.UNPROCESSABLE_ENTITY, "conta encerrada");
        }
        if (origem.getSaldo().compareTo(req.valor()) < 0) {
            return erro(HttpStatus.UNPROCESSABLE_ENTITY, "saldo insuficiente");
        }
        origem.setSaldo(origem.getSaldo().subtract(req.valor()));
        destino.setSaldo(destino.getSaldo().add(req.valor()));
        contas.save(origem);
        contas.save(destino);
        movimentacoes.save(new Movimentacao(origem.getId(), "TRANSFERENCIA_ENVIADA", req.valor(), req.descricao()));
        movimentacoes.save(new Movimentacao(destino.getId(), "TRANSFERENCIA_RECEBIDA", req.valor(), req.descricao()));
        return ResponseEntity.ok(Map.of("mensagem", "transferencia realizada"));
    }

    private ResponseEntity<Map<String, String>> erro(HttpStatus status, String mensagem) {
        return ResponseEntity.status(status).body(Map.of("erro", mensagem));
    }
}
