package sistema.web.spring;

import java.sql.Connection;
import java.sql.DriverManager;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

abstract class PostgresNoContainer {

    static final PostgreSQLContainer<?> BANCO = new PostgreSQLContainer<>("postgres:16-alpine")
            .withInitScript("schema-postgres.sql");

    static {
        BANCO.start();
        try (Connection conexao = DriverManager.getConnection(BANCO.getJdbcUrl(), BANCO.getUsername(),
                BANCO.getPassword())) {
            ScriptUtils.executeSqlScript(conexao, new ClassPathResource("dados-postgres.sql"));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @DynamicPropertySource
    static void banco(DynamicPropertyRegistry registro) {
        registro.add("spring.datasource.url", BANCO::getJdbcUrl);
        registro.add("spring.datasource.username", BANCO::getUsername);
        registro.add("spring.datasource.password", BANCO::getPassword);
    }

}