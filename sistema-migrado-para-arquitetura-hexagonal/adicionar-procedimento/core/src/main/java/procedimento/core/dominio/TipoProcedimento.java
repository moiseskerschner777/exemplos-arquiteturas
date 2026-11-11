package procedimento.core.dominio;

import java.util.Optional;

public enum TipoProcedimento {

    TODOS(1),
    PACIENTE(2),
    ANIMAL(3),
    ANALISES_ESPECIAIS(4);

    private final int codigo;

    TipoProcedimento(int codigo) {
        this.codigo = codigo;
    }

    public static Optional<TipoProcedimento> deCodigo(Integer codigo) {
        for (TipoProcedimento tipo : values()) {
            if (codigo != null && tipo.codigo == codigo) {
                return Optional.of(tipo);
            }
        }
        return Optional.empty();
    }

    public boolean aceita(CategoriaAtendimento categoria) {
        return switch (categoria) {
            case HUMANO -> this == TODOS || this == PACIENTE;
            case ANIMAL -> this == TODOS || this == ANIMAL;
            case OUTRA -> true;
        };
    }

}