package procedimento.core.porta.out;

import java.time.LocalDate;
import java.util.Optional;

import procedimento.core.dominio.TetoFaturamento;

public interface TetoFaturamentoRepositoryPortOut {

    Optional<TetoFaturamento> buscarPeriodo(long fontePagadoraId, LocalDate data);

}