package sistema.core.dominio;

public class ContaNaoEncontradaException extends RuntimeException {

    public ContaNaoEncontradaException(String mensagem) {
        super(mensagem);
    }

}