package procedimento.adaptador.postgres;

import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import procedimento.core.dominio.Material;
import procedimento.core.porta.out.MaterialRepositoryPortOut;

public class MaterialRepositoryPostgres implements MaterialRepositoryPortOut {

    private final Consulta consulta;

    public MaterialRepositoryPostgres(DataSource dataSource) {
        this.consulta = new Consulta(dataSource);
    }

    @Override
    public Optional<Material> buscar(String materialId) {
        return consulta.uma("select id, descricao, material_diversos, complemento_obrigatorio from material where id = ?",
                linha -> new Material(linha.getString("id"), linha.getString("descricao"), Consulta.ligado(linha, "material_diversos"),
                        Consulta.ligado(linha, "complemento_obrigatorio")),
                materialId);
    }

    @Override
    public List<String> buscarMateriaisPermitidos(String procedimentoId) {
        return consulta.lista("select material_id from procedimento_material_permitido where procedimento_id = ? order by id",
                linha -> linha.getString("material_id"), procedimentoId);
    }

}