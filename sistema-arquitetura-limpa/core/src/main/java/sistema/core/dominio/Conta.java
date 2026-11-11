package sistema.core.dominio;

import java.math.BigDecimal;

public class Conta {

    private final String id;
    private final String titular;
    private BigDecimal saldo;
    private SituacaoConta situacao;

    private Conta(String id, String titular, BigDecimal saldo, SituacaoConta situacao) {
        this.id = id;
        this.titular = titular;
        this.saldo = saldo;
        this.situacao = situacao;
    }

    public static Conta abrir(String id, String titular) {
        if (titular == null || titular.trim().isEmpty()) {
            throw new RegraVioladaException("titular obrigatorio");
        }
        return new Conta(id, titular.trim(), BigDecimal.ZERO, SituacaoConta.ATIVA);
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

    public SituacaoConta getSituacao() {
        return situacao;
    }

}