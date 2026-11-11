package sistema.core.casodeuso;

import java.math.BigDecimal;
import sistema.core.dominio.Conta;
import sistema.core.dominio.ContaNaoEncontradaException;
import sistema.core.dominio.Movimentacao;
import sistema.core.dominio.TipoMovimentacao;
import sistema.core.porta.in.DepositarPortIn;
import sistema.core.porta.out.ContaPortOut;
import sistema.core.porta.out.MovimentacaoPortOut;
import sistema.core.porta.out.RelogioPortOut;
import sistema.core.porta.out.TransacaoPortOut;

public class Depositar implements DepositarPortIn {

    private final ContaPortOut contas;
    private final MovimentacaoPortOut movimentacoes;
    private final RelogioPortOut relogio;
    private final TransacaoPortOut transacao;

    public Depositar(ContaPortOut contas, MovimentacaoPortOut movimentacoes, RelogioPortOut relogio,
            TransacaoPortOut transacao) {
        this.contas = contas;
        this.movimentacoes = movimentacoes;
        this.relogio = relogio;
        this.transacao = transacao;
    }

    @Override
    public void executar(String contaId, BigDecimal valor, String descricao) {
        transacao.executar(() -> {
            Conta conta = contas.buscarParaAtualizar(contaId)
                    .orElseThrow(() -> new ContaNaoEncontradaException("conta nao encontrada"));
            conta.depositar(valor);
            contas.salvar(conta);
            movimentacoes.registrar(Movimentacao.nova(contaId, TipoMovimentacao.DEPOSITO, valor, descricao, relogio.agora()));
            return null;
        });
    }

}