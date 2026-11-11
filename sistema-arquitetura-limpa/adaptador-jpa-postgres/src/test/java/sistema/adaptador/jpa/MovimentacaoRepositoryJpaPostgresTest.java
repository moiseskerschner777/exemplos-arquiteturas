package sistema.adaptador.jpa;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import sistema.core.contrato.MovimentacaoPortOutContrato;
import sistema.core.dominio.Movimentacao;
import sistema.core.dominio.TipoMovimentacao;
import sistema.core.porta.out.MovimentacaoPortOut;
import sistema.core.porta.out.TransacaoPortOut;

@SpringBootTest(classes = ConfiguracaoDeTeste.class)
class MovimentacaoRepositoryJpaPostgresTest extends MovimentacaoPortOutContrato {

    @DynamicPropertySource
    static void banco(DynamicPropertyRegistry registro) {
        Postgres.configurar(registro);
    }

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private MovimentacaoJpaRepository repositorio;

    @Autowired
    private PlatformTransactionManager gerenciador;

    @BeforeEach
    void limpar() {
        jdbc.update("DELETE FROM movimentacao");
    }

    @Override
    protected MovimentacaoPortOut movimentacoes() {
        return new MovimentacaoRepositoryJpaPostgres(repositorio);
    }

    @Override
    protected TransacaoPortOut transacao() {
        return new TransacaoRepositorySpring(gerenciador);
    }

    @Override
    protected List<Movimentacao> lerPorConta(String contaId) {
        return jdbc.query("SELECT id, tipo, valor, descricao, data_hora FROM movimentacao WHERE conta_id = ? ORDER BY id",
                (rs, n) -> Movimentacao.reconstituir(rs.getLong(1), contaId, TipoMovimentacao.valueOf(rs.getString(2)),
                        rs.getBigDecimal(3), rs.getString(4), rs.getTimestamp(5).toLocalDateTime()), contaId);
    }

}