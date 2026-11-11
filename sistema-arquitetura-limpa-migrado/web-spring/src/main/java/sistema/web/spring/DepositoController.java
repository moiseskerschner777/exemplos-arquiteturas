package sistema.web.spring;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import sistema.core.porta.in.DepositarPortIn;

@RestController
public class DepositoController {

    private final DepositarPortIn depositar;

    public DepositoController(DepositarPortIn depositar) {
        this.depositar = depositar;
    }

    @PostMapping("/api/contas/{id}/depositos")
    public MensagemDtoResponse depositar(@PathVariable String id, @RequestBody MovimentoDtoRequest pedido) {
        if (pedido.valor() == null) {
            throw new ValorInvalidoException();
        }
        depositar.executar(id, pedido.valor(), pedido.descricao());
        return new MensagemDtoResponse("operacao realizada");
    }

}