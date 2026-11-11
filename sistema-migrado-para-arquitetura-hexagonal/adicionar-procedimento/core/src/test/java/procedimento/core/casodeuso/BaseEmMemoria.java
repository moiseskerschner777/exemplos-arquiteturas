package procedimento.core.casodeuso;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import procedimento.core.dominio.Atendimento;
import procedimento.core.dominio.CategoriaAtendimento;
import procedimento.core.dominio.CobrancaDoDia;
import procedimento.core.dominio.ComponentePreco;
import procedimento.core.dominio.ExcecaoContrato;
import procedimento.core.dominio.Feriado;
import procedimento.core.dominio.FontePagadora;
import procedimento.core.dominio.ItemAtendimento;
import procedimento.core.dominio.ListaPosicional;
import procedimento.core.dominio.Material;
import procedimento.core.dominio.ParametrosDePrazo;
import procedimento.core.dominio.PrazoDaUnidade;
import procedimento.core.dominio.PrazoProcedimento;
import procedimento.core.dominio.Procedimento;
import procedimento.core.dominio.RedirecionamentoSetor;
import procedimento.core.dominio.RotinaPrazo;
import procedimento.core.dominio.Sexo;
import procedimento.core.dominio.TetoFaturamento;
import procedimento.core.dominio.TipoProcedimento;
import procedimento.core.dominio.UnidadeColeta;
import procedimento.core.porta.out.ContratoRepositoryPortOut;
import procedimento.core.porta.out.FaturaRepositoryPortOut;
import procedimento.core.porta.out.MaterialRepositoryPortOut;
import procedimento.core.porta.out.PercentualCobrancaRepositoryPortOut;
import procedimento.core.porta.out.PrazoRepositoryPortOut;
import procedimento.core.porta.out.ProcedimentoRepositoryPortOut;
import procedimento.core.porta.out.SetorRepositoryPortOut;

class BaseEmMemoria {

    final Map<String, Procedimento> procedimentos = new HashMap<>();
    final Map<Long, Atendimento> atendimentos = new HashMap<>();
    final Map<String, UnidadeColeta> unidades = new HashMap<>();
    final Set<String> restricoes = new HashSet<>();
    final Map<String, Material> materiais = new HashMap<>();
    final Map<String, List<String>> materiaisPermitidos = new HashMap<>();
    final Map<Long, FontePagadora> fontes = new HashMap<>();
    final Map<String, List<String>> situacoesFatura = new HashMap<>();
    final Map<Long, Long> complementares = new HashMap<>();
    final Map<String, String> agrupamentos = new HashMap<>();
    final Map<String, List<CobrancaDoDia>> cobrancasDoDia = new HashMap<>();
    final Set<String> agendasAtivas = new HashSet<>();
    final Map<String, ExcecaoContrato> excecoes = new HashMap<>();
    final Map<String, String> isolados = new HashMap<>();
    final Map<String, String> codigosAmb = new HashMap<>();
    final Map<String, List<ComponentePreco>> componentes = new HashMap<>();
    final Map<Long, TetoFaturamento> tetos = new HashMap<>();

    static BaseEmMemoria comBaseDosCenarios() {
        BaseEmMemoria base = new BaseEmMemoria();
        base.procedimentos.put("34", procedimento("34", "TGO", "Transaminase glutamico oxalacetica (AST/TGO)", "00003", true));
        base.procedimentos.put("1", procedimento("1", "HEM", "Hemograma completo", "00002", true));
        base.procedimentos.put("3", procedimento("3", "HGB", "Dosagem de hemoglobina", "00002", false));
        base.procedimentos.put("7", procedimento("7", "RETIC", "Reticulocitos", "00002", false));
        base.atendimentos.put(15L, new Atendimento(15, LocalDate.of(2024, 12, 23), CategoriaAtendimento.HUMANO, null, "2", null,
                List.of(new ItemAtendimento("1", null, 2L), new ItemAtendimento("7", null, 2L))));
        base.unidades.put("2", new UnidadeColeta("2", "1", 1));
        base.materiais.put("00002", new Material("00002", "Sangue total (EDTA)", false, false));
        base.materiais.put("00003", new Material("00003", "Soro", false, false));
        base.fontes.put(1L, fonte(1, "Particular", "B", "1"));
        base.fontes.put(2L, fonte(2, "Cortesia", "C", null));
        base.componentes.put("1|34", List.of(new ComponentePreco(BigDecimal.valueOf(50), BigDecimal.valueOf(100), true, null, BigDecimal.ONE)));
        base.codigosAmb.put("1|34", "40302504");
        return base;
    }

