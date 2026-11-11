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
import sistema.core.dominio.Movimentacao;
import sistema.core.dominio.Pagina;
import sistema.core.porta.in.AbrirContaPortIn;
import sistema.core.porta.in.ConsultarExtratoPortIn;
import sistema.core.porta.in.DepositarPortIn;

@Testcontainers
class ExtratoServletTest {

    @Container
    static OracleContainer oracle = new OracleContainer("gvenzl/oracle-free:23-slim-faststart")
            .withInitScript("schema.sql");

    private static Map<String, Object> contexto;
    private static ExtratoServlet servlet;

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
        servlet = new ExtratoServlet((ConsultarExtratoPortIn) contexto.get(ConsultarExtratoPortIn.class.getName()));
    }

    static Map<String, Object> post(String conta, String pagina, String tamanho) throws Exception {
        Map<String, Object> atributos = new HashMap<String, Object>();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        when(req.getParameter("conta")).thenReturn(conta);
        when(req.getParameter("pagina")).thenReturn(pagina);
        when(req.getParameter("tamanho")).thenReturn(tamanho);
        when(req.getRequestDispatcher(anyString())).thenReturn(mock(RequestDispatcher.class));
        doAnswer(i -> atributos.put(i.<String>getArgument(0), i.getArgument(1)))
                .when(req).setAttribute(anyString(), any());
        servlet.doPost(req, resp);
        return atributos;
    }

    @Test
    @SuppressWarnings("unchecked")
    void extratoComTresMovimentacoes() throws Exception {
        String id = ((AbrirContaPortIn) contexto.get(AbrirContaPortIn.class.getName())).executar("Eva");
        DepositarPortIn depositar = (DepositarPortIn) contexto.get(DepositarPortIn.class.getName());
        depositar.executar(id, new BigDecimal("1"), "m1");
        depositar.executar(id, new BigDecimal("2"), "m2");
        depositar.executar(id, new BigDecimal("3"), "m3");

        Pagina<Movimentacao> primeira = (Pagina<Movimentacao>) post(id, "0", "2").get("extrato");
        Pagina<Movimentacao> segunda = (Pagina<Movimentacao>) post(id, "1", "2").get("extrato");

        assertThat(primeira.getItens()).extracting(Movimentacao::getDescricao).containsExactly("m1", "m2");
        assertThat(segunda.getItens()).extracting(Movimentacao::getDescricao).containsExactly("m3");
        assertThat(primeira.getTotal()).isEqualTo(3);
    }

    @Test
    void paginaNegativaMostraErro() throws Exception {
        Map<String, Object> resultado = post("c1", "-1", "2");

        assertThat(resultado.get("erro")).isEqualTo("pagina e tamanho invalidos");
    }

}