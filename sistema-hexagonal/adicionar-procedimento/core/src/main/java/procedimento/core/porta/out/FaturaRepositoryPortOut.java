package procedimento.core.porta.out;

import java.util.List;
import java.util.Optional;

public interface FaturaRepositoryPortOut {

    List<String> buscarSituacoesDeFatura(long atendimentoId, long fontePagadoraId);

    boolean existeCobrancaSemFatura(long atendimentoId, long fontePagadoraId);

    Optional<Long> buscarFontePagadoraComplementar(long atendimentoId);

}