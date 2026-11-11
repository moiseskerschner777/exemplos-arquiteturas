package procedimento.rest;

import java.util.List;

public record InclusaoRecusadaDtoResponse(
        String motivo,
        String codigoCos,
        int retornoCos,
        String mensagem,
        String materialId,
        String materialDescricao,
        List<String> materiaisPermitidos) {

}