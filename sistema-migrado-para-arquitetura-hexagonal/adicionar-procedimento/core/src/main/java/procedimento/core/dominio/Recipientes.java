package procedimento.core.dominio;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public record Recipientes(List<String> todos, List<String> amostraEncaminhada, List<String> naoPermitemAmostraEncaminhada,
                          Map<String, List<String>> procedimentosPorRecipiente) {

    public static Recipientes montar(Procedimento procedimento, UnidadeColeta unidade) {
        Map<String, List<String>> porRecipiente = new LinkedHashMap<>();
        List<String> todos = new ArrayList<>();
        for (String recipiente : procedimento.recipientesNaUnidade(unidade.id())) {
            porRecipiente.computeIfAbsent(recipiente, r -> new ArrayList<>()).add(procedimento.id());
            if (!todos.contains(recipiente)) {
                todos.add(recipiente);
            }
        }
        List<String> encaminhada = new ArrayList<>();
        List<String> naoPermite = new ArrayList<>();
        Integer flag = procedimento.amostraEncaminhada();
        Integer configuracaoUnidade = unidade.amostraEncaminhada();
        for (String recipiente : todos) {
            boolean unidadeEncaminhaTudo = Integer.valueOf(3).equals(configuracaoUnidade);
            boolean procedimentoEncaminha = Integer.valueOf(3).equals(flag) && Integer.valueOf(1).equals(configuracaoUnidade);
            if (unidadeEncaminhaTudo || procedimentoEncaminha) {
                encaminhada.add(recipiente);
            } else if (Integer.valueOf(2).equals(flag)) {
                naoPermite.add(recipiente);
            }
        }
        return new Recipientes(todos, encaminhada, naoPermite, porRecipiente);
    }

    public boolean temProcedimentoRepetido() {
        for (List<String> procedimentos : procedimentosPorRecipiente.values()) {
            Set<String> vistos = new HashSet<>();
            for (String procedimento : procedimentos) {
                if (!vistos.add(procedimento)) {
                    return true;
                }
            }
        }
        return false;
    }

}