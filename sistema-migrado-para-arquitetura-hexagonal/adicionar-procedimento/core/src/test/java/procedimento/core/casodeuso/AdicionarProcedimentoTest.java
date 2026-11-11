package procedimento.core.casodeuso;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import procedimento.core.dominio.Atendimento;
import procedimento.core.dominio.ComponentePreco;
import procedimento.core.dominio.ExcecaoContrato;
import procedimento.core.dominio.FontePagadora;
import procedimento.core.dominio.Material;
import procedimento.core.dominio.Pendencia;
import procedimento.core.dominio.Sexo;
import procedimento.core.dominio.TetoFaturamento;
import procedimento.core.dominio.UnidadeColeta;
import procedimento.core.erro.InclusaoRecusadaException;
import procedimento.core.erro.MotivoRecusa;
import procedimento.core.porta.in.dto.AdicionarProcedimentoDto;
import procedimento.core.porta.in.dto.ProcedimentoAdicionadoDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

class AdicionarProcedimentoTest {

    private static final Clock RELOGIO = Clock.fixed(Instant.parse("2026-11-11T10:00:00Z"), ZoneOffset.UTC);

    private BaseEmMemoria base;

    @BeforeEach
    void montarBase() {
        base = BaseEmMemoria.comBaseDosCenarios();
    }

    @Test
    void c00ExameSimplesAtivoComPrecoEPrazoEhAdicionado() {
        ProcedimentoAdicionadoDto item = executar(comando("TGO"));

        assertItemPadrao(item);
        assertThat(item.pendencias()).isEmpty();
    }

    @Test
    void c01MnemonicoInexistenteRecusaComoNaoEncontrado() {
        assertThat(recusa(comando("XXXX")).motivo()).isEqualTo(MotivoRecusa.NAO_ENCONTRADO);
    }

    @Test
    void c02aProcedimentoFemininoParaPacienteMasculinoRecusa() {
        base.alterarTgo(tgo -> tgo.sexo = Sexo.FEMININO);

        assertThat(recusa(comando("TGO")).motivo()).isEqualTo(MotivoRecusa.SEXO_INCOMPATIVEL);
    }

    @Test
    void c02bPacienteSemSexoRecusaComoSexoIncompativel() {
        base.alterarTgo(tgo -> tgo.sexo = Sexo.FEMININO);

        assertThat(recusa(comandoComSexo("TGO", "")).motivo()).isEqualTo(MotivoRecusa.SEXO_INCOMPATIVEL);
    }

    @Test
    void c03ProcedimentoRestritoNaUnidadeRecusa() {
        base.restricoes.add("34|2");

        assertThat(recusa(comando("TGO")).motivo()).isEqualTo(MotivoRecusa.RESTRITO_NA_UNIDADE);
    }

    @Test
    void c04aProcedimentoInativoRecusa() {
        base.alterarTgo(tgo -> tgo.ativo = false);

        assertThat(recusa(comando("TGO")).motivo()).isEqualTo(MotivoRecusa.INATIVO);
    }

    @Test
    void c04bIgnorarValidacoesAceitaProcedimentoInativo() {
        base.alterarTgo(tgo -> tgo.ativo = false);

        assertItemPadrao(executar(new AdicionarProcedimentoDto(15, "TGO", "M", 1, null, null, null, false, true)));
    }

    @Test
    void c05MaterialDiversosPedeOMaterial() {
        base.materiais.put("00003", new Material("00003", "Soro", true, false));

        InclusaoRecusadaException recusa = recusa(comando("TGO"));

        assertThat(recusa.motivo()).isEqualTo(MotivoRecusa.MATERIAL_DIVERSOS_PENDENTE);
        assertThat(recusa.materialId()).isEqualTo("00003");
        assertThat(recusa.materialDescricao()).isEqualTo("Soro");
    }

    @Test
    void c06ProcedimentoDeAnimalEmAtendimentoHumanoRecusa() {
        base.alterarTgo(tgo -> tgo.tipo = 3);

        assertThat(recusa(comando("TGO")).motivo()).isEqualTo(MotivoRecusa.CATEGORIA_INCOMPATIVEL);
    }

    @Test
    void c07FaturaFechadaNaFontePagadoraRecusa() {
        base.situacoesFatura.put("15|2", List.of("F"));

        assertThat(recusa(comandoNaFonte("TGO", 2)).motivo()).isEqualTo(MotivoRecusa.FATURA_FECHADA);
    }

