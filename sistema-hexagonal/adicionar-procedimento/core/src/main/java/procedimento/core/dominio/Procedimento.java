package procedimento.core.dominio;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public record Procedimento(
        String id,
        String mnemonico,
        String descricao,
        Sexo sexo,
        Optional<TipoProcedimento> tipo,
        boolean ativo,
        String materialId,
        boolean microbiologia,
        Integer tipoMicrobiologia,
        String tsaId,
        boolean antibiograma,
        String alertaCadastro,
        Integer segmento,
        boolean perfil,
        String setorPadraoId,
        String setorExclusivoId,
        Map<String, String> setorPorUnidade,
        Integer amostraEncaminhada,
        Integer prazoMinimoSegundos,
        String rotinaPrazoId,
        List<String> recipientes,
        Map<String, List<String>> recipientesPorUnidade) {

    public String mnemonicoOuId() {
        return mnemonico == null || mnemonico.isBlank() ? id : mnemonico;
    }

    public boolean aceita(CategoriaAtendimento categoria) {
        return tipo.map(t -> t.aceita(categoria)).orElse(categoria == CategoriaAtendimento.OUTRA);
    }

    public boolean temTsa() {
        return tsaId != null && !tsaId.isBlank();
    }

    public List<String> recipientesNaUnidade(String unidadeColetaId) {
        return recipientesPorUnidade.getOrDefault(unidadeColetaId, recipientes);
    }

}