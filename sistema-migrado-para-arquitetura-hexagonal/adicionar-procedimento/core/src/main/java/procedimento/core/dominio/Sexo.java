package procedimento.core.dominio;

public enum Sexo {

    AMBOS("A"),
    FEMININO("F"),
    MASCULINO("M"),
    NAO_INFORMADO("N");

    private final String codigo;

    Sexo(String codigo) {
        this.codigo = codigo;
    }

    public String codigo() {
        return codigo;
    }

    public static Sexo deCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return NAO_INFORMADO;
        }
        for (Sexo sexo : values()) {
            if (sexo.codigo.equalsIgnoreCase(codigo.trim())) {
                return sexo;
            }
        }
        return NAO_INFORMADO;
    }

    public boolean compativelCom(Sexo sexoPaciente) {
        return this == AMBOS
                || (this == FEMININO && sexoPaciente == FEMININO)
                || (this == MASCULINO && sexoPaciente == MASCULINO);
    }

}