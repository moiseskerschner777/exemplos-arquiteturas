package procedimento.adaptador.postgres;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class PostgresDisponivelTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @Test
    void conectaNoPostgresDoTeste() throws Exception {
        try (Connection conexao = DriverManager.getConnection(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
             ResultSet resultado = conexao.createStatement().executeQuery("select 1")) {
            assertThat(resultado.next()).isTrue();
            assertThat(resultado.getInt(1)).isEqualTo(1);
        }
    }

}