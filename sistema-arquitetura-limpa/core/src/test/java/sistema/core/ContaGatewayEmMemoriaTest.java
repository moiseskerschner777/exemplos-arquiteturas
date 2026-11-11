package sistema.core;

import sistema.core.contrato.ContaPortOutContrato;
import sistema.core.dominio.Conta;
import sistema.core.porta.out.ContaPortOut;
import sistema.core.porta.out.TransacaoPortOut;

class ContaGatewayEmMemoriaTest extends ContaPortOutContrato {

    private final ContaGatewayEmMemoria gateway = new ContaGatewayEmMemoria();
    private final TransacaoGatewayEmMemoria transacao = new TransacaoGatewayEmMemoria();

    @Override
    protected ContaPortOut contas() {
        return gateway;
    }

    @Override
    protected TransacaoPortOut transacao() {
        return transacao;
    }

    @Override
    protected LinhaDeConta ler(String id) {
        Conta conta = gateway.buscar(id);
        return new LinhaDeConta(conta.getTitular(), conta.getSaldo(), conta.getSituacao());
    }

    @Override
    protected int quantidadeComId(String id) {
        return gateway.buscar(id) == null ? 0 : 1;
    }

}