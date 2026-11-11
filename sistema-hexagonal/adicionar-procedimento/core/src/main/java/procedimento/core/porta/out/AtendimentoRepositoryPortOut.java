package procedimento.core.porta.out;

import java.util.Optional;

import procedimento.core.dominio.Atendimento;

public interface AtendimentoRepositoryPortOut {

    Optional<Atendimento> buscar(long atendimentoId);

}