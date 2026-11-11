package sistema.web.spring;

import java.sql.Connection;
import java.sql.DriverManager;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.oracle.OracleContainer;

abstract class OracleNoContainer {

    static final boolean POSTGRES = "postgres".equals(System.getProperty("spring.profiles.active"));

    static final JdbcDatabaseContainer<?> BANCO = POSTGRES
            ? new PostgreSQLContainer<>("postgres:16-alpine").withInitScript("schema-postgres.sql")
            : new OracleContainer("gvenzl/oracle-free:23-slim-faststart").withInitScript("schema.sql");

    static {
        BANCO.start();
        if (POSTGRES) {
            try (Connection conexao = DriverManager.getConnection(BANCO.getJdbcUrl(), BANCO.getUsername(),
                    BANCO.getPassword())) {
                ScriptUtils.executeSqlScript(conexao, new ClassPathResource("dados-postgres.sql"));
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
    }

    @DynamicPropertySource
    static void banco(DynamicPropertyRegistry registro) {
        String prefixo = POSTGRES ? "spring.datasource." : "db.";
        registro.add(prefixo + "url", BANCO::getJdbcUrl);
        registro.add(prefixo + (POSTGRES ? "username" : "user"), BANCO::getUsername);
        registro.add(prefixo + "password", BANCO::getPassword);
    }

}