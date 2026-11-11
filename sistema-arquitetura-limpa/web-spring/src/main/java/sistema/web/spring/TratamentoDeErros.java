package sistema.web.spring;

import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import sistema.core.dominio.ContaNaoEncontradaException;
import sistema.core.dominio.RegraVioladaException;

@RestControllerAdvice
public class TratamentoDeErros {

    @ExceptionHandler(RegraVioladaException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    ErroDtoResponse regraViolada(RegraVioladaException e) {
        return new ErroDtoResponse(e.getMessage());
    }

    @ExceptionHandler(ContaNaoEncontradaException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    ErroDtoResponse contaNaoEncontrada(ContaNaoEncontradaException e) {
        return new ErroDtoResponse(e.getMessage());
    }

    @ExceptionHandler({ValorInvalidoException.class, HttpMessageNotReadableException.class})
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    ErroDtoResponse valorInvalido() {
        return new ErroDtoResponse("valor invalido");
    }

    @ExceptionHandler({PaginaInvalidaException.class, MissingServletRequestParameterException.class})
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    ErroDtoResponse paginaInvalida() {
        return new ErroDtoResponse("pagina e tamanho invalidos");
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    ErroDtoResponse erroAoProcessar() {
        return new ErroDtoResponse("erro ao processar");
    }

}