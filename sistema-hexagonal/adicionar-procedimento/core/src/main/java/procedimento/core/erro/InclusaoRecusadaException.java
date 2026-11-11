package procedimento.core.erro;

import java.util.List;

public class InclusaoRecusadaException extends RuntimeException {

    private final MotivoRecusa motivo;
    private final String materialId;
    private final String materialDescricao;
    private final List<String> materiaisPermitidos;

    public InclusaoRecusadaException(MotivoRecusa motivo, String mensagem) {
        this(motivo, mensagem, null, null, List.of());
    }

    public InclusaoRecusadaException(MotivoRecusa motivo, String mensagem, String materialId, String materialDescricao,
                                     List<String> materiaisPermitidos) {
        super(mensagem == null || mensagem.isBlank() ? motivo.name() : mensagem);
        this.motivo = motivo;
        this.materialId = materialId;
        this.materialDescricao = materialDescricao;
        this.materiaisPermitidos = List.copyOf(materiaisPermitidos);
    }

    public MotivoRecusa motivo() {
        return motivo;
    }

    public String materialId() {
        return materialId;
    }

    public String materialDescricao() {
        return materialDescricao;
    }

    public List<String> materiaisPermitidos() {
        return materiaisPermitidos;
    }

}