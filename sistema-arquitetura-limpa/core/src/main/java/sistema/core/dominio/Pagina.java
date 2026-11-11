package sistema.core.dominio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Pagina<T> {

    private final List<T> itens;
    private final int pagina;
    private final int tamanho;
    private final long total;

    public Pagina(List<T> itens, int pagina, int tamanho, long total) {
        this.itens = Collections.unmodifiableList(new ArrayList<T>(itens));
        this.pagina = pagina;
        this.tamanho = tamanho;
        this.total = total;
    }

    public List<T> getItens() {
        return itens;
    }

    public int getPagina() {
        return pagina;
    }

    public int getTamanho() {
        return tamanho;
    }

    public long getTotal() {
        return total;
    }

}