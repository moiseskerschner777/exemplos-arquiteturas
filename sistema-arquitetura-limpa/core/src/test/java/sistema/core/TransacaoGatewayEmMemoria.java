package sistema.core;

import java.util.function.Supplier;
import sistema.core.porta.out.TransacaoPortOut;

public class TransacaoGatewayEmMemoria implements TransacaoPortOut {

    @Override
    public <T> T executar(Supplier<T> operacao) {
        return operacao.get();
    }

}