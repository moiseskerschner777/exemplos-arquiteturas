package sistema.core;

import java.util.List;
import sistema.core.contrato.MovimentacaoPortOutContrato;
import sistema.core.dominio.Movimentacao;
import sistema.core.porta.out.MovimentacaoPortOut;
import sistema.core.porta.out.TransacaoPortOut;

class MovimentacaoRepositoryEmMemoriaTest extends MovimentacaoPortOutContrato {

    private final MovimentacaoRepositoryEmMemoria repository = new MovimentacaoRepositoryEmMemoria();
    private final TransacaoRepositoryEmMemoria transacao = new TransacaoRepositoryEmMemoria();

    @Override
    protected MovimentacaoPortOut movimentacoes() {
        return repository;
    }

    @Override
    protected TransacaoPortOut transacao() {
        return transacao;
    }

    @Override
    protected List<Movimentacao> lerPorConta(String contaId) {
        return repository.listar(contaId);
    }

}