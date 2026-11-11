package sistema.web.spring;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.oracle.OracleContainer;

abstract class OracleNoContainer {

    static final OracleContainer ORACLE = new OracleContainer("gvenzl/oracle-free:23-slim-faststart")
            .withInitScript("schema.sql");

    static {
        ORACLE.start();
    }

    @DynamicPropertySource
    static void banco(DynamicPropertyRegistry registro) {
        registro.add("db.url", ORACLE::getJdbcUrl);
        registro.add("db.user", ORACLE::getUsername);
        registro.add("db.password", ORACLE::getPassword);
    }

}