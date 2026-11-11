package procedimento.core.dominio;

import java.util.Locale;

public record Material(String id, String descricao, boolean diversos, boolean complementoObrigatorio) {

    public static String descricaoComComplemento(String descricao, String complemento) {
        String texto = complemento == null ? "" : complemento.toLowerCase(Locale.ROOT);
        return descricao + " " + texto;
    }

}