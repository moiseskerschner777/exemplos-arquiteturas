package sistema.web.servlet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.oracle.OracleContainer;
import sistema.core.porta.in.AbrirContaPortIn;
import sistema.core.porta.in.EncerrarContaPortIn;

@Testcontainers
class EncerrarContaServletTest {

    @Container
    static OracleContainer oracle = new OracleContainer("gvenzl/oracle-free:23-slim-faststart")
            .withInitScript("schema.sql");

    private static Map<String, Object> contexto;
    private static EncerrarContaServlet servlet;

    @BeforeAll
    static void configurar() {
        System.setProperty("db.url", oracle.getJdbcUrl());
        System.setProperty("db.user", oracle.getUsername());
        System.setProperty("db.password", oracle.getPassword());
        contexto = new HashMap<String, Object>();
        ServletContext servletContext = mock(ServletContext.class);
        doAnswer(i -> contexto.put(i.<String>getArgument(0), i.getArgument(1)))
                .when(servletContext).setAttribute(anyString(), any());
        new Montagem().contextInitialized(new ServletContextEvent(servletContext));
        servlet = new EncerrarContaServlet((EncerrarContaPortIn) contexto.get(EncerrarContaPortIn.class.getName()));
    }

    static Map<String, Object> post(String conta) throws Exception {
        Map<String, Object> atributos = new HashMap<String, Object>();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        when(req.getParameter("conta")).thenReturn(conta);
        when(req.getRequestDispatcher(anyString())).thenReturn(mock(RequestDispatcher.class));
        doAnswer(i -> atributos.put(i.<String>getArgument(0), i.getArgument(1)))
                .when(req).setAttribute(anyString(), any());
        servlet.doPost(req, resp);
        return atributos;
    }

    static String situacao(String id) throws Exception {
        try (Connection con = DriverManager.getConnection(oracle.getJdbcUrl(), oracle.getUsername(), oracle.getPassword());
                PreparedStatement ps = con.prepareStatement("SELECT situacao FROM conta WHERE id = ?")) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getString(1);
            }
        }
    }

    @Test
    void encerraContaSemSaldo() throws Exception {
        String id = ((AbrirContaPortIn) contexto.get(AbrirContaPortIn.class.getName())).executar("Davi");

        Map<String, Object> resultado = post(id);

        assertThat(resultado.get("mensagem")).isEqualTo("conta encerrada");
        assertThat(situacao(id)).isEqualTo("ENCERRADA");
    }

    @Test
    void contaComSaldoMostraErro() throws Exception {
        Map<String, Object> resultado = post("c1");

        assertThat(resultado.get("erro")).isEqualTo("conta com saldo nao pode ser encerrada");
        assertThat(situacao("c1")).isEqualTo("ATIVA");
    }

}