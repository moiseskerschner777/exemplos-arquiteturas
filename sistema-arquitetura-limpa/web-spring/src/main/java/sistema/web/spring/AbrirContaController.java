package sistema.web.spring;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import sistema.core.porta.in.AbrirContaPortIn;

@RestController
public class AbrirContaController {

    private final AbrirContaPortIn abrirConta;

    public AbrirContaController(AbrirContaPortIn abrirConta) {
        this.abrirConta = abrirConta;
    }

    @PostMapping("/api/contas")
    @ResponseStatus(HttpStatus.CREATED)
    public ContaAbertaDtoResponse abrir(@RequestBody AberturaDeContaDtoRequest pedido) {
        String id = abrirConta.executar(pedido.titular());
        return new ContaAbertaDtoResponse(id, "conta aberta: " + id);
    }

}