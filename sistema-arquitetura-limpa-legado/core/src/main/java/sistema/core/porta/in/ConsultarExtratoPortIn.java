package sistema.core.porta.in;

import sistema.core.dominio.Movimentacao;
import sistema.core.dominio.Pagina;

public interface ConsultarExtratoPortIn {

    Pagina<Movimentacao> executar(String contaId, int pagina, int tamanho);

}