package procedimento.core.dominio;

import java.math.BigDecimal;
import java.util.Set;

public record FontePagadora(
        long id,
        String descricao,
        String apelido,
        String tipo,
        String tabelaPrecoId,
        BigDecimal valorTetoAtendimento,
        boolean faturaParcial,
        boolean usaControleTetoFaturamento,
        boolean bloqueiaTetoFaturamentoAtingido,
        boolean usaAutorizacaoOnline,
        Integer layoutAutorizacaoOnline,
        boolean permiteAutorizacaoManual,
        String isoladoPadraoId) {

    private static final Set<Integer> LAYOUTS_COM_CODIGO_AMB = Set.of(1, 2, 5, 7, 8, 11, 16, 20, 21, 22, 23);

    public boolean cortesia() {
        return "C".equals(tipo);
    }

    public boolean exigeCodigoAmb() {
        return usaAutorizacaoOnline && LAYOUTS_COM_CODIGO_AMB.contains(layoutAutorizacaoOnline) && !permiteAutorizacaoManual;
    }

}