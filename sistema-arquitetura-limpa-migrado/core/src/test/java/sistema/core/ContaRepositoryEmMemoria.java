package sistema.core;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import sistema.core.dominio.Conta;
import sistema.core.porta.out.ContaPortOut;

public class ContaRepositoryEmMemoria implements ContaPortOut, Restauravel {

    private final Map<String, Conta> contas = new LinkedHashMap<String, Conta>();

    @Override
    public Optional<Conta> buscarParaAtualizar(String id) {
        Conta conta = contas.get(id);
        if (conta == null) {
            return Optional.empty();
        }
        return Optional.of(copiar(conta));
    }

    @Override
    public void salvar(Conta conta) {
        contas.put(conta.getId(), copiar(conta));
    }

    @Override
    public Runnable pontoDeRestauracao() {
        Map<String, Conta> copia = new LinkedHashMap<String, Conta>(contas);
        return () -> {
            contas.clear();
            contas.putAll(copia);
        };
    }

    private static Conta copiar(Conta conta) {
        return Conta.reconstituir(conta.getId(), conta.getTitular(), conta.getSaldo(), conta.getSituacao());
    }

    public Conta buscar(String id) {
        return contas.get(id);
    }

    public int quantidade() {
        return contas.size();
    }

}