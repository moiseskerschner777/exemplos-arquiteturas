package sistema.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sistema.core.casodeuso.AbrirConta;
import sistema.core.casodeuso.ConsultarExtrato;
import sistema.core.casodeuso.Depositar;
import sistema.core.casodeuso.EncerrarConta;
import sistema.core.casodeuso.Sacar;
import sistema.core.casodeuso.Transferir;
import sistema.core.dominio.Conta;
import sistema.core.dominio.ContaNaoEncontradaException;
import sistema.core.dominio.Movimentacao;
import sistema.core.dominio.Pagina;
import sistema.core.dominio.RegraVioladaException;
import sistema.core.dominio.SituacaoConta;
import sistema.core.dominio.TipoMovimentacao;
import sistema.core.porta.out.ContaPortOut;

class CenariosTest {

    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 1, 10, 9, 30);

    private ContaRepositoryEmMemoria contas;
    private MovimentacaoRepositoryEmMemoria movimentacoes;
    private AbrirConta abrirConta;
    private Depositar depositar;
    private Sacar sacar;
    private TransacaoRepositoryEmMemoria transacao;
    private Transferir transferir;
    private EncerrarConta encerrarConta;
    private ConsultarExtrato consultarExtrato;

    @BeforeEach
    void preparar() {
        contas = new ContaRepositoryEmMemoria();
        movimentacoes = new MovimentacaoRepositoryEmMemoria();
        transacao = new TransacaoRepositoryEmMemoria(contas, movimentacoes);
        abrirConta = new AbrirConta(contas, new GeradorDeIdSequencial(), transacao);
        depositar = new Depositar(contas, movimentacoes, new RelogioFixo(AGORA), transacao);
        sacar = new Sacar(contas, movimentacoes, new RelogioFixo(AGORA), transacao);
        transferir = new Transferir(contas, movimentacoes, new RelogioFixo(AGORA), transacao);
        encerrarConta = new EncerrarConta(contas, transacao);
        consultarExtrato = new ConsultarExtrato(contas, movimentacoes, transacao);
    }

    private void criarContasDeExemplo() {
        contas.salvar(Conta.reconstituir("c1", "Ana", new BigDecimal("100.00"), SituacaoConta.ATIVA));
        contas.salvar(Conta.reconstituir("c2", "Bruno", new BigDecimal("50.00"), SituacaoConta.ATIVA));
        contas.salvar(Conta.reconstituir("c3", "Carla", BigDecimal.ZERO, SituacaoConta.ENCERRADA));
    }

    @Test
    void abreContaComSaldoZero() {
        String id = abrirConta.executar("Ana");

        Conta conta = contas.buscar(id);
        assertThat(conta.getTitular()).isEqualTo("Ana");
        assertThat(conta.getSaldo()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(conta.getSituacao()).isEqualTo(SituacaoConta.ATIVA);
    }

    @Test
    void recusaAbrirContaSemTitular() {
        assertThatThrownBy(() -> abrirConta.executar("  "))
                .isInstanceOf(RegraVioladaException.class)
                .hasMessage("titular obrigatorio");
        assertThatThrownBy(() -> abrirConta.executar(null))
                .isInstanceOf(RegraVioladaException.class);
        assertThat(contas.quantidade()).isZero();
    }

    @Test
    void deposita() {
        criarContasDeExemplo();

        depositar.executar("c1", new BigDecimal("25"), "x");

        assertThat(contas.buscar("c1").getSaldo()).isEqualByComparingTo("125");
        List<Movimentacao> registradas = movimentacoes.listar("c1");
        assertThat(registradas).hasSize(1);
        assertThat(registradas.get(0).getTipo()).isEqualTo(TipoMovimentacao.DEPOSITO);
        assertThat(registradas.get(0).getValor()).isEqualByComparingTo("25");
        assertThat(registradas.get(0).getDescricao()).isEqualTo("x");
        assertThat(registradas.get(0).getDataHora()).isEqualTo(AGORA);
    }

    @Test
    void recusaDepositoInvalido() {
        criarContasDeExemplo();

        assertThatThrownBy(() -> depositar.executar("c1", BigDecimal.ZERO, null))
                .isInstanceOf(RegraVioladaException.class)
                .hasMessage("valor deve ser maior que zero");
        assertThatThrownBy(() -> depositar.executar("c3", BigDecimal.TEN, null))
                .isInstanceOf(RegraVioladaException.class)
                .hasMessage("conta encerrada");
        assertThatThrownBy(() -> depositar.executar("zzz", BigDecimal.TEN, null))
                .isInstanceOf(ContaNaoEncontradaException.class)
                .hasMessage("conta nao encontrada");
        assertThat(contas.buscar("c1").getSaldo()).isEqualByComparingTo("100");
        assertThat(movimentacoes.listar("c1")).isEmpty();
        assertThat(movimentacoes.listar("c3")).isEmpty();
    }

    @Test
    void saca() {
        criarContasDeExemplo();

        sacar.executar("c1", new BigDecimal("40"), "x");

        assertThat(contas.buscar("c1").getSaldo()).isEqualByComparingTo("60");
        List<Movimentacao> registradas = movimentacoes.listar("c1");
        assertThat(registradas).hasSize(1);
        assertThat(registradas.get(0).getTipo()).isEqualTo(TipoMovimentacao.SAQUE);
        assertThat(registradas.get(0).getValor()).isEqualByComparingTo("40");
        assertThat(registradas.get(0).getDescricao()).isEqualTo("x");
        assertThat(registradas.get(0).getDataHora()).isEqualTo(AGORA);
    }

    @Test
    void recusaSaqueSemSaldoOuEmContaEncerrada() {
        criarContasDeExemplo();

        assertThatThrownBy(() -> sacar.executar("c1", new BigDecimal("150"), null))
                .isInstanceOf(RegraVioladaException.class)
                .hasMessage("saldo insuficiente");
        assertThatThrownBy(() -> sacar.executar("c3", BigDecimal.ONE, null))
                .isInstanceOf(RegraVioladaException.class)
                .hasMessage("conta encerrada");
        assertThat(contas.buscar("c1").getSaldo()).isEqualByComparingTo("100");
        assertThat(movimentacoes.listar("c1")).isEmpty();
        assertThat(movimentacoes.listar("c3")).isEmpty();
    }

    private void assertSaldosIniciais() {
        assertThat(contas.buscar("c1").getSaldo()).isEqualByComparingTo("100");
        assertThat(contas.buscar("c2").getSaldo()).isEqualByComparingTo("50");
        assertThat(movimentacoes.listar("c1")).isEmpty();
        assertThat(movimentacoes.listar("c2")).isEmpty();
    }

    @Test
    void aceitaTransferenciaComSaldo() {
        criarContasDeExemplo();

        transferir.executar("c1", "c2", new BigDecimal("50"), "x");

        assertThat(contas.buscar("c1").getSaldo()).isEqualByComparingTo("50");
        assertThat(contas.buscar("c2").getSaldo()).isEqualByComparingTo("100");
        List<Movimentacao> enviadas = movimentacoes.listar("c1");
        List<Movimentacao> recebidas = movimentacoes.listar("c2");
        assertThat(enviadas).hasSize(1);
        assertThat(recebidas).hasSize(1);
        assertThat(enviadas.get(0).getTipo()).isEqualTo(TipoMovimentacao.TRANSFERENCIA_ENVIADA);
        assertThat(enviadas.get(0).getValor()).isEqualByComparingTo("50");
        assertThat(recebidas.get(0).getTipo()).isEqualTo(TipoMovimentacao.TRANSFERENCIA_RECEBIDA);
        assertThat(recebidas.get(0).getValor()).isEqualByComparingTo("50");
        assertThat(recebidas.get(0).getDataHora()).isEqualTo(AGORA);
    }

    @Test
    void recusaSaldoInsuficiente() {
        criarContasDeExemplo();

        assertThatThrownBy(() -> transferir.executar("c1", "c2", new BigDecimal("150"), null))
                .isInstanceOf(RegraVioladaException.class)
                .hasMessage("saldo insuficiente");
        assertSaldosIniciais();
    }

    @Test
    void recusaValorZeroOuNegativo() {
        criarContasDeExemplo();

        assertThatThrownBy(() -> transferir.executar("c1", "c2", BigDecimal.ZERO, null))
                .isInstanceOf(RegraVioladaException.class)
                .hasMessage("valor deve ser maior que zero");
        assertThatThrownBy(() -> transferir.executar("c1", "c2", new BigDecimal("-10"), null))
                .isInstanceOf(RegraVioladaException.class)
                .hasMessage("valor deve ser maior que zero");
        assertSaldosIniciais();
    }

    @Test
    void recusaOrigemIgualAoDestino() {
        criarContasDeExemplo();

        assertThatThrownBy(() -> transferir.executar("c1", "c1", BigDecimal.TEN, null))
                .isInstanceOf(RegraVioladaException.class)
                .hasMessage("origem deve ser diferente do destino");
        assertSaldosIniciais();
    }

    @Test
    void recusaContaEncerrada() {
        criarContasDeExemplo();

        assertThatThrownBy(() -> transferir.executar("c1", "c3", BigDecimal.TEN, null))
                .isInstanceOf(RegraVioladaException.class)
                .hasMessage("conta encerrada");
        assertSaldosIniciais();
        assertThat(movimentacoes.listar("c3")).isEmpty();
    }

    @Test
    void recusaDestinoInexistenteSemAlterarOrigem() {
        criarContasDeExemplo();

        assertThatThrownBy(() -> transferir.executar("c1", "zzz", BigDecimal.TEN, null))
                .isInstanceOf(ContaNaoEncontradaException.class)
                .hasMessage("conta de destino nao encontrada");
        assertThatThrownBy(() -> transferir.executar("zzz", "c1", BigDecimal.TEN, null))
                .isInstanceOf(ContaNaoEncontradaException.class)
                .hasMessage("conta de origem nao encontrada");
        assertSaldosIniciais();
    }

    @Test
    void falhaAoSalvarODestinoDesfazAOrigem() {
        criarContasDeExemplo();
        ContaPortOut falhaNoDestino = new ContaPortOut() {

            @Override
            public Optional<Conta> buscarParaAtualizar(String id) {
                return contas.buscarParaAtualizar(id);
            }

            @Override
            public void salvar(Conta conta) {
                if ("c2".equals(conta.getId())) {
                    throw new IllegalStateException("erro ao processar");
                }
                contas.salvar(conta);
            }

        };
        Transferir comFalha = new Transferir(falhaNoDestino, movimentacoes, new RelogioFixo(AGORA), transacao);

        assertThatThrownBy(() -> comFalha.executar("c1", "c2", new BigDecimal("50"), null))
                .isInstanceOf(IllegalStateException.class);
        assertSaldosIniciais();
    }

    @Test
    void encerraContaSemSaldo() {
        criarContasDeExemplo();
        contas.salvar(Conta.reconstituir("c4", "Davi", BigDecimal.ZERO, SituacaoConta.ATIVA));

        encerrarConta.executar("c4");

        assertThat(contas.buscar("c4").getSituacao()).isEqualTo(SituacaoConta.ENCERRADA);
    }

    @Test
    void recusaEncerrarContaComSaldoOuJaEncerrada() {
        criarContasDeExemplo();

        assertThatThrownBy(() -> encerrarConta.executar("c1"))
                .isInstanceOf(RegraVioladaException.class)
                .hasMessage("conta com saldo nao pode ser encerrada");
        assertThatThrownBy(() -> encerrarConta.executar("c3"))
                .isInstanceOf(RegraVioladaException.class)
                .hasMessage("conta ja encerrada");
        assertThat(contas.buscar("c1").getSituacao()).isEqualTo(SituacaoConta.ATIVA);
    }

    @Test
    void paginaOExtratoNaOrdemDeRegistro() {
        criarContasDeExemplo();
        depositar.executar("c1", new BigDecimal("1"), "m1");
        depositar.executar("c1", new BigDecimal("2"), "m2");
        depositar.executar("c1", new BigDecimal("3"), "m3");

        Pagina<Movimentacao> primeira = consultarExtrato.executar("c1", 0, 2);
        Pagina<Movimentacao> segunda = consultarExtrato.executar("c1", 1, 2);

        assertThat(primeira.getItens()).extracting(Movimentacao::getDescricao).containsExactly("m1", "m2");
        assertThat(segunda.getItens()).extracting(Movimentacao::getDescricao).containsExactly("m3");
        assertThat(primeira.getTotal()).isEqualTo(3);
    }

    @Test
    void recusaPaginaOuTamanhoInvalidos() {
        criarContasDeExemplo();

        assertThatThrownBy(() -> consultarExtrato.executar("c1", -1, 2))
                .isInstanceOf(RegraVioladaException.class)
                .hasMessage("pagina e tamanho invalidos");
        assertThatThrownBy(() -> consultarExtrato.executar("c1", 0, 0))
                .isInstanceOf(RegraVioladaException.class)
                .hasMessage("pagina e tamanho invalidos");
    }

}