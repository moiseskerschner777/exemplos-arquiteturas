package sistema.core.casodeuso;

import sistema.core.dominio.ContaNaoEncontradaException;
import sistema.core.dominio.Movimentacao;
import sistema.core.dominio.Pagina;
import sistema.core.dominio.RegraVioladaException;
import sistema.core.porta.in.ConsultarExtratoPortIn;
import sistema.core.porta.out.ContaPortOut;
import sistema.core.porta.out.MovimentacaoPortOut;
import sistema.core.porta.out.TransacaoPortOut;

public class ConsultarExtrato implements ConsultarExtratoPortIn {

    private final ContaPortOut contas;
    private final MovimentacaoPortOut movimentacoes;
    private final TransacaoPortOut transacao;

    public ConsultarExtrato(ContaPortOut contas, MovimentacaoPortOut movimentacoes, TransacaoPortOut transacao) {
        this.contas = contas;
        this.movimentacoes = movimentacoes;
        this.transacao = transacao;
    }

    @Override
    public Pagina<Movimentacao> executar(String contaId, int pagina, int tamanho) {
        if (pagina < 0 || tamanho <= 0) {
            throw new RegraVioladaException("pagina e tamanho invalidos");
        }
        return transacao.executar(() -> {
            contas.buscarParaAtualizar(contaId)
                    .orElseThrow(() -> new ContaNaoEncontradaException("conta nao encontrada"));
            return movimentacoes.listarPorConta(contaId, pagina, tamanho);
        });
    }

}