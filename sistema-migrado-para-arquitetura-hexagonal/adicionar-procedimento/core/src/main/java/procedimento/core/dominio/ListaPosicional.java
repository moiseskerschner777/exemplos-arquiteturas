package procedimento.core.dominio;

import java.util.List;

public record ListaPosicional(List<String> valores) {

    public ListaPosicional {
        valores = List.copyOf(valores);
    }

    public static ListaPosicional vazia() {
        return new ListaPosicional(List.of());
    }

    public int posicao(String valor) {
        return valores.indexOf(valor) + 1;
    }

    public int proxima(int posicao) {
        int seguinte = posicao + 1;
        return seguinte >= 1 && seguinte <= valores.size() ? seguinte : 0;
    }

    public String valor(int posicao) {
        return posicao >= 1 && posicao <= valores.size() ? valores.get(posicao - 1) : "";
    }

    public boolean temValor() {
        return valores.stream().anyMatch(valor -> !valor.isEmpty());
    }

}