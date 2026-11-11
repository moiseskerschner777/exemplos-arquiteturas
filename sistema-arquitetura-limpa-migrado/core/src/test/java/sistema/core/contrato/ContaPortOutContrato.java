package sistema.core.contrato;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import sistema.core.dominio.Conta;
import sistema.core.dominio.SituacaoConta;
import sistema.core.porta.out.ContaPortOut;
import sistema.core.porta.out.TransacaoPortOut;

public abstract class ContaPortOutContrato {

    public static final class LinhaDeConta {

        public final String titular;
        public final BigDecimal saldo;
        public final SituacaoConta situacao;

        public LinhaDeConta(String titular, BigDecimal saldo, SituacaoConta situacao) {
            this.titular = titular;
            this.saldo = saldo;
            this.situacao = situacao;
        }

    }

    protected abstract ContaPortOut contas();

    protected abstract TransacaoPortOut transacao();

    protected abstract LinhaDeConta ler(String id);

    protected abstract int quantidadeComId(String id);

    @Test
    void salvaContaNova() {
        Conta conta = Conta.abrir("c9", "Eva");

        transacao().executar(() -> {
            contas().salvar(conta);
            return null;
        });

        LinhaDeConta linha = ler("c9");
        assertThat(linha.titular).isEqualTo("Eva");
        assertThat(linha.saldo).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(linha.situacao).isEqualTo(SituacaoConta.ATIVA);
    }

    @Test
    void salvarDuasVezesNaoDuplicaAConta() {
        Conta conta = Conta.abrir("c9", "Eva");

        transacao().executar(() -> {
            contas().salvar(conta);
            contas().salvar(conta);
            return null;
        });

        assertThat(quantidadeComId("c9")).isEqualTo(1);
    }

    @Test
    void buscarParaAtualizarContaInexistenteDevolveVazio() {
        Optional<Conta> conta = transacao().executar(() -> contas().buscarParaAtualizar("inexistente"));

        assertThat(conta).isEmpty();
    }

    @Test
    void buscarParaAtualizarDevolveAContaSalva() {
        Conta salva = Conta.abrir("c9", "Eva");
        salva.depositar(new BigDecimal("12.50"));

        Optional<Conta> lida = transacao().executar(() -> {
            contas().salvar(salva);
            return contas().buscarParaAtualizar("c9");
        });

        assertThat(lida).isPresent();
        assertThat(lida.get().getTitular()).isEqualTo("Eva");
        assertThat(lida.get().getSaldo()).isEqualByComparingTo("12.50");
        assertThat(lida.get().getSituacao()).isEqualTo(SituacaoConta.ATIVA);
    }

}