    @Test
    void c10aAtendimentoAgendadoComAgendaAtivaGeraPendencia() {
        agendarAtendimento();

        ProcedimentoAdicionadoDto item = executar(comando("TGO"));

        assertItemPadrao(item);
        assertThat(motivos(item)).containsExactly(MotivoRecusa.AGENDA_ESPECIFICA);
    }

    @Test
    void c10bConfirmacaoDoAgendamentoPulaAPergunta() {
        agendarAtendimento();

        ProcedimentoAdicionadoDto item = executar(new AdicionarProcedimentoDto(15, "TGO", "M", 1, null, null, null, true, false));

        assertThat(item.pendencias()).isEmpty();
    }

    @Test
    void c13AntibiogramaSemCulturaNoAtendimentoGeraPendencia() {
        base.procedimentos.put("3", BaseEmMemoria.procedimento("3", "HGB", "Dosagem de hemoglobina", "00002", true));
        base.alterarTgo(tgo -> tgo.tsaId = "3");

        ProcedimentoAdicionadoDto item = executar(comando("TGO"));

        assertThat(item.tsaId()).isEqualTo("3");
        assertThat(motivos(item)).containsExactly(MotivoRecusa.ANTIBIOGRAMA_SEM_CULTURA);
    }

    @Test
    void c14AntibiogramaSemCulturaQueOTenhaComoTsaRecusa() {
        base.alterarTgo(tgo -> tgo.antibiograma = true);

        assertThat(recusa(comando("TGO")).motivo()).isEqualTo(MotivoRecusa.CULTURA_SEM_ANTIBIOGRAMA);
    }

    @Test
    void c15FontePagadoraSemCoberturaRecusaComOMotivo() {
        base.excecoes.put("1|34", new ExcecaoContrato(true, "Fora do rol", false, null, null));

        InclusaoRecusadaException recusa = recusa(comando("TGO"));

        assertThat(recusa.motivo()).isEqualTo(MotivoRecusa.SEM_COBERTURA);
        assertThat(recusa.getMessage()).isEqualTo("\nFonte pagadora: Particular - Particular\n\n - TGO - "
                + "Transaminase glutamico oxalacetica (AST/TGO)\n   Motivo: Fora do rol");
    }

    @Test
    void c17FraseDeAutorizacaoVaiNoItem() {
        base.excecoes.put("1|34", new ExcecaoContrato(false, null, false, "Exige guia médica", null));

        ProcedimentoAdicionadoDto item = executar(comando("TGO"));

        assertItemPadrao(item);
        assertThat(item.fraseAutorizacao()).isEqualTo("\n - TGO - Transaminase glutamico oxalacetica (AST/TGO): Exige guia médica");
    }

    @Test
    void c18AlertaDeCadastroVaiNoItem() {
        base.alterarTgo(tgo -> tgo.alertaCadastro = "Jejum de 8 horas");

        ProcedimentoAdicionadoDto item = executar(comando("TGO"));

        assertThat(item.alertaCadastro()).isEqualTo("Alerta(s) cadastro:\n\n - TGO: Jejum de 8 horas");
    }

    @Test
    void c19ExameJaIncluidoRecusa() {
        assertThat(recusa(comando("HEM")).motivo()).isEqualTo(MotivoRecusa.JA_INCLUIDO);
    }

    @Test
    void c21aProcedimentoSemPrecoRecusa() {
        base.componentes.put("1|34", List.of(new ComponentePreco(null, BigDecimal.valueOf(100), true, null, BigDecimal.ONE)));

        assertThat(recusa(comando("TGO")).motivo()).isEqualTo(MotivoRecusa.SEM_PRECO);
    }

    @Test
    void c21bNegociacaoEspecialAceitaComValorZero() {
        base.excecoes.put("1|34", new ExcecaoContrato(false, null, true, null, null));

        ProcedimentoAdicionadoDto item = executar(comando("TGO"));

        assertThat(item.valor()).isEqualByComparingTo("0");
        assertThat(item.negociacaoEspecial()).isTrue();
    }

    @Test
    void c22MicrobiologiaUsaOMaterialInformado() {
        base.alterarTgo(tgo -> tgo.microbiologia = true);

        ProcedimentoAdicionadoDto item = executar(new AdicionarProcedimentoDto(15, "TGO", "M", 1, "00002", "Sangue", null, false, false));

        assertThat(item.materialId()).isEqualTo("00002");
        assertThat(item.materialDescricao()).isEqualTo("Sangue");
    }

