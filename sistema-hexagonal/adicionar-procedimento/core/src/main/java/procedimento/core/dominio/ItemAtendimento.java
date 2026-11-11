package procedimento.core.dominio;

public record ItemAtendimento(String procedimentoId, String perfilId, Long fontePagadoraId) {

    public boolean incluidoPorPerfil() {
        return perfilId != null && !perfilId.isBlank();
    }

}