    static final PrazoProcedimento PRAZO_PADRAO = new PrazoProcedimento("1", "28800", 3, 1, false, ListaPosicional.vazia(),
            ListaPosicional.vazia(), ListaPosicional.vazia(), ListaPosicional.vazia(), ListaPosicional.vazia(), ListaPosicional.vazia());

    static Procedimento procedimento(String id, String mnemonico, String descricao, String materialId, boolean ativo) {
        return new ProcedimentoTeste(id, mnemonico, descricao, materialId, ativo).criar();
    }

    static final class ProcedimentoTeste {

        String id;
        String mnemonico;
        String descricao;
        Sexo sexo = Sexo.AMBOS;
        Integer tipo = 2;
        boolean ativo;
        String materialId;
        boolean microbiologia;
        String tsaId;
        boolean antibiograma;
        String alertaCadastro;

        ProcedimentoTeste(String id, String mnemonico, String descricao, String materialId, boolean ativo) {
            this.id = id;
            this.mnemonico = mnemonico;
            this.descricao = descricao;
            this.materialId = materialId;
            this.ativo = ativo;
        }

        Procedimento criar() {
            return new Procedimento(id, mnemonico, descricao, sexo, TipoProcedimento.deCodigo(tipo), ativo, materialId, microbiologia,
                    null, tsaId, antibiograma, alertaCadastro, 1, false, "003", null, Map.of(), 1, PRAZO_PADRAO, List.of("4"), Map.of());
        }

    }

    void alterarTgo(java.util.function.Consumer<ProcedimentoTeste> alteracao) {
        ProcedimentoTeste tgo = new ProcedimentoTeste("34", "TGO", "Transaminase glutamico oxalacetica (AST/TGO)", "00003",
                procedimentos.get("34").ativo());
        Procedimento atual = procedimentos.get("34");
        tgo.sexo = atual.sexo();
        tgo.tsaId = atual.tsaId();
        tgo.antibiograma = atual.antibiograma();
        tgo.alertaCadastro = atual.alertaCadastro();
        tgo.microbiologia = atual.microbiologia();
        tgo.tipo = atual.tipo().map(t -> t.ordinal() + 1).orElse(null);
        alteracao.accept(tgo);
        procedimentos.put("34", tgo.criar());
    }

    static FontePagadora fonte(long id, String descricao, String tipo, String tabela) {
        return new FontePagadora(id, descricao, descricao, tipo, tabela, BigDecimal.ZERO, false, false, false, false, null, false, null);
    }

    Repositorios repositorios() {
        return new Repositorios(
                new ProcedimentoRepositoryPortOut() {
                    @Override
                    public Optional<Procedimento> buscarPorMnemonicoOuId(String mnemonicoOuId) {
                        return procedimentos.values().stream()
                                .filter(p -> p.mnemonico().equalsIgnoreCase(mnemonicoOuId))
                                .findFirst()
                                .or(() -> Optional.ofNullable(procedimentos.get(mnemonicoOuId)));
                    }

                    @Override
                    public Optional<Procedimento> buscar(String procedimentoId) {
                        return Optional.ofNullable(procedimentos.get(procedimentoId));
                    }
                },
                atendimentoId -> Optional.ofNullable(atendimentos.get(atendimentoId)),
                unidadeColetaId -> Optional.ofNullable(unidades.get(unidadeColetaId)),
                (procedimentoId, unidadeColetaId, unidadeAtendimentoId) -> restricoes.contains(procedimentoId + "|" + unidadeColetaId),
                new MaterialRepositoryPortOut() {
                    @Override
                    public Optional<Material> buscar(String materialId) {
                        return Optional.ofNullable(materiais.get(materialId));
                    }

                    @Override
                    public List<String> buscarMateriaisPermitidos(String procedimentoId) {
                        return materiaisPermitidos.getOrDefault(procedimentoId, List.of());
                    }
                },
                fontePagadoraId -> Optional.ofNullable(fontes.get(fontePagadoraId)),
                new FaturaRepositoryPortOut() {
                    @Override
                    public List<String> buscarSituacoesDeFatura(long atendimentoId, long fontePagadoraId) {
                        return situacoesFatura.getOrDefault(atendimentoId + "|" + fontePagadoraId, List.of());
                    }

                    @Override
                    public boolean existeCobrancaSemFatura(long atendimentoId, long fontePagadoraId) {
                        return false;
                    }

                    @Override
                    public Optional<Long> buscarFontePagadoraComplementar(long atendimentoId) {
                        return Optional.ofNullable(complementares.get(atendimentoId));
                    }
                },
                new PercentualCobrancaRepositoryPortOut() {
                    @Override
                    public Optional<String> buscarAgrupamento(long fontePagadoraId, String procedimentoId) {
                        return Optional.ofNullable(agrupamentos.get(fontePagadoraId + "|" + procedimentoId));
                    }

                    @Override
                    public List<CobrancaDoDia> buscarCobrancasDoDia(LocalDate data, long fontePagadoraId, String procedimentoId) {
                        return cobrancasDoDia.getOrDefault(fontePagadoraId + "|" + procedimentoId, List.of());
                    }
                },
                agendasAtivas::contains,
                new ContratoRepositoryPortOut() {
                    @Override
                    public Optional<ExcecaoContrato> buscarExcecao(long fontePagadoraId, String procedimentoId) {
                        return Optional.ofNullable(excecoes.get(fontePagadoraId + "|" + procedimentoId));
                    }

                    @Override
                    public Optional<String> buscarIsolado(long fontePagadoraId, String procedimentoId) {
                        return Optional.ofNullable(isolados.get(fontePagadoraId + "|" + procedimentoId));
                    }
                },
                (procedimentoId, tabelaPrecoId) -> Optional.ofNullable(codigosAmb.get(tabelaPrecoId + "|" + procedimentoId)),
                new PrazoRepositoryPortOut() {
                    @Override
                    public Optional<RotinaPrazo> buscarRotina(String rotinaPrazoId) {
                        return Optional.empty();
                    }

                    @Override
                    public Optional<PrazoDaUnidade> buscarPrazoDaUnidade(String unidadeColetaId) {
                        return Optional.of(new PrazoDaUnidade(unidadeColetaId, "Unidade " + unidadeColetaId, "1", "S",
                                ListaPosicional.vazia(), ListaPosicional.vazia(), null, null));
                    }

                    @Override
                    public ListaPosicional buscarDiasProducaoDoSetor(String setorId) {
                        return ListaPosicional.vazia();
                    }

                    @Override
                    public Optional<ParametrosDePrazo> buscarParametros() {
                        ListaPosicional diasUteis = new ListaPosicional(List.of("", "1", "2", "3", "4", "5", "6"));
                        return Optional.of(new ParametrosDePrazo(diasUteis, diasUteis, 46800, 61200));
                    }

                    @Override
                    public List<Feriado> buscarFeriados() {
                        return List.of();
                    }
                },
                (tabelaPrecoId, procedimentoId, fontePagadoraId) ->
                        new ArrayList<>(componentes.getOrDefault(tabelaPrecoId + "|" + procedimentoId, List.of())),
                new SetorRepositoryPortOut() {
                    @Override
                    public Optional<String> buscarSetorQueProcessaRotina(String unidadeAtendimentoId, String setorPadraoId) {
                        return Optional.empty();
                    }

                    @Override
                    public List<RedirecionamentoSetor> buscarRedirecionamentos(String setorId, String unidadeColetaId) {
                        return List.of();
                    }
                },
                (fontePagadoraId, data) -> Optional.ofNullable(tetos.get(fontePagadoraId)));
    }

}