package sistema.adaptador.jpa;

import java.util.function.Supplier;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import sistema.core.porta.out.TransacaoPortOut;

public class TransacaoRepositorySpring implements TransacaoPortOut {

    private final TransactionTemplate template;

    public TransacaoRepositorySpring(PlatformTransactionManager gerenciador) {
        this.template = new TransactionTemplate(gerenciador);
    }

    @Override
    public <T> T executar(Supplier<T> operacao) {
        return template.execute(status -> operacao.get());
    }

}