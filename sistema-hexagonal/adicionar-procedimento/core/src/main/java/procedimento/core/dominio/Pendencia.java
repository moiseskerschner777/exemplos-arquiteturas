package procedimento.core.dominio;

import procedimento.core.erro.MotivoRecusa;

public record Pendencia(MotivoRecusa motivo, String mensagem) {

}