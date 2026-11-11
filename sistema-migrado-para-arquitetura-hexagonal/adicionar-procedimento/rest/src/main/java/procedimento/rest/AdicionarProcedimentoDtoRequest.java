package procedimento.rest;

public record AdicionarProcedimentoDtoRequest(
        String mnemonico,
        String sexoPaciente,
        long fontePagadoraId,
        String materialId,
        String materialDescricao,
        String complementoMaterial,
        boolean confirmouAgendamento,
        boolean ignorarValidacoes) {

}