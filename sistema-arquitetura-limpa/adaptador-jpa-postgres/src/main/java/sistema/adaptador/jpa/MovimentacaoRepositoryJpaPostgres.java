package sistema.adaptador.jpa;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import sistema.core.dominio.Movimentacao;
import sistema.core.dominio.Pagina;
import sistema.core.dominio.TipoMovimentacao;
import sistema.core.porta.out.MovimentacaoPortOut;

public class MovimentacaoRepositoryJpaPostgres implements MovimentacaoPortOut {

    private final MovimentacaoJpaRepository repositorio;

    public MovimentacaoRepositoryJpaPostgres(MovimentacaoJpaRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public void registrar(Movimentacao movimentacao) {
        repositorio.save(new MovimentacaoJpa(movimentacao.getContaId(), movimentacao.getTipo().name(),
                movimentacao.getValor(), vazioComoNulo(movimentacao.getDescricao()), movimentacao.getDataHora()));
    }

    private static String vazioComoNulo(String texto) {
        return texto == null || texto.isEmpty() ? null : texto;
    }

    @Override
    public Pagina<Movimentacao> listarPorConta(String contaId, int pagina, int tamanho) {
        Page<MovimentacaoJpa> lidas = repositorio.findByContaIdOrderById(contaId, PageRequest.of(pagina, tamanho));
        return new Pagina<Movimentacao>(lidas.map(m -> Movimentacao.reconstituir(m.getId(), m.getContaId(),
                TipoMovimentacao.valueOf(m.getTipo()), m.getValor(), m.getDescricao(), m.getDataHora())).getContent(),
                pagina, tamanho, lidas.getTotalElements());
    }

}