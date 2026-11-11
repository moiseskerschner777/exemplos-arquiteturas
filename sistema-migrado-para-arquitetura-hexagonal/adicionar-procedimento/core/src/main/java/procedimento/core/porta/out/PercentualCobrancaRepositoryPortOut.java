package procedimento.core.porta.out;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import procedimento.core.dominio.CobrancaDoDia;

public interface PercentualCobrancaRepositoryPortOut {

    Optional<String> buscarAgrupamento(long fontePagadoraId, String procedimentoId);

    List<CobrancaDoDia> buscarCobrancasDoDia(LocalDate data, long fontePagadoraId, String procedimentoId);

}