package procedimento.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

@Path("/health")
public class SaudeController {

    private final DataSource dataSource;

    @Inject
    public SaudeController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> verificar() {
        try (Connection conexao = dataSource.getConnection()) {
            return Map.of("postgres", conexao.isValid(2) ? "ok" : "falha");
        } catch (SQLException erro) {
            return Map.of("postgres", "falha");
        }
    }

}