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

@Testcontainers
class AbrirContaServletTest {

    @Container
    static OracleContainer oracle = new OracleContainer("gvenzl/oracle-free:23-slim-faststart")
            .withInitScript("schema.sql");

    private static AbrirContaServlet servlet;

    @BeforeAll
    static void configurar() throws Exception {
        System.setProperty("db.url", oracle.getJdbcUrl());
        System.setProperty("db.user", oracle.getUsername());
        System.setProperty("db.password", oracle.getPassword());
        Map<String, Object> contexto = new HashMap<String, Object>();
        ServletContext servletContext = mock(ServletContext.class);
        doAnswer(i -> contexto.put(i.<String>getArgument(0), i.getArgument(1)))
                .when(servletContext).setAttribute(anyString(), any());
        ServletContextEvent evento = new ServletContextEvent(servletContext);
        new Montagem().contextInitialized(evento);
        servlet = new AbrirContaServlet((AbrirContaPortIn) contexto.get(AbrirContaPortIn.class.getName()));
    }

    static Map<String, Object> post(String titular) throws Exception {
        Map<String, Object> atributos = new HashMap<String, Object>();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        when(req.getParameter("titular")).thenReturn(titular);
        when(req.getRequestDispatcher(anyString())).thenReturn(mock(RequestDispatcher.class));
        doAnswer(i -> atributos.put(i.<String>getArgument(0), i.getArgument(1)))
                .when(req).setAttribute(anyString(), any());
        servlet.doPost(req, resp);
        return atributos;
    }

    @Test
    void abreContaComTitularEConsultaSaldoZero() throws Exception {
        Map<String, Object> resultado = post("Fulano");

        String mensagem = (String) resultado.get("mensagem");
        assertThat(mensagem).startsWith("conta aberta: ");
        String id = mensagem.substring("conta aberta: ".length());
        try (Connection con = DriverManager.getConnection(oracle.getJdbcUrl(), oracle.getUsername(), oracle.getPassword());
                PreparedStatement ps = con.prepareStatement("SELECT titular, saldo, situacao FROM conta WHERE id = ?")) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                assertThat(rs.next()).isTrue();
                assertThat(rs.getString(1)).isEqualTo("Fulano");
                assertThat(rs.getBigDecimal(2)).isEqualByComparingTo(BigDecimal.ZERO);
                assertThat(rs.getString(3)).isEqualTo("ATIVA");
            }
        }
    }

    @Test
    void tituloVazioMostraTitularObrigatorio() throws Exception {
        Map<String, Object> resultado = post("  ");

        assertThat(resultado.get("erro")).isEqualTo("titular obrigatorio");
        assertThat(resultado).doesNotContainKey("mensagem");
    }

}