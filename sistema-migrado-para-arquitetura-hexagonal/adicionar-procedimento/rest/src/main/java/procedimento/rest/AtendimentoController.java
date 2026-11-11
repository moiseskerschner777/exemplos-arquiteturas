package procedimento.rest;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import procedimento.core.porta.in.AdicionarProcedimentoPortIn;
import procedimento.core.porta.in.dto.AdicionarProcedimentoDto;
import procedimento.core.porta.in.dto.ProcedimentoAdicionadoDto;

@RestController
@RequestMapping("/attendances/{attendanceId}/procedures")
public class AtendimentoController {

    private final AdicionarProcedimentoPortIn adicionarProcedimento;

    public AtendimentoController(AdicionarProcedimentoPortIn adicionarProcedimento) {
        this.adicionarProcedimento = adicionarProcedimento;
    }

    @PostMapping
    public ProcedimentoAdicionadoDto adicionar(@PathVariable("attendanceId") long atendimentoId,
                                               @RequestBody AdicionarProcedimentoDtoRequest pedido) {
        return adicionarProcedimento.executar(new AdicionarProcedimentoDto(
                atendimentoId,
                pedido.mnemonico(),
                pedido.sexoPaciente(),
                pedido.fontePagadoraId(),
                pedido.materialId(),
                pedido.materialDescricao(),
                pedido.complementoMaterial(),
                pedido.confirmouAgendamento(),
                pedido.ignorarValidacoes()));
    }

}