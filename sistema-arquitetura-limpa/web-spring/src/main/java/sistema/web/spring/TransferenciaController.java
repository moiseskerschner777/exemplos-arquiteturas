package sistema.web.spring;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import sistema.core.porta.in.TransferirPortIn;

@RestController
public class TransferenciaController {

    private final TransferirPortIn transferir;

    public TransferenciaController(TransferirPortIn transferir) {
        this.transferir = transferir;
    }

    @PostMapping("/api/transferencias")
    public MensagemDtoResponse transferir(@RequestBody TransferenciaDtoRequest pedido) {
        if (pedido.valor() == null) {
            throw new ValorInvalidoException();
        }
        transferir.executar(pedido.origem(), pedido.destino(), pedido.valor(), pedido.descricao());
        return new MensagemDtoResponse("transferencia realizada");
    }

}