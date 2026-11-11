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

    public static Conta reconstituir(String id, String titular, BigDecimal saldo, SituacaoConta situacao) {
        return new Conta(id, titular, saldo, situacao);
    }

    public void depositar(BigDecimal valor) {
        exigirValorPositivo(valor);
        exigirAtiva();
        saldo = saldo.add(valor);
    }

    public void sacar(BigDecimal valor) {
        exigirValorPositivo(valor);
        exigirAtiva();
        if (saldo.compareTo(valor) < 0) {
            throw new RegraVioladaException("saldo insuficiente");
        }
        saldo = saldo.subtract(valor);
    }

    public void encerrar() {
        if (situacao == SituacaoConta.ENCERRADA) {
            throw new RegraVioladaException("conta ja encerrada");
        }
        if (saldo.signum() != 0) {
            throw new RegraVioladaException("conta com saldo nao pode ser encerrada");
        }
        situacao = SituacaoConta.ENCERRADA;
    }

    private static void exigirValorPositivo(BigDecimal valor) {
        if (valor == null || valor.signum() <= 0) {
            throw new RegraVioladaException("valor deve ser maior que zero");
        }
    }

    private void exigirAtiva() {
        if (situacao == SituacaoConta.ENCERRADA) {
            throw new RegraVioladaException("conta encerrada");
        }
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