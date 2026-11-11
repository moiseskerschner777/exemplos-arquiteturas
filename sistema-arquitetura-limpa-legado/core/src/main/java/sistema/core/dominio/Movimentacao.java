package sistema.core.dominio;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class Movimentacao {

    private final Long id;
    private final String contaId;
    private final TipoMovimentacao tipo;
    private final BigDecimal valor;
    private final String descricao;
    private final LocalDateTime dataHora;

    private Movimentacao(Long id, String contaId, TipoMovimentacao tipo, BigDecimal valor, String descricao,
            LocalDateTime dataHora) {
        this.id = id;
        this.contaId = contaId;
        this.tipo = tipo;
        this.valor = valor;
        this.descricao = descricao;
        this.dataHora = dataHora;
    }

    public static Movimentacao nova(String contaId, TipoMovimentacao tipo, BigDecimal valor, String descricao,
            LocalDateTime dataHora) {
        return new Movimentacao(null, contaId, tipo, valor, descricao, dataHora);
    }

    public static Movimentacao reconstituir(Long id, String contaId, TipoMovimentacao tipo, BigDecimal valor,
            String descricao, LocalDateTime dataHora) {
        return new Movimentacao(id, contaId, tipo, valor, descricao, dataHora);
    }

    public Long getId() {
        return id;
    }

    public String getContaId() {
        return contaId;
    }

    public TipoMovimentacao getTipo() {
        return tipo;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public String getDescricao() {
        return descricao;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

}