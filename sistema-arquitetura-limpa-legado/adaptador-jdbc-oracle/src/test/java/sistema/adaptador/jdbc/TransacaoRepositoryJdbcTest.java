package sistema.adaptador.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import oracle.jdbc.pool.OracleDataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.oracle.OracleContainer;
import sistema.core.contrato.TransacaoPortOutContrato;
import sistema.core.porta.out.ContaPortOut;
import sistema.core.porta.out.TransacaoPortOut;

@Testcontainers
class TransacaoRepositoryJdbcTest extends TransacaoPortOutContrato {

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
        try (Connection con = conectar(); PreparedStatement ps = con.prepareStatement("DELETE FROM conta WHERE id = 'c9'")) {
            ps.executeUpdate();
        }
    }

    static Connection conectar() throws Exception {
        return DriverManager.getConnection(oracle.getJdbcUrl(), oracle.getUsername(), oracle.getPassword());
    }

    @Override
    protected TransacaoPortOut transacao() {
        return transacao;
    }

    @Override
    protected ContaPortOut contas() {
        return new ContaRepositoryJdbcOracle();
    }

    @Override
    protected int quantidadeComId(String id) {
        try (Connection con = conectar();
                PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM conta WHERE id = ?")) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

}