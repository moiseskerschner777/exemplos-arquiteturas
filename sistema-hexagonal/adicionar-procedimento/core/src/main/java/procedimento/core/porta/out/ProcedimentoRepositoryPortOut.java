package procedimento.core.porta.out;

import java.util.Optional;

import procedimento.core.dominio.Procedimento;

public interface ProcedimentoRepositoryPortOut {

    Optional<Procedimento> buscarPorMnemonicoOuId(String mnemonicoOuId);

    Optional<Procedimento> buscar(String procedimentoId);

}