package sistema.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sistema.core.casodeuso.AbrirConta;
import sistema.core.dominio.Conta;
import sistema.core.dominio.RegraVioladaException;
import sistema.core.dominio.SituacaoConta;

class CenariosTest {

    private ContaGatewayEmMemoria contas;
    private AbrirConta abrirConta;

    @BeforeEach
    void preparar() {
        contas = new ContaGatewayEmMemoria();
        abrirConta = new AbrirConta(contas, new GeradorDeIdSequencial(), new TransacaoGatewayEmMemoria());
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

}