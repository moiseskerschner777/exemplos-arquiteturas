package sistema.web.spring;

public class PaginaInvalidaException extends RuntimeException {

    public PaginaInvalidaException() {
        super("pagina e tamanho invalidos");
    }

}