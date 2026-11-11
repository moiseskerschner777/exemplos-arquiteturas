package sistema.web.spring;

import java.util.List;

public record ExtratoDtoResponse(List<MovimentacaoDtoResponse> itens, int pagina, int tamanho, long total) {

}