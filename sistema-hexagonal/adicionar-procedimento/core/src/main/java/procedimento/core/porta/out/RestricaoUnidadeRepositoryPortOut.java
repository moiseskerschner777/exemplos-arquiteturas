package procedimento.core.porta.out;

public interface RestricaoUnidadeRepositoryPortOut {

    boolean existeRestricao(String procedimentoId, String unidadeColetaId, String unidadeAtendimentoId);

}