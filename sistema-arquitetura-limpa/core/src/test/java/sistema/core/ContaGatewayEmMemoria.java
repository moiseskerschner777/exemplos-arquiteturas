package sistema.core;

import java.util.LinkedHashMap;
import java.util.Map;
import sistema.core.dominio.Conta;
import sistema.core.porta.out.ContaPortOut;

public class ContaGatewayEmMemoria implements ContaPortOut {

    private final Map<String, Conta> contas = new LinkedHashMap<String, Conta>();

    @Override
    public void salvar(Conta conta) {
        contas.put(conta.getId(), conta);
    }

    public Conta buscar(String id) {
        return contas.get(id);
    }

    public int quantidade() {
        return contas.size();
    }

}