package sistema.core.porta.out;

import sistema.core.dominio.Movimentacao;
import sistema.core.dominio.Pagina;

public interface MovimentacaoPortOut {

    void registrar(Movimentacao movimentacao);

    Pagina<Movimentacao> listarPorConta(String contaId, int pagina, int tamanho);

}