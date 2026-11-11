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
import sistema.core.porta.in.DepositarPortIn;

@Testcontainers
class DepositoServletTest {

    @Container
    static OracleContainer oracle = new OracleContainer("gvenzl/oracle-free:23-slim-faststart")
            .withInitScript("schema.sql");

    private static DepositoServlet servlet;

    @BeforeAll
    static void configurar() {
        System.setProperty("db.url", oracle.getJdbcUrl());
        System.setProperty("db.user", oracle.getUsername());
        System.setProperty("db.password", oracle.getPassword());
        Map<String, Object> contexto = new HashMap<String, Object>();
        ServletContext servletContext = mock(ServletContext.class);
        doAnswer(i -> contexto.put(i.<String>getArgument(0), i.getArgument(1)))
                .when(servletContext).setAttribute(anyString(), any());
        new Montagem().contextInitialized(new ServletContextEvent(servletContext));
        servlet = new DepositoServlet((DepositarPortIn) contexto.get(DepositarPortIn.class.getName()));
    }

    static Map<String, Object> post(String conta, String valor) throws Exception {
        Map<String, Object> atributos = new HashMap<String, Object>();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        when(req.getParameter("conta")).thenReturn(conta);
        when(req.getParameter("valor")).thenReturn(valor);
        when(req.getParameter("descricao")).thenReturn("x");
        when(req.getRequestDispatcher(anyString())).thenReturn(mock(RequestDispatcher.class));
        doAnswer(i -> atributos.put(i.<String>getArgument(0), i.getArgument(1)))
                .when(req).setAttribute(anyString(), any());
        servlet.doPost(req, resp);
        return atributos;
    }

    static BigDecimal saldo(String id) throws Exception {
        try (Connection con = DriverManager.getConnection(oracle.getJdbcUrl(), oracle.getUsername(), oracle.getPassword());
                PreparedStatement ps = con.prepareStatement("SELECT saldo FROM conta WHERE id = ?")) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getBigDecimal(1);
            }
        }
    }

    @Test
    void depositaNaContaC1EOSaldoAumenta() throws Exception {
        BigDecimal antes = saldo("c1");

        Map<String, Object> resultado = post("c1", "25");

        assertThat(resultado.get("mensagem")).isEqualTo("operacao realizada");
        assertThat(saldo("c1")).isEqualByComparingTo(antes.add(new BigDecimal("25")));
    }

    @Test
    void valorZeroMostraValorDeveSerMaiorQueZero() throws Exception {
        BigDecimal antes = saldo("c1");

        Map<String, Object> resultado = post("c1", "0");

        assertThat(resultado.get("erro")).isEqualTo("valor deve ser maior que zero");
        assertThat(saldo("c1")).isEqualByComparingTo(antes);
    }

}