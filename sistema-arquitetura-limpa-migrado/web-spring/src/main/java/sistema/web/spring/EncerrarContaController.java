package sistema.web.spring;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import sistema.core.porta.in.EncerrarContaPortIn;

@RestController
public class EncerrarContaController {

    private final EncerrarContaPortIn encerrarConta;

    public EncerrarContaController(EncerrarContaPortIn encerrarConta) {
        this.encerrarConta = encerrarConta;
    }

    @PostMapping("/api/contas/{id}/encerramento")
    public MensagemDtoResponse encerrar(@PathVariable String id) {
        encerrarConta.executar(id);
        return new MensagemDtoResponse("conta encerrada");
    }

}