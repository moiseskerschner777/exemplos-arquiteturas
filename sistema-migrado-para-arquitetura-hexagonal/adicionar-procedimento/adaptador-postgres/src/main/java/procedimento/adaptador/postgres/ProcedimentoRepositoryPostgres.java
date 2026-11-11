package procedimento.adaptador.postgres;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.sql.DataSource;

import procedimento.core.dominio.PrazoProcedimento;
import procedimento.core.dominio.Procedimento;
import procedimento.core.dominio.Sexo;
import procedimento.core.dominio.TipoProcedimento;
import procedimento.core.porta.out.ProcedimentoRepositoryPortOut;

public class ProcedimentoRepositoryPostgres implements ProcedimentoRepositoryPortOut {

    private static final String COLUNAS = "select * from procedimento ";

    private final Consulta consulta;

    public ProcedimentoRepositoryPostgres(DataSource dataSource) {
        this.consulta = new Consulta(dataSource);
    }

    @Override
    public Optional<Procedimento> buscarPorMnemonicoOuId(String mnemonicoOuId) {
        return consulta.uma(COLUNAS + "where upper(mnemonico) = upper(?) order by id", this::procedimento, mnemonicoOuId)
                .or(() -> buscar(mnemonicoOuId));
    }

    @Override
    public Optional<Procedimento> buscar(String procedimentoId) {
        return consulta.uma(COLUNAS + "where id = ?", this::procedimento, procedimentoId);
    }

    private Procedimento procedimento(ResultSet linha) throws SQLException {
        String id = linha.getString("id");
        String perfil = linha.getString("perfil");
        return new Procedimento(
                id,
                linha.getString("mnemonico"),
                linha.getString("descricao"),
                Sexo.deCodigo(linha.getString("sexo")),
                TipoProcedimento.deCodigo(Consulta.inteiro(linha, "tipo_exame")),
                Consulta.ligado(linha, "status"),
                linha.getString("material"),
                Consulta.ligado(linha, "utilizado_microbiologia"),
                Consulta.inteiro(linha, "tipo_microbiologia"),
                linha.getString("tsa"),
                Consulta.ligado(linha, "antibiograma"),
                linha.getString("alerta_cadastro"),
                Consulta.inteiro(linha, "segmento"),
                perfil != null && !perfil.isBlank(),
                linha.getString("setor_padrao"),
                linha.getString("setor_exclusivo"),
                pares(linha.getString("setor_codigo_posto"), linha.getString("setor_por_posto")),
                Consulta.inteiro(linha, "amostra_encaminhada"),
                prazo(linha),
                recipientes(id, linha.getString("recipiente")),
                recipientesPorUnidade(linha.getString("recipiente_unidade_unidade"), linha.getString("recipiente_unidade_recipiente")));
    }

    private static PrazoProcedimento prazo(ResultSet linha) throws SQLException {
        return new PrazoProcedimento(
                linha.getString("prazo_codigo_rotina"),
                linha.getString("prazo_minimo"),
                Consulta.inteiro(linha, "prazo_minimo_tipo"),
                Consulta.inteiro(linha, "prazo_rotina_tipo"),
                Consulta.ligado(linha, "prazo_rotina_utiliza_rotina"),
                Consulta.listaPosicional(linha.getString("prazo_codigo_posto")),
                Consulta.listaPosicional(linha.getString("prazo_codigo_rotina_posto")),
                Consulta.listaPosicional(linha.getString("prazo_minimo_posto")),
                Consulta.listaPosicional(linha.getString("prazo_minimo_posto_tipo")),
                Consulta.listaPosicional(linha.getString("prazo_rotina_posto_tipo")),
                Consulta.listaPosicional(linha.getString("prazo_rotina_posto_utiliza_rotina")));
    }

    private List<String> recipientes(String procedimentoId, String recipientePadrao) {
        List<String> lista = consulta.lista("select recipiente from procedimento_recipiente where tbl_exame = ? order by element_key",
                linha -> linha.getString("recipiente"), procedimentoId);
        if (!lista.isEmpty()) {
            return lista;
        }
        return recipientePadrao == null || recipientePadrao.isBlank() ? List.of() : List.of(recipientePadrao);
    }

    private static Map<String, String> pares(String chaves, String valores) {
        List<String> listaChaves = Consulta.listaSeparada(chaves);
        List<String> listaValores = Consulta.listaSeparada(valores);
        Map<String, String> pares = new LinkedHashMap<>();
        for (int i = 0; i < Math.min(listaChaves.size(), listaValores.size()); i++) {
            pares.put(listaChaves.get(i), listaValores.get(i));
        }
        return pares;
    }

    private static Map<String, List<String>> recipientesPorUnidade(String unidades, String recipientes) {
        List<String> listaUnidades = Consulta.listaSeparada(unidades);
        List<String> listaRecipientes = Consulta.listaSeparada(recipientes);
        Map<String, List<String>> porUnidade = new LinkedHashMap<>();
        for (int i = 0; i < Math.min(listaUnidades.size(), listaRecipientes.size()); i++) {
            porUnidade.computeIfAbsent(listaUnidades.get(i), unidade -> new java.util.ArrayList<>()).add(listaRecipientes.get(i));
        }
        return porUnidade;
    }

}