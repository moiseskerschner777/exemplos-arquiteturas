package procedimento.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import procedimento.core.erro.InclusaoRecusadaException;

@RestControllerAdvice
public class InclusaoRecusadaMapper {

    @ExceptionHandler(InclusaoRecusadaException.class)
    public ResponseEntity<InclusaoRecusadaDtoResponse> recusar(InclusaoRecusadaException recusa) {
        InclusaoRecusadaDtoResponse corpo = new InclusaoRecusadaDtoResponse(
                recusa.motivo().name(),
                recusa.motivo().codigoCos(),
                recusa.motivo().retornoCos(),
                recusa.getMessage(),
                recusa.materialId(),
                recusa.materialDescricao(),
                recusa.materiaisPermitidos());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(corpo);
    }

}