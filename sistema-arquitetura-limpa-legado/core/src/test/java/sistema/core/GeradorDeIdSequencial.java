package sistema.core;

import java.util.concurrent.atomic.AtomicInteger;
import sistema.core.porta.out.GeradorDeIdPortOut;

public class GeradorDeIdSequencial implements GeradorDeIdPortOut {

    private final AtomicInteger proximo = new AtomicInteger(1);

    @Override
    public String novoIdDeConta() {
        return "conta-" + proximo.getAndIncrement();
    }

}