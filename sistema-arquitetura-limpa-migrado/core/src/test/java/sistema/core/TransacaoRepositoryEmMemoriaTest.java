package sistema.core;

import sistema.core.contrato.TransacaoPortOutContrato;
import sistema.core.porta.out.ContaPortOut;
import sistema.core.porta.out.TransacaoPortOut;

class TransacaoRepositoryEmMemoriaTest extends TransacaoPortOutContrato {

    private final ContaRepositoryEmMemoria contas = new ContaRepositoryEmMemoria();
    private final TransacaoRepositoryEmMemoria transacao = new TransacaoRepositoryEmMemoria(contas);

    @Override
    protected TransacaoPortOut transacao() {
        return transacao;
    }

    @Override
    protected ContaPortOut contas() {
        return contas;
    }

    @Override
    protected int quantidadeComId(String id) {
        return contas.buscar(id) == null ? 0 : 1;
    }

}