    @Test
    void c25ComplementoObrigatorioEntraNaDescricaoEmMinusculas() {
        base.materiais.put("00003", new Material("00003", "Soro", false, true));

        ProcedimentoAdicionadoDto item = executar(new AdicionarProcedimentoDto(15, "TGO", "M", 1, null, null, "BRAÇO ESQUERDO", false, false));

        assertThat(item.materialDescricao()).isEqualTo("Soro braço esquerdo");
        assertThat(item.materialComplementoObrigatorio()).isTrue();
    }

    @Test
    void c26ValorAcimaDoTetoDoAtendimentoGeraPendencia() {
        base.fontes.put(1L, fonteParticular(BigDecimal.TEN, false));

        ProcedimentoAdicionadoDto item = executar(comando("TGO"));

        assertItemPadrao(item);
        assertThat(item.pendencias()).containsExactly(new Pendencia(MotivoRecusa.TETO_ATENDIMENTO_EXCEDIDO,
                "Atingido o valor máximo em uma única O.S. para o convênio Particular"));
    }

    @Test
    void c27TetoDeFaturamentoSemPeriodoGeraPendencia() {
        base.fontes.put(1L, fonteParticular(BigDecimal.ZERO, true));

        ProcedimentoAdicionadoDto item = executar(comando("TGO"));

        assertThat(motivos(item)).containsExactly(MotivoRecusa.TETO_FATURAMENTO_EXCEDIDO);
    }

    @Test
    void c28SaldoEmRecalculoGeraPendencia() {
        base.fontes.put(1L, fonteParticular(BigDecimal.ZERO, true));
        base.tetos.put(1L, new TetoFaturamento(true, BigDecimal.valueOf(1000), BigDecimal.ZERO));

        ProcedimentoAdicionadoDto item = executar(comando("TGO"));

        assertThat(item.pendencias().get(item.pendencias().size() - 1).motivo()).isEqualTo(MotivoRecusa.SALDO_EM_RECALCULO);
    }

    @Test
    void c30UnidadeQueEncaminhaTodasAsAmostras() {
        base.unidades.put("2", new UnidadeColeta("2", "1", 3));

        ProcedimentoAdicionadoDto item = executar(comando("TGO"));

        assertThat(item.recipientesAmostraEncaminhada()).containsExactly("4");
    }

    private void agendarAtendimento() {
        Atendimento atendimento = base.atendimentos.get(15L);
        base.atendimentos.put(15L, new Atendimento(atendimento.id(), atendimento.data(), atendimento.categoria(),
                atendimento.sexoAnimal(), atendimento.unidadeColetaId(), 1L, atendimento.itens()));
        base.agendasAtivas.add("34");
    }

    private static FontePagadora fonteParticular(BigDecimal tetoAtendimento, boolean controlaTetoFaturamento) {
        return new FontePagadora(1, "Particular", "Particular", "B", "1", tetoAtendimento, false, controlaTetoFaturamento,
                controlaTetoFaturamento, false, null, false, null);
    }

    private static void assertItemPadrao(ProcedimentoAdicionadoDto item) {
        assertThat(item.procedimentoId()).isEqualTo("34");
        assertThat(item.materialId()).isEqualTo("00003");
        assertThat(item.materialDescricao()).isEqualTo("Soro");
        assertThat(item.setorId()).isEqualTo("003");
        assertThat(item.recipientes()).containsExactly("4");
        assertThat(item.valor()).isEqualByComparingTo("50");
        assertThat(item.entrega()).isNotNull();
    }

    private static List<MotivoRecusa> motivos(ProcedimentoAdicionadoDto item) {
        return item.pendencias().stream().map(Pendencia::motivo).toList();
    }

    private static AdicionarProcedimentoDto comando(String mnemonico) {
        return comandoComSexo(mnemonico, "M");
    }

    private static AdicionarProcedimentoDto comandoComSexo(String mnemonico, String sexo) {
        return new AdicionarProcedimentoDto(15, mnemonico, sexo, 1, null, null, null, false, false);
    }

    private static AdicionarProcedimentoDto comandoNaFonte(String mnemonico, long fontePagadoraId) {
        return new AdicionarProcedimentoDto(15, mnemonico, "M", fontePagadoraId, null, null, null, false, false);
    }

    private ProcedimentoAdicionadoDto executar(AdicionarProcedimentoDto comando) {
        return new AdicionarProcedimento(base.repositorios(), RELOGIO).executar(comando);
    }

    private InclusaoRecusadaException recusa(AdicionarProcedimentoDto comando) {
        InclusaoRecusadaException recusa = catchThrowableOfType(() -> executar(comando), InclusaoRecusadaException.class);
        assertThat(recusa).as("inclusão deveria ser recusada").isNotNull();
        return recusa;
    }

}