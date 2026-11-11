package procedimento.core.porta.in.dto;

public record AdicionarProcedimentoDto(
        long atendimentoId,
        String mnemonico,
        String sexoPaciente,
        long fontePagadoraId,
        String materialId,
        String materialDescricao,
        String complementoMaterial,
        boolean confirmouAgendamento,
        boolean ignorarValidacoes) {

}