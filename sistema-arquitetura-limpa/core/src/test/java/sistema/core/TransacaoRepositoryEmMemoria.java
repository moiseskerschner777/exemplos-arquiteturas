package sistema.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import sistema.core.porta.out.TransacaoPortOut;

public class TransacaoRepositoryEmMemoria implements TransacaoPortOut {

    private final List<Restauravel> participantes;

    public TransacaoRepositoryEmMemoria(Restauravel... participantes) {
        this.participantes = Arrays.asList(participantes);
    }

    @Override
    public <T> T executar(Supplier<T> operacao) {
        List<Runnable> restauracoes = new ArrayList<Runnable>();
        for (Restauravel participante : participantes) {
            restauracoes.add(participante.pontoDeRestauracao());
        }
        try {
            return operacao.get();
        } catch (RuntimeException e) {
            for (Runnable restauracao : restauracoes) {
                restauracao.run();
            }
            throw e;
        }
    }

}