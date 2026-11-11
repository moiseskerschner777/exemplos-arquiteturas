package procedimento.core.porta.out;

import java.util.List;
import java.util.Optional;

import procedimento.core.dominio.RedirecionamentoSetor;

public interface SetorRepositoryPortOut {

    Optional<String> buscarSetorQueProcessaRotina(String unidadeAtendimentoId, String setorPadraoId);

    List<RedirecionamentoSetor> buscarRedirecionamentos(String setorId, String unidadeColetaId);

}