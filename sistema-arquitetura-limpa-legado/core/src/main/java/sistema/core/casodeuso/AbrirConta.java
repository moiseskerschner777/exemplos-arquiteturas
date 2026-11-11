package sistema.core.casodeuso;

import sistema.core.dominio.Conta;
import sistema.core.porta.in.AbrirContaPortIn;
import sistema.core.porta.out.ContaPortOut;
import sistema.core.porta.out.GeradorDeIdPortOut;
import sistema.core.porta.out.TransacaoPortOut;

public class AbrirConta implements AbrirContaPortIn {

    private final ContaPortOut contas;
    private final GeradorDeIdPortOut geradorDeId;
    private final TransacaoPortOut transacao;

    public AbrirConta(ContaPortOut contas, GeradorDeIdPortOut geradorDeId, TransacaoPortOut transacao) {
        this.contas = contas;
        this.geradorDeId = geradorDeId;
        this.transacao = transacao;
    }

    @Override
    public String executar(String titular) {
        return transacao.executar(() -> {
            String id = geradorDeId.novoIdDeConta();
            contas.salvar(Conta.abrir(id, titular));
            return id;
        });
    }

}