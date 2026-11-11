package sistema.core.casodeuso;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import sistema.core.dominio.Conta;
import sistema.core.dominio.ContaNaoEncontradaException;
import sistema.core.dominio.Movimentacao;
import sistema.core.dominio.RegraVioladaException;
import sistema.core.dominio.TipoMovimentacao;
import sistema.core.porta.in.TransferirPortIn;
import sistema.core.porta.out.ContaPortOut;
import sistema.core.porta.out.MovimentacaoPortOut;
import sistema.core.porta.out.RelogioPortOut;
import sistema.core.porta.out.TransacaoPortOut;

public class Transferir implements TransferirPortIn {

    private final ContaPortOut contas;
    private final MovimentacaoPortOut movimentacoes;
    private final RelogioPortOut relogio;
    private final TransacaoPortOut transacao;

    public Transferir(ContaPortOut contas, MovimentacaoPortOut movimentacoes, RelogioPortOut relogio,
            TransacaoPortOut transacao) {
        this.contas = contas;
        this.movimentacoes = movimentacoes;
        this.relogio = relogio;
        this.transacao = transacao;
    }

    @Override
    public void executar(String origemId, String destinoId, BigDecimal valor, String descricao) {
        if (valor == null || valor.signum() <= 0) {
            throw new RegraVioladaException("valor deve ser maior que zero");
        }
        if (origemId == null || origemId.equals(destinoId)) {
            throw new RegraVioladaException("origem deve ser diferente do destino");
        }
        transacao.executar(() -> {
            Conta origem = contas.buscarParaAtualizar(origemId)
                    .orElseThrow(() -> new ContaNaoEncontradaException("conta de origem nao encontrada"));
            Conta destino = contas.buscarParaAtualizar(destinoId)
                    .orElseThrow(() -> new ContaNaoEncontradaException("conta de destino nao encontrada"));
            destino.depositar(valor);
            origem.sacar(valor);
            contas.salvar(origem);
            contas.salvar(destino);
            LocalDateTime agora = relogio.agora();
            movimentacoes.registrar(Movimentacao.nova(origemId, TipoMovimentacao.TRANSFERENCIA_ENVIADA, valor, descricao, agora));
            movimentacoes.registrar(Movimentacao.nova(destinoId, TipoMovimentacao.TRANSFERENCIA_RECEBIDA, valor, descricao, agora));
            return null;
        });
    }

}