package procedimento.core.casodeuso;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import procedimento.core.dominio.Atendimento;
import procedimento.core.dominio.CalculoDePrazo;
import procedimento.core.dominio.CalculoDePreco;
import procedimento.core.dominio.CategoriaAtendimento;
import procedimento.core.dominio.CobrancaDoDia;
import procedimento.core.dominio.ConfiguracaoPrazo;
import procedimento.core.dominio.DefinicaoDeSetor;
import procedimento.core.dominio.ExcecaoContrato;
import procedimento.core.dominio.FontePagadora;
import procedimento.core.dominio.Material;
import procedimento.core.dominio.Pendencia;
import procedimento.core.dominio.Procedimento;
import procedimento.core.dominio.Recipientes;
import procedimento.core.dominio.RedirecionamentoSetor;
import procedimento.core.dominio.Sexo;
import procedimento.core.dominio.TetoFaturamento;
import procedimento.core.dominio.UnidadeColeta;
import procedimento.core.erro.InclusaoRecusadaException;
import procedimento.core.erro.MotivoRecusa;
import procedimento.core.porta.in.AdicionarProcedimentoPortIn;
import procedimento.core.porta.in.dto.AdicionarProcedimentoDto;
import procedimento.core.porta.in.dto.ProcedimentoAdicionadoDto;

public class AdicionarProcedimento implements AdicionarProcedimentoPortIn {

    private static final List<String> SITUACOES_FATURA_FECHADA = List.of("F", "R", "E");
    private static final String AGRUPAMENTO_POR_DIA = "DIA";

    private final Repositorios repositorios;
    private final Clock relogio;

    public AdicionarProcedimento(Repositorios repositorios, Clock relogio) {
        this.repositorios = repositorios;
        this.relogio = relogio;
    }

    @Override
    public ProcedimentoAdicionadoDto executar(AdicionarProcedimentoDto comando) {
        Atendimento atendimento = repositorios.atendimentos().buscar(comando.atendimentoId())
                .orElseThrow(() -> new IllegalArgumentException("atendimento " + comando.atendimentoId() + " não encontrado"));
        FontePagadora fontePagadora = repositorios.fontesPagadoras().buscar(comando.fontePagadoraId())
                .orElseThrow(() -> new IllegalArgumentException("fonte pagadora " + comando.fontePagadoraId() + " não encontrada"));
        UnidadeColeta unidade = repositorios.unidadesColeta().buscar(atendimento.unidadeColetaId())
                .orElseThrow(() -> new IllegalArgumentException("unidade de coleta " + atendimento.unidadeColetaId() + " não encontrada"));
        Inclusao inclusao = new Inclusao(comando, atendimento, fontePagadora, unidade);

        identificarProcedimento(inclusao);
        validarAtendimentoEFatura(inclusao);
        validarClinicaECobertura(inclusao);
        montarExameSimples(inclusao);
        return validarLimitesERecipientes(inclusao);
    }

    private void identificarProcedimento(Inclusao inclusao) {
        AdicionarProcedimentoDto comando = inclusao.comando;
        Procedimento procedimento = repositorios.procedimentos().buscarPorMnemonicoOuId(comando.mnemonico())
                .orElseThrow(() -> recusa(MotivoRecusa.NAO_ENCONTRADO, comando.mnemonico()));
        inclusao.procedimento = procedimento;

        Sexo sexoPaciente = inclusao.atendimento.categoria() == CategoriaAtendimento.ANIMAL
                ? inclusao.atendimento.sexoAnimal()
                : Sexo.deCodigo(comando.sexoPaciente());
        if (!procedimento.sexo().compativelCom(sexoPaciente)) {
            throw recusa(MotivoRecusa.SEXO_INCOMPATIVEL, procedimento.mnemonico());
        }
        if (repositorios.restricoes().existeRestricao(procedimento.id(), inclusao.unidade.id(),
                inclusao.unidade.unidadeAtendimentoId())) {
            throw recusa(MotivoRecusa.RESTRITO_NA_UNIDADE, procedimento.mnemonico());
        }
        if (!procedimento.ativo() && !comando.ignorarValidacoes()) {
            throw recusa(MotivoRecusa.INATIVO, procedimento.mnemonico());
        }

        Optional<Material> materialDiversos = materialDiversosPendente(procedimento, comando);
        if (!procedimento.aceita(inclusao.atendimento.categoria())) {
            throw recusa(MotivoRecusa.CATEGORIA_INCOMPATIVEL, procedimento.mnemonico());
        }
        if (materialDiversos.isPresent()) {
            Material material = materialDiversos.get();
            throw new InclusaoRecusadaException(MotivoRecusa.MATERIAL_DIVERSOS_PENDENTE, "escolha o material",
                    material.id(), material.descricao(), repositorios.materiais().buscarMateriaisPermitidos(procedimento.id()));
        }
    }

