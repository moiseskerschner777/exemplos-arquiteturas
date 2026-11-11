package sistema.adaptador.jdbc;

import java.util.UUID;
import sistema.core.porta.out.GeradorDeIdPortOut;

public class GeradorDeIdUuid implements GeradorDeIdPortOut {

    @Override
    public String novoIdDeConta() {
        return UUID.randomUUID().toString();
    }

}