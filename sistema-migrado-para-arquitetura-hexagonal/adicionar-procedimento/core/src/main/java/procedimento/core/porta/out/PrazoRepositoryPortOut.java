package procedimento.core.porta.out;

import java.util.List;
import java.util.Optional;

import procedimento.core.dominio.Feriado;
import procedimento.core.dominio.ListaPosicional;
import procedimento.core.dominio.ParametrosDePrazo;
import procedimento.core.dominio.PrazoDaUnidade;
import procedimento.core.dominio.RotinaPrazo;

public interface PrazoRepositoryPortOut {

    Optional<RotinaPrazo> buscarRotina(String rotinaPrazoId);

    Optional<PrazoDaUnidade> buscarPrazoDaUnidade(String unidadeColetaId);

    ListaPosicional buscarDiasProducaoDoSetor(String setorId);

    Optional<ParametrosDePrazo> buscarParametros();

    List<Feriado> buscarFeriados();

}