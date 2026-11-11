package sistema.adaptador.jpa;

import java.sql.Connection;
import java.sql.DriverManager;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.PostgreSQLContainer;

final class Postgres {

    static final PostgreSQLContainer<?> CONTAINER = new PostgreSQLContainer<>("postgres:16-alpine")
            .withInitScript("schema-postgres.sql");

    static {
        CONTAINER.start();
        try (Connection conexao = DriverManager.getConnection(CONTAINER.getJdbcUrl(), CONTAINER.getUsername(),
                CONTAINER.getPassword())) {
            ScriptUtils.executeSqlScript(conexao, new ClassPathResource("dados-postgres.sql"));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private Postgres() {
    }

    static void configurar(DynamicPropertyRegistry registro) {
        registro.add("spring.datasource.url", CONTAINER::getJdbcUrl);
        registro.add("spring.datasource.username", CONTAINER::getUsername);
        registro.add("spring.datasource.password", CONTAINER::getPassword);
        registro.add("spring.jpa.hibernate.ddl-auto", () -> "none");
    }

}