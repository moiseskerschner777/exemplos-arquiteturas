package procedimento.core.porta.out;

import java.util.Optional;

import procedimento.core.dominio.UnidadeColeta;

public interface UnidadeColetaRepositoryPortOut {

    Optional<UnidadeColeta> buscar(String unidadeColetaId);

}