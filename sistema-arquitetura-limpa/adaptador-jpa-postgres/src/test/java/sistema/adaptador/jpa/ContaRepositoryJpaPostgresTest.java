package sistema.adaptador.jpa;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import sistema.core.contrato.ContaPortOutContrato;
import sistema.core.dominio.SituacaoConta;
import sistema.core.porta.out.ContaPortOut;
import sistema.core.porta.out.TransacaoPortOut;

@SpringBootTest(classes = ConfiguracaoDeTeste.class)
class ContaRepositoryJpaPostgresTest extends ContaPortOutContrato {

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
    protected ContaPortOut contas() {
        return new ContaRepositoryJpaPostgres(repositorio);
    }

    @Override
    protected TransacaoPortOut transacao() {
        return new TransacaoRepositorySpring(gerenciador);
    }

    @Override
    protected LinhaDeConta ler(String id) {
        return jdbc.queryForObject("SELECT titular, saldo, situacao FROM conta WHERE id = ?",
                (rs, n) -> new LinhaDeConta(rs.getString(1), rs.getBigDecimal(2), SituacaoConta.valueOf(rs.getString(3))), id);
    }

    @Override
    protected int quantidadeComId(String id) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM conta WHERE id = ?", Integer.class, id);
    }

}