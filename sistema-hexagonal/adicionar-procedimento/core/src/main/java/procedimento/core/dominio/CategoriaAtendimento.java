package procedimento.core.dominio;

public enum CategoriaAtendimento {

    HUMANO,
    ANIMAL,
    OUTRA;

    public static CategoriaAtendimento deCodigo(Integer codigo) {
        if (codigo == null) {
            return OUTRA;
        }
        return switch (codigo) {
            case 1 -> HUMANO;
            case 2 -> ANIMAL;
            default -> OUTRA;
        };
    }

}