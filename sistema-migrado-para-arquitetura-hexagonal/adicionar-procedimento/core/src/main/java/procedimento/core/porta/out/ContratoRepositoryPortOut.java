package procedimento.core.porta.out;

import java.util.Optional;

import procedimento.core.dominio.ExcecaoContrato;

public interface ContratoRepositoryPortOut {

    Optional<ExcecaoContrato> buscarExcecao(long fontePagadoraId, String procedimentoId);

    Optional<String> buscarIsolado(long fontePagadoraId, String procedimentoId);

}