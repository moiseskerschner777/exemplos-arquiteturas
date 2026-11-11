package procedimento.core.porta.in.dto;

import procedimento.core.dominio.Pendencia;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ProcedimentoAdicionadoDto(
        String procedimentoId,
        String mnemonico,
        String descricao,
        long fontePagadoraId,
        BigDecimal valor,
        boolean negociacaoEspecial,
        LocalDateTime entrega,
        String materialId,
        String materialDescricao,
        boolean materialComplementoObrigatorio,
        String setorId,
        List<String> recipientes,
        List<String> recipientesAmostraEncaminhada,
        List<String> recipientesNaoPermitemAmostraEncaminhada,
        String tsaId,
        String fraseAutorizacao,
        String alertaCadastro,
        List<Pendencia> pendencias) {

}