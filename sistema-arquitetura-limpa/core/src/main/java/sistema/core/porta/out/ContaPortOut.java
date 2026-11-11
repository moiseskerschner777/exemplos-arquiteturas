package sistema.core.porta.out;

import java.util.Optional;
import sistema.core.dominio.Conta;

public interface ContaPortOut {

    Optional<Conta> buscarParaAtualizar(String id);

    void salvar(Conta conta);

}