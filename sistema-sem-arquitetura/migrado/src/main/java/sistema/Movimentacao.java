package sistema;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "movimentacao")
public class Movimentacao {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_mov")
    @SequenceGenerator(name = "seq_mov", sequenceName = "seq_movimentacao", allocationSize = 1)
    private Long id;
    @Column(name = "conta_id")
    private String contaId;
    private String tipo;
    private BigDecimal valor;
    private String descricao;
    @Column(name = "data_hora")
    private LocalDateTime dataHora;

    protected Movimentacao() {
    }

    public Movimentacao(String contaId, String tipo, BigDecimal valor, String descricao) {
        this.contaId = contaId;
        this.tipo = tipo;
        this.valor = valor;
        this.descricao = descricao;
        this.dataHora = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getContaId() {
        return contaId;
    }

    public String getTipo() {
        return tipo;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public String getDescricao() {
        return descricao;
    }
}
