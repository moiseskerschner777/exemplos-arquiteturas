package procedimento.core.porta.out;

import java.util.List;
import java.util.Optional;

import procedimento.core.dominio.Material;

public interface MaterialRepositoryPortOut {

    Optional<Material> buscar(String materialId);

    List<String> buscarMateriaisPermitidos(String procedimentoId);

}