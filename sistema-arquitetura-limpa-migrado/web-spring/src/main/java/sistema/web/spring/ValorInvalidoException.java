package sistema.web.spring;

public class ValorInvalidoException extends RuntimeException {

    public ValorInvalidoException() {
        super("valor invalido");
    }

}