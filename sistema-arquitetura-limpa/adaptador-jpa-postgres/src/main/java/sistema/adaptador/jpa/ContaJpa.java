package sistema.adaptador.jpa;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "conta")
public class ContaJpa {

    @Id
    private String id;

    private String titular;

    private BigDecimal saldo;

    private String situacao;

    protected ContaJpa() {
    }

    public ContaJpa(String id, String titular, BigDecimal saldo, String situacao) {
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

    public String getSituacao() {
        return situacao;
    }

}