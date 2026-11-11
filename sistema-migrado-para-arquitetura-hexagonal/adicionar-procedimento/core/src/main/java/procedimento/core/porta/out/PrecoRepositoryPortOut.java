package procedimento.core.porta.out;

import java.util.List;

import procedimento.core.dominio.ComponentePreco;

public interface PrecoRepositoryPortOut {

    List<ComponentePreco> buscarComponentes(String tabelaPrecoId, String procedimentoId, long fontePagadoraId);

}