package procedimento.core.porta.out;

import java.util.Optional;

import procedimento.core.dominio.FontePagadora;

public interface FontePagadoraRepositoryPortOut {

    Optional<FontePagadora> buscar(long fontePagadoraId);

}