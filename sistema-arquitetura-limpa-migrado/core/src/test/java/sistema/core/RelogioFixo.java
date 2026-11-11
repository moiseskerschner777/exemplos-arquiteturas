package sistema.core;

import java.time.LocalDateTime;
import sistema.core.porta.out.RelogioPortOut;

public class RelogioFixo implements RelogioPortOut {

    private final LocalDateTime instante;

    public RelogioFixo(LocalDateTime instante) {
        this.instante = instante;
    }

    @Override
    public LocalDateTime agora() {
        return instante;
    }

}