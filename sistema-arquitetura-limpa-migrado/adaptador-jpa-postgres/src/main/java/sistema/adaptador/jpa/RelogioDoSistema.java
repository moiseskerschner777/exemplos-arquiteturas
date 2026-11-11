package sistema.adaptador.jpa;

import java.time.LocalDateTime;
import sistema.core.porta.out.RelogioPortOut;

public class RelogioDoSistema implements RelogioPortOut {

    @Override
    public LocalDateTime agora() {
        return LocalDateTime.now();
    }

}