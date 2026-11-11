package sistema.core;

import java.util.ArrayList;
import java.util.List;
import sistema.core.dominio.Movimentacao;
import sistema.core.dominio.Pagina;
import sistema.core.porta.out.MovimentacaoPortOut;

public class MovimentacaoRepositoryEmMemoria implements MovimentacaoPortOut, Restauravel {

    private final List<Movimentacao> movimentacoes = new ArrayList<Movimentacao>();
    private long proximoId = 1;

    @Override
    public void registrar(Movimentacao movimentacao) {
        String descricao = movimentacao.getDescricao() == null || movimentacao.getDescricao().isEmpty()
                ? null
                : movimentacao.getDescricao();
        movimentacoes.add(Movimentacao.reconstituir(proximoId++, movimentacao.getContaId(), movimentacao.getTipo(),
                movimentacao.getValor(), descricao, movimentacao.getDataHora()));
    }

    @Override
    public Pagina<Movimentacao> listarPorConta(String contaId, int pagina, int tamanho) {
        List<Movimentacao> daConta = listar(contaId);
        int inicio = Math.min(pagina * tamanho, daConta.size());
        int fim = Math.min(inicio + tamanho, daConta.size());
        return new Pagina<Movimentacao>(daConta.subList(inicio, fim), pagina, tamanho, daConta.size());
    }

    @Override
    public Runnable pontoDeRestauracao() {
        List<Movimentacao> copia = new ArrayList<Movimentacao>(movimentacoes);
        long id = proximoId;
        return () -> {
            movimentacoes.clear();
            movimentacoes.addAll(copia);
            proximoId = id;
        };
    }

    public List<Movimentacao> listar(String contaId) {
        List<Movimentacao> daConta = new ArrayList<Movimentacao>();
        for (Movimentacao movimentacao : movimentacoes) {
            if (movimentacao.getContaId().equals(contaId)) {
                daConta.add(movimentacao);
            }
        }
        return daConta;
    }

}