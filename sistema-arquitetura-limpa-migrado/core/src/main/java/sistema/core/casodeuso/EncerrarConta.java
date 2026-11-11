package sistema.core.casodeuso;

import sistema.core.dominio.Conta;
import sistema.core.dominio.ContaNaoEncontradaException;
import sistema.core.porta.in.EncerrarContaPortIn;
import sistema.core.porta.out.ContaPortOut;
import sistema.core.porta.out.TransacaoPortOut;

public class EncerrarConta implements EncerrarContaPortIn {

    private final ContaPortOut contas;
    private final TransacaoPortOut transacao;

    public EncerrarConta(ContaPortOut contas, TransacaoPortOut transacao) {
        this.contas = contas;
        this.transacao = transacao;
    }

    @Override
    public void executar(String contaId) {
        transacao.executar(() -> {
            Conta conta = contas.buscarParaAtualizar(contaId)
                    .orElseThrow(() -> new ContaNaoEncontradaException("conta nao encontrada"));
            conta.encerrar();
            contas.salvar(conta);
            return null;
        });
    }

}