package procedimento.core.porta.out;

import java.util.Optional;

public interface AutorizacaoRepositoryPortOut {

    Optional<String> buscarCodigoAmb(String procedimentoId, String tabelaPrecoId);

}