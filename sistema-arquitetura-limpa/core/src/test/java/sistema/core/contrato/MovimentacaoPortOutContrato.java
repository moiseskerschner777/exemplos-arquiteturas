package sistema.core.contrato;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import sistema.core.dominio.Movimentacao;
import sistema.core.dominio.Pagina;
import sistema.core.dominio.TipoMovimentacao;
import sistema.core.porta.out.MovimentacaoPortOut;
import sistema.core.porta.out.TransacaoPortOut;

public abstract class MovimentacaoPortOutContrato {

    protected static final String CONTA = "c1";
    protected static final LocalDateTime INSTANTE = LocalDateTime.of(2026, 1, 10, 9, 30);

    protected abstract MovimentacaoPortOut movimentacoes();

    protected abstract TransacaoPortOut transacao();

    protected abstract List<Movimentacao> lerPorConta(String contaId);

    private void registrar(String descricao) {
        transacao().executar(() -> {
            movimentacoes().registrar(Movimentacao.nova(CONTA, TipoMovimentacao.DEPOSITO, new BigDecimal("25.00"), descricao, INSTANTE));
            return null;
        });
    }

    @Test
    void registraMovimentacao() {
        registrar("x");

        List<Movimentacao> lidas = lerPorConta(CONTA);
        assertThat(lidas).hasSize(1);
        Movimentacao lida = lidas.get(0);
        assertThat(lida.getId()).isNotNull();
        assertThat(lida.getTipo()).isEqualTo(TipoMovimentacao.DEPOSITO);
        assertThat(lida.getValor()).isEqualByComparingTo("25");
        assertThat(lida.getDescricao()).isEqualTo("x");
        assertThat(lida.getDataHora()).isEqualTo(INSTANTE);
    }

    @Test
    void descricaoVaziaEGravadaComoNula() {
        registrar("");

        assertThat(lerPorConta(CONTA).get(0).getDescricao()).isNull();
    }

    @Test
    void idDaMovimentacaoGeradoEmSequencia() {
        registrar("a");
        registrar("b");

        List<Movimentacao> lidas = lerPorConta(CONTA);
        assertThat(lidas).hasSize(2);
        assertThat(lidas.get(1).getId()).isGreaterThan(lidas.get(0).getId());
        assertThat(lidas.get(0).getDescricao()).isEqualTo("a");
    }

    @Test
    void paginaNaOrdemDeRegistro() {
        registrar("m1");
        registrar("m2");
        registrar("m3");

        Pagina<Movimentacao> primeira = transacao().executar(() -> movimentacoes().listarPorConta(CONTA, 0, 2));
        Pagina<Movimentacao> segunda = transacao().executar(() -> movimentacoes().listarPorConta(CONTA, 1, 2));

        assertThat(primeira.getItens()).extracting(Movimentacao::getDescricao).containsExactly("m1", "m2");
        assertThat(segunda.getItens()).extracting(Movimentacao::getDescricao).containsExactly("m3");
        assertThat(primeira.getTotal()).isEqualTo(3);
    }

}