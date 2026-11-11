package procedimento.rest;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SaudeController {

    private final DataSource dataSource;

    public SaudeController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/health")
    public Map<String, String> verificar() {
        try (Connection conexao = dataSource.getConnection()) {
            return Map.of("postgres", conexao.isValid(2) ? "ok" : "falha");
        } catch (SQLException erro) {
            return Map.of("postgres", "falha");
        }
    }

}