package sistema.core;

import sistema.core.contrato.ContaPortOutContrato;
import sistema.core.dominio.Conta;
import sistema.core.porta.out.ContaPortOut;
import sistema.core.porta.out.TransacaoPortOut;

class ContaRepositoryEmMemoriaTest extends ContaPortOutContrato {

    private final ContaRepositoryEmMemoria repository = new ContaRepositoryEmMemoria();
    private final TransacaoRepositoryEmMemoria transacao = new TransacaoRepositoryEmMemoria();

    @Override
    protected ContaPortOut contas() {
        return repository;
    }

    @Override
    protected TransacaoPortOut transacao() {
        return transacao;
    }

    @Override
    protected LinhaDeConta ler(String id) {
        Conta conta = repository.buscar(id);
        return new LinhaDeConta(conta.getTitular(), conta.getSaldo(), conta.getSituacao());
    }

    @Override
    protected int quantidadeComId(String id) {
        return repository.buscar(id) == null ? 0 : 1;
    }

}