    private Optional<Material> materialDiversosPendente(Procedimento procedimento, AdicionarProcedimentoDto comando) {
        if (procedimento.materialId() == null || (comando.materialDescricao() != null && !comando.materialDescricao().isBlank())) {
            return Optional.empty();
        }
        Optional<Material> informado = procedimento.microbiologia()
                ? Optional.ofNullable(comando.materialId()).flatMap(repositorios.materiais()::buscar)
                : Optional.empty();
        if (informado.isPresent()) {
            return informado.filter(Material::diversos);
        }
        return repositorios.materiais().buscar(procedimento.materialId()).filter(Material::diversos);
    }

    private void validarAtendimentoEFatura(Inclusao inclusao) {
        long atendimentoId = inclusao.atendimento.id();
        long fontePagadoraId = inclusao.fontePagadora.id();
        String procedimentoId = inclusao.procedimento.id();
        if (faturaNaoPermiteInclusao(atendimentoId, fontePagadoraId, inclusao.fontePagadora.faturaParcial())) {
            throw recusa(MotivoRecusa.FATURA_FECHADA, inclusao.fontePagadora.descricao());
        }
        Optional<Long> complementar = repositorios.faturas().buscarFontePagadoraComplementar(atendimentoId);
        if (complementar.isPresent()) {
            boolean parcial = repositorios.fontesPagadoras().buscar(complementar.get())
                    .map(FontePagadora::faturaParcial).orElse(false);
            if (faturaNaoPermiteInclusao(atendimentoId, complementar.get(), parcial)) {
                throw recusa(MotivoRecusa.FATURA_COMPLEMENTAR_FECHADA, String.valueOf(complementar.get()));
            }
        }
        boolean agrupaPorDia = repositorios.percentuaisCobranca().buscarAgrupamento(fontePagadoraId, procedimentoId)
                .filter(AGRUPAMENTO_POR_DIA::equals).isPresent();
        if (agrupaPorDia) {
            List<String> atendimentos = repositorios.percentuaisCobranca()
                    .buscarCobrancasDoDia(inclusao.atendimento.data(), fontePagadoraId, procedimentoId).stream()
                    .filter(cobranca -> SITUACOES_FATURA_FECHADA.contains(cobranca.situacaoFatura()))
                    .map(CobrancaDoDia::codigoAtendimento)
                    .distinct()
                    .toList();
            if (!atendimentos.isEmpty()) {
                throw recusa(MotivoRecusa.PERCENTUAL_COBRANCA, String.join("\n", atendimentos));
            }
        }
        if (!inclusao.comando.confirmouAgendamento() && inclusao.atendimento.temAgendamento()
                && repositorios.agendas().existeAgendaAtiva(procedimentoId)) {
            inclusao.pendencias.add(new Pendencia(MotivoRecusa.AGENDA_ESPECIFICA, "procedimento com agenda específica"));
        }
    }

    private boolean faturaNaoPermiteInclusao(long atendimentoId, long fontePagadoraId, boolean faturaParcial) {
        boolean faturaFechada = repositorios.faturas().buscarSituacoesDeFatura(atendimentoId, fontePagadoraId).stream()
                .anyMatch(SITUACOES_FATURA_FECHADA::contains);
        if (!faturaFechada) {
            return false;
        }
        return !(faturaParcial && repositorios.faturas().existeCobrancaSemFatura(atendimentoId, fontePagadoraId));
    }

