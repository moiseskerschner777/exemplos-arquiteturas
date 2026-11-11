package procedimento.core.dominio;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public record Atendimento(
        long id,
        LocalDate data,
        CategoriaAtendimento categoria,
        Sexo sexoAnimal,
        String unidadeColetaId,
        Long agendamentoId,
        List<ItemAtendimento> itens) {

    public Optional<ItemAtendimento> itemDoProcedimento(String procedimentoId) {
        return itens.stream().filter(item -> procedimentoId.equals(item.procedimentoId())).findFirst();
    }

    public boolean temAgendamento() {
        return agendamentoId != null;
    }

}