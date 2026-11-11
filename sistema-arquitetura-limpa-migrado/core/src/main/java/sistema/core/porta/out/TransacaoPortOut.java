package sistema.core.porta.out;

import java.util.function.Supplier;

public interface TransacaoPortOut {

    <T> T executar(Supplier<T> operacao);

}