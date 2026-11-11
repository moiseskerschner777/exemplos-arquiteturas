package procedimento.core.dominio;

public class ErroDePrazo extends RuntimeException {

    public ErroDePrazo(String mensagem) {
        super(mensagem);
    }

}