    private void validarClinicaECobertura(Inclusao inclusao) {
        Procedimento procedimento = inclusao.procedimento;
        FontePagadora fontePagadora = inclusao.fontePagadora;
        inclusao.excecao = repositorios.contratos().buscarExcecao(fontePagadora.id(), procedimento.id()).orElse(null);

        validarIsolado(inclusao);
        if (!inclusao.comando.ignorarValidacoes() && !procedimento.ativo()) {
            throw recusa(MotivoRecusa.INATIVO_SEGUNDA_VERIFICACAO, procedimento.mnemonico());
        }
        if (procedimento.temTsa()) {
            boolean tsaAtivo = repositorios.procedimentos().buscar(procedimento.tsaId()).map(Procedimento::ativo).orElse(false);
            if (tsaAtivo && inclusao.atendimento.itemDoProcedimento(procedimento.tsaId()).isEmpty()) {
                inclusao.tsaPendente = procedimento.tsaId();
                inclusao.pendencias.add(new Pendencia(MotivoRecusa.ANTIBIOGRAMA_SEM_CULTURA, procedimento.tsaId()));
            }
        }
        if (procedimento.antibiograma() && procedimento.ativo() && !atendimentoTemCulturaDoAntibiograma(inclusao)) {
            throw recusa(MotivoRecusa.CULTURA_SEM_ANTIBIOGRAMA, procedimento.mnemonico());
        }

        CalculoDePreco.Resultado cobertura = calcularPreco(fontePagadora, inclusao.excecao, procedimento.id());
        if (cobertura.situacao() == CalculoDePreco.Situacao.SEM_COBERTURA) {
            String motivo = inclusao.excecao == null ? "" : inclusao.excecao.motivoNaoCobertura();
            throw recusa(MotivoRecusa.SEM_COBERTURA, "\nFonte pagadora: " + fontePagadora.descricao() + " - " + fontePagadora.apelido()
                    + "\n\n - " + procedimento.mnemonico() + " - " + procedimento.descricao() + "\n   Motivo: " + motivo);
        }
        if (fontePagadora.exigeCodigoAmb()) {
            String tabela = CalculoDePreco.tabelaDePreco(fontePagadora, inclusao.excecao);
            boolean semCodigo = repositorios.autorizacoes().buscarCodigoAmb(procedimento.id(), tabela)
                    .filter(codigo -> !codigo.isBlank()).isEmpty();
            if (semCodigo) {
                throw recusa(MotivoRecusa.CODIGO_AMB_NAO_DEFINIDO, procedimento.mnemonico());
            }
        }
        Optional.ofNullable(inclusao.excecao).map(ExcecaoContrato::autorizacao).filter(frase -> !frase.isBlank()).ifPresent(frase ->
                inclusao.fraseAutorizacao = "\n - " + procedimento.mnemonico() + " - " + procedimento.descricao() + ": " + frase);
        if (procedimento.alertaCadastro() != null && !procedimento.alertaCadastro().isBlank()) {
            inclusao.alertaCadastro = "Alerta(s) cadastro:\n\n - " + procedimento.mnemonico() + ": " + procedimento.alertaCadastro();
        }
    }

    private void validarIsolado(Inclusao inclusao) {
        Procedimento procedimento = inclusao.procedimento;
        boolean culturaComIsolado = procedimento.microbiologia() && !Integer.valueOf(2).equals(procedimento.tipoMicrobiologia())
                && !procedimento.temTsa();
        if (!culturaComIsolado) {
            return;
        }
        Optional<String> isolado = repositorios.contratos().buscarIsolado(inclusao.fontePagadora.id(), procedimento.id())
                .or(() -> Optional.ofNullable(inclusao.fontePagadora.isoladoPadraoId()));
        if (isolado.isPresent()) {
            ExcecaoContrato excecaoIsolado = repositorios.contratos().buscarExcecao(inclusao.fontePagadora.id(), isolado.get())
                    .orElse(null);
            CalculoDePreco.Resultado preco = calcularPreco(inclusao.fontePagadora, excecaoIsolado, isolado.get());
            if (preco.situacao() == CalculoDePreco.Situacao.SEM_DEFINICAO) {
                throw recusa(MotivoRecusa.ISOLADO_SEM_PRECO, isolado.get());
            }
        }
    }

