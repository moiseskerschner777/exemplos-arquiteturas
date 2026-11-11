package sistema.adaptador.jpa;

import java.util.Optional;
import sistema.core.dominio.Conta;
import sistema.core.dominio.SituacaoConta;
import sistema.core.porta.out.ContaPortOut;

public class ContaRepositoryJpaPostgres implements ContaPortOut {

    private final ContaJpaRepository repositorio;

    public ContaRepositoryJpaPostgres(ContaJpaRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public Optional<Conta> buscarParaAtualizar(String id) {
        return repositorio.buscarParaAtualizar(id).map(c -> Conta.reconstituir(c.getId(), c.getTitular(), c.getSaldo(),
                SituacaoConta.valueOf(c.getSituacao())));
    }

    @Override
    public void salvar(Conta conta) {
        repositorio.save(new ContaJpa(conta.getId(), conta.getTitular(), conta.getSaldo(), conta.getSituacao().name()));
    }

}