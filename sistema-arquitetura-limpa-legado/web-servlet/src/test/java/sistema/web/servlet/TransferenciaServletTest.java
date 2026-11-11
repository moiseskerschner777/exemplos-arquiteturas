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
import sistema.core.porta.in.TransferirPortIn;

@Testcontainers
class TransferenciaServletTest {

    @Container
    static OracleContainer oracle = new OracleContainer("gvenzl/oracle-free:23-slim-faststart")
            .withInitScript("schema.sql");

    private static TransferenciaServlet servlet;

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
        servlet = new TransferenciaServlet((TransferirPortIn) contexto.get(TransferirPortIn.class.getName()));
    }

    static Map<String, Object> post(String origem, String destino, String valor) throws Exception {
        Map<String, Object> atributos = new HashMap<String, Object>();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        when(req.getParameter("origem")).thenReturn(origem);
        when(req.getParameter("destino")).thenReturn(destino);
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
    void transfere50DeC1ParaC2() throws Exception {
        BigDecimal origemAntes = saldo("c1");
        BigDecimal destinoAntes = saldo("c2");

        Map<String, Object> resultado = post("c1", "c2", "50");

        assertThat(resultado.get("mensagem")).isEqualTo("transferencia realizada");
        assertThat(saldo("c1")).isEqualByComparingTo(origemAntes.subtract(new BigDecimal("50")));
        assertThat(saldo("c2")).isEqualByComparingTo(destinoAntes.add(new BigDecimal("50")));
    }

    @Test
    void destinoInexistenteMostraContaDeDestinoNaoEncontrada() throws Exception {
        BigDecimal antes = saldo("c1");

        Map<String, Object> resultado = post("c1", "zzz", "10");

        assertThat(resultado.get("erro")).isEqualTo("conta de destino nao encontrada");
        assertThat(saldo("c1")).isEqualByComparingTo(antes);
    }

}