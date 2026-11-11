package sistema.adaptador.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import oracle.jdbc.pool.OracleDataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.oracle.OracleContainer;
import sistema.core.contrato.MovimentacaoPortOutContrato;
import sistema.core.dominio.Movimentacao;
import sistema.core.dominio.TipoMovimentacao;
import sistema.core.porta.out.MovimentacaoPortOut;
import sistema.core.porta.out.TransacaoPortOut;

@Testcontainers
class MovimentacaoRepositoryJdbcOracleTest extends MovimentacaoPortOutContrato {

    @Container
    static OracleContainer oracle = new OracleContainer("gvenzl/oracle-free:23-slim-faststart")
            .withInitScript("schema.sql");

    private static TransacaoPortOut transacao;

    @BeforeAll
    static void configurar() throws Exception {
        OracleDataSource dataSource = new OracleDataSource();
        dataSource.setURL(oracle.getJdbcUrl());
        dataSource.setUser(oracle.getUsername());
        dataSource.setPassword(oracle.getPassword());
        transacao = new TransacaoRepositoryJdbc(dataSource);
    }

    @BeforeEach
    void limpar() throws Exception {
        try (Connection con = conectar(); PreparedStatement ps = con.prepareStatement("DELETE FROM movimentacao")) {
            ps.executeUpdate();
        }
    }

    static Connection conectar() throws Exception {
        return DriverManager.getConnection(oracle.getJdbcUrl(), oracle.getUsername(), oracle.getPassword());
    }

    @Override
    protected MovimentacaoPortOut movimentacoes() {
        return new MovimentacaoRepositoryJdbcOracle();
    }

    @Override
    protected TransacaoPortOut transacao() {
        return transacao;
    }

    @Override
    protected List<Movimentacao> lerPorConta(String contaId) {
        try (Connection con = conectar();
                PreparedStatement ps = con.prepareStatement(
                        "SELECT id, tipo, valor, descricao, data_hora FROM movimentacao WHERE conta_id = ? ORDER BY id")) {
            ps.setString(1, contaId);
            List<Movimentacao> lidas = new ArrayList<Movimentacao>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lidas.add(Movimentacao.reconstituir(rs.getLong(1), contaId, TipoMovimentacao.valueOf(rs.getString(2)),
                            rs.getBigDecimal(3), rs.getString(4), rs.getTimestamp(5).toLocalDateTime()));
                }
            }
            return lidas;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

}