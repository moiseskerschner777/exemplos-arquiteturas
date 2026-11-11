package sistema.web.spring;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sistema.core.dominio.Movimentacao;
import sistema.core.dominio.Pagina;
import sistema.core.porta.in.ConsultarExtratoPortIn;

@RestController
public class ExtratoController {

    private final ConsultarExtratoPortIn consultarExtrato;

    public ExtratoController(ConsultarExtratoPortIn consultarExtrato) {
        this.consultarExtrato = consultarExtrato;
    }

    @GetMapping("/api/contas/{id}/extrato")
    public ExtratoDtoResponse consultar(@PathVariable String id, @RequestParam String pagina,
            @RequestParam String tamanho) {
        Pagina<Movimentacao> extrato = consultarExtrato.executar(id, numero(pagina), numero(tamanho));
        return new ExtratoDtoResponse(extrato.getItens().stream()
                .map(m -> new MovimentacaoDtoResponse(m.getDataHora(), m.getTipo().name(), m.getValor(), m.getDescricao()))
                .toList(), extrato.getPagina(), extrato.getTamanho(), extrato.getTotal());
    }

    private int numero(String texto) {
        try {
            return Integer.parseInt(texto);
        } catch (NumberFormatException e) {
            throw new PaginaInvalidaException();
        }
    }

}