    private boolean atendimentoTemCulturaDoAntibiograma(Inclusao inclusao) {
        return inclusao.atendimento.itens().stream()
                .map(item -> repositorios.procedimentos().buscar(item.procedimentoId()))
                .flatMap(Optional::stream)
                .anyMatch(exame -> inclusao.procedimento.id().equals(exame.tsaId()));
    }

    private void montarExameSimples(Inclusao inclusao) {
        Procedimento procedimento = inclusao.procedimento;
        AdicionarProcedimentoDto comando = inclusao.comando;
        if (procedimento.perfil()) {
            throw recusa(MotivoRecusa.FORA_DO_RECORTE, "perfil de exames fica fora do recorte");
        }
        inclusao.atendimento.itemDoProcedimento(procedimento.id()).ifPresent(item -> {
            throw recusa(item.incluidoPorPerfil() ? MotivoRecusa.JA_INCLUIDO_POR_PERFIL : MotivoRecusa.JA_INCLUIDO,
                    procedimento.mnemonico());
        });

        LocalDateTime agora = LocalDateTime.now(relogio);
        ConfiguracaoPrazo rotina = Optional.ofNullable(procedimento.rotinaPrazoId())
                .flatMap(repositorios.prazos()::buscarRotina)
                .orElse(null);
        inclusao.entrega = CalculoDePrazo.calcular(procedimento, rotina, agora)
                .orElseThrow(() -> recusa(MotivoRecusa.ERRO_PRAZO, "procedimento " + procedimento.mnemonico() + " sem prazo configurado"));

        CalculoDePreco.Resultado preco = calcularPreco(inclusao.fontePagadora, inclusao.excecao, procedimento.id());
        switch (preco.situacao()) {
            case NEGOCIACAO_ESPECIAL -> {
                inclusao.negociacaoEspecial = true;
                inclusao.valor = BigDecimal.ZERO;
            }
            case CALCULADO -> inclusao.valor = preco.valor();
            default -> throw recusa(MotivoRecusa.SEM_PRECO, procedimento.mnemonico());
        }

        definirMaterial(inclusao, procedimento, comando);

        String setorQueProcessaRotina = Optional.ofNullable(procedimento.setorPadraoId())
                .flatMap(setor -> repositorios.setores().buscarSetorQueProcessaRotina(inclusao.unidade.unidadeAtendimentoId(), setor))
                .orElse(null);
        String setor = DefinicaoDeSetor.definir(procedimento, inclusao.unidade.id(), setorQueProcessaRotina);
        Optional<String> destino = Optional.ofNullable(setor)
                .flatMap(s -> RedirecionamentoSetor.destino(repositorios.setores().buscarRedirecionamentos(s, inclusao.unidade.id()), agora));
        inclusao.setor = destino.orElse(setor);
    }

    private void definirMaterial(Inclusao inclusao, Procedimento procedimento, AdicionarProcedimentoDto comando) {
        Optional<Material> informado = Optional.ofNullable(comando.materialId()).flatMap(repositorios.materiais()::buscar);
        Optional<Material> doProcedimento = Optional.ofNullable(procedimento.materialId()).flatMap(repositorios.materiais()::buscar);
        Material material = null;
        String descricao = "";
        if (procedimento.microbiologia() && informado.isPresent()) {
            material = informado.get();
            descricao = comando.materialDescricao();
        } else if (doProcedimento.isPresent()) {
            material = doProcedimento.get();
            descricao = material.descricao();
            if (material.diversos()) {
                descricao = comando.materialDescricao();
                material = informado.orElse(material);
            }
        } else if (informado.isPresent()) {
            material = informado.get();
            descricao = comando.materialDescricao();
        }
        if (material != null && material.complementoObrigatorio()) {
            String base = descricao == null || descricao.isBlank() ? material.descricao() : descricao;
            descricao = Material.descricaoComComplemento(base, comando.complementoMaterial());
            inclusao.materialComplementoObrigatorio = true;
        }
        inclusao.material = material;
        inclusao.materialDescricao = descricao == null ? "" : descricao;
    }

    private ProcedimentoAdicionadoDto validarLimitesERecipientes(Inclusao inclusao) {
        FontePagadora fontePagadora = inclusao.fontePagadora;
        BigDecimal valorTotal = inclusao.valor;
        BigDecimal tetoAtendimento = fontePagadora.valorTetoAtendimento();
        if (tetoAtendimento != null && tetoAtendimento.signum() > 0 && valorTotal.compareTo(tetoAtendimento) > 0) {
            inclusao.pendencias.add(new Pendencia(MotivoRecusa.TETO_ATENDIMENTO_EXCEDIDO,
                    "Atingido o valor máximo em uma única O.S. para o convênio " + fontePagadora.descricao()));
        }
        if (fontePagadora.usaControleTetoFaturamento() && fontePagadora.bloqueiaTetoFaturamentoAtingido()) {
            BigDecimal saldo = repositorios.tetosFaturamento()
                    .buscarPeriodo(fontePagadora.id(), LocalDateTime.now(relogio).toLocalDate())
                    .map(TetoFaturamento::saldoDisponivel)
                    .orElse(TetoFaturamento.SEM_PERIODO);
            if (valorTotal.compareTo(saldo) > 0) {
                inclusao.pendencias.add(new Pendencia(MotivoRecusa.TETO_FATURAMENTO_EXCEDIDO,
                        "O novo valor cobrado está acima do teto de faturamento da F.P. " + fontePagadora.descricao()));
            }
            if (saldo.compareTo(TetoFaturamento.EM_RECALCULO) == 0) {
                inclusao.pendencias.add(new Pendencia(MotivoRecusa.SALDO_EM_RECALCULO,
                        "Convênio " + fontePagadora.descricao() + " não pode ser utilizado. Recalculando o saldo do período."));
            }
        }

        Recipientes recipientes = Recipientes.montar(inclusao.procedimento, inclusao.unidade);
        if (recipientes.temProcedimentoRepetido()) {
            throw recusa(MotivoRecusa.REPETIDO_NO_RECIPIENTE, inclusao.procedimento.mnemonico());
        }

        Procedimento procedimento = inclusao.procedimento;
        return new ProcedimentoAdicionadoDto(
                procedimento.id(),
                procedimento.mnemonicoOuId(),
                procedimento.descricao(),
                fontePagadora.id(),
                inclusao.valor,
                inclusao.negociacaoEspecial,
                inclusao.entrega,
                inclusao.material == null ? null : inclusao.material.id(),
                inclusao.materialDescricao,
                inclusao.materialComplementoObrigatorio,
                inclusao.setor,
                recipientes.todos(),
                recipientes.amostraEncaminhada(),
                recipientes.naoPermitemAmostraEncaminhada(),
                inclusao.tsaPendente,
                inclusao.fraseAutorizacao,
                inclusao.alertaCadastro,
                List.copyOf(inclusao.pendencias));
    }

    private CalculoDePreco.Resultado calcularPreco(FontePagadora fontePagadora, ExcecaoContrato excecao,
                                                   String procedimentoId) {
        String tabela = CalculoDePreco.tabelaDePreco(fontePagadora, excecao);
        return CalculoDePreco.calcular(fontePagadora, excecao,
                tabela == null ? List.of() : repositorios.precos().buscarComponentes(tabela, procedimentoId, fontePagadora.id()));
    }

    private static InclusaoRecusadaException recusa(MotivoRecusa motivo, String mensagem) {
        return new InclusaoRecusadaException(motivo, mensagem);
    }

    private static final class Inclusao {

        private final AdicionarProcedimentoDto comando;
        private final Atendimento atendimento;
        private final FontePagadora fontePagadora;
        private final UnidadeColeta unidade;
        private final List<Pendencia> pendencias = new ArrayList<>();
        private Procedimento procedimento;
        private ExcecaoContrato excecao;
        private String tsaPendente;
        private String fraseAutorizacao;
        private String alertaCadastro;
        private LocalDateTime entrega;
        private BigDecimal valor = BigDecimal.ZERO;
        private boolean negociacaoEspecial;
        private Material material;
        private String materialDescricao;
        private boolean materialComplementoObrigatorio;
        private String setor;

        private Inclusao(AdicionarProcedimentoDto comando, Atendimento atendimento, FontePagadora fontePagadora,
                         UnidadeColeta unidade) {
            this.comando = comando;
            this.atendimento = atendimento;
            this.fontePagadora = fontePagadora;
            this.unidade = unidade;
        }

    }

}