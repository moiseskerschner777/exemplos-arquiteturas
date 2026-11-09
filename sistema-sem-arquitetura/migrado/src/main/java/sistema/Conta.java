package sistema;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "conta")
public class Conta {

    @Id
    private String id;
    private String titular;
    private BigDecimal saldo;
    private String situacao;

    protected Conta() {
    }

    public Conta(String id, String titular, BigDecimal saldo, String situacao) {
        this.id = id;
        this.titular = titular;
        this.saldo = saldo;
        this.situacao = situacao;
    }

    public String getId() {
        return id;
    }

    public String getTitular() {
        return titular;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }

    public void setSaldo(BigDecimal saldo) {
        this.saldo = saldo;
    }

    public String getSituacao() {
        return situacao;
    }

    public void setSituacao(String situacao) {
        this.situacao = situacao;
    }
}
