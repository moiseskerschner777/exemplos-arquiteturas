package procedimento.core.dominio;

import java.util.List;

public record Feriado(String data, String ano, String definicao, List<UnidadeDoFeriado> unidades) {

    public Feriado {
        unidades = List.copyOf(unidades);
    }

    public boolean valeEm(String diaMes, String anoDaData) {
        return data.equals(diaMes) && (ano.equals(anoDaData) || "*".equals(ano));
    }

    public record UnidadeDoFeriado(String unidadeColetaId, String unidadeAtendimentoId) {

    }

}