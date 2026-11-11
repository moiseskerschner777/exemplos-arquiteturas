package sistema.core.contrato;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import sistema.core.dominio.Conta;
import sistema.core.porta.out.ContaPortOut;
import sistema.core.porta.out.TransacaoPortOut;

public abstract class TransacaoPortOutContrato {

    protected abstract TransacaoPortOut transacao();

    protected abstract ContaPortOut contas();

    protected abstract int quantidadeComId(String id);

    @Test
    void confirmaQuandoAOperacaoTermina() {
        transacao().executar(() -> {
            contas().salvar(Conta.abrir("c9", "Eva"));
            return null;
        });

        assertThat(quantidadeComId("c9")).isEqualTo(1);
    }

    @Test
    void desfazTudoQuandoAOperacaoLancaExcecao() {
        assertThatThrownBy(() -> transacao().executar(() -> {
            contas().salvar(Conta.abrir("c9", "Eva"));
            throw new IllegalStateException("falha depois de salvar");
        })).isInstanceOf(IllegalStateException.class).hasMessage("falha depois de salvar");

        assertThat(quantidadeComId("c9")).isZero();
    }

}