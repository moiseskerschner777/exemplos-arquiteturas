package sistema.web.spring;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import sistema.core.porta.in.SacarPortIn;

@RestController
public class SaqueController {

    private final SacarPortIn sacar;

    public SaqueController(SacarPortIn sacar) {
        this.sacar = sacar;
    }

    @PostMapping("/api/contas/{id}/saques")
    public MensagemDtoResponse sacar(@PathVariable String id, @RequestBody MovimentoDtoRequest pedido) {
        if (pedido.valor() == null) {
            throw new ValorInvalidoException();
        }
        sacar.executar(id, pedido.valor(), pedido.descricao());
        return new MensagemDtoResponse("operacao realizada");
    }

}