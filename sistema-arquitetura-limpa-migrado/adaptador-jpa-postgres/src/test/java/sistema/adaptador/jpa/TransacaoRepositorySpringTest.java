package sistema.adaptador.jpa;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import sistema.core.contrato.TransacaoPortOutContrato;
import sistema.core.porta.out.ContaPortOut;
import sistema.core.porta.out.TransacaoPortOut;

@SpringBootTest(classes = ConfiguracaoDeTeste.class)
class TransacaoRepositorySpringTest extends TransacaoPortOutContrato {

    @DynamicPropertySource
    static void banco(DynamicPropertyRegistry registro) {
        Postgres.configurar(registro);
    }

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private ContaJpaRepository repositorio;

    @Autowired
    private PlatformTransactionManager gerenciador;

    @BeforeEach
    void limpar() {
        jdbc.update("DELETE FROM conta WHERE id = 'c9'");
    }

    @Override
    protected TransacaoPortOut transacao() {
        return new TransacaoRepositorySpring(gerenciador);
    }

    @Override
    protected ContaPortOut contas() {
        return new ContaRepositoryJpaPostgres(repositorio);
    }

    @Override
    protected int quantidadeComId(String id) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM conta WHERE id = ?", Integer.class, id);
    }

}