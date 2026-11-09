package sistema;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.RequestDispatcher;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.oracle.OracleContainer;

@Testcontainers
class CenariosLegadoTest {

    @Container
    static OracleContainer oracle = new OracleContainer("gvenzl/oracle-free:23-slim-faststart");

    @BeforeAll
    static void configurar() throws Exception {
        System.setProperty("db.url", oracle.getJdbcUrl());
        System.setProperty("db.user", oracle.getUsername());
        System.setProperty("db.password", oracle.getPassword());
        try (Connection con = conectar(); Statement st = con.createStatement()) {
            st.execute("CREATE TABLE conta (id VARCHAR2(36) PRIMARY KEY, titular VARCHAR2(100) NOT NULL, "
                    + "saldo NUMBER(15,2) NOT NULL, situacao VARCHAR2(10) NOT NULL)");
            st.execute("CREATE SEQUENCE seq_movimentacao START WITH 1 INCREMENT BY 1");
            st.execute("CREATE TABLE movimentacao (id NUMBER PRIMARY KEY, conta_id VARCHAR2(36) NOT NULL "
                    + "REFERENCES conta (id), tipo VARCHAR2(30) NOT NULL, valor NUMBER(15,2) NOT NULL, "
                    + "descricao VARCHAR2(200), data_hora TIMESTAMP NOT NULL)");
        }
    }

    static Connection conectar() throws Exception {
        return DriverManager.getConnection(oracle.getJdbcUrl(), oracle.getUsername(), oracle.getPassword());
    }

    @BeforeEach
    void preparar() throws Exception {
        try (Connection con = conectar(); Statement st = con.createStatement()) {
            st.execute("DELETE FROM movimentacao");
            st.execute("DELETE FROM conta");
            st.execute("INSERT INTO conta VALUES ('c1', 'Ana', 100.00, 'ATIVA')");
            st.execute("INSERT INTO conta VALUES ('c2', 'Bruno', 50.00, 'ATIVA')");
            st.execute("INSERT INTO conta VALUES ('c3', 'Carla', 0.00, 'ENCERRADA')");
            st.execute("INSERT INTO conta VALUES ('c4', 'Davi', 0.00, 'ATIVA')");
        }
    }

    static Map<String, Object> chamar(HttpServlet servlet, String... pares) throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        for (int i = 0; i < pares.length; i += 2) {
            params.put(pares[i], pares[i + 1]);
        }
        Map<String, Object> atributos = new HashMap<String, Object>();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        when(req.getParameter(anyString())).thenAnswer(i -> params.get(i.<String>getArgument(0)));
        when(req.getRequestDispatcher(anyString())).thenReturn(mock(RequestDispatcher.class));
        doAnswer(i -> atributos.put(i.<String>getArgument(0), i.getArgument(1)))
                .when(req).setAttribute(anyString(), org.mockito.ArgumentMatchers.any());
        if (servlet instanceof ExtratoServlet) {
            ((ExtratoServlet) servlet).doGet(req, resp);
        } else if (servlet instanceof AbrirContaServlet) {
            ((AbrirContaServlet) servlet).doPost(req, resp);
        } else if (servlet instanceof DepositoServlet) {
            ((DepositoServlet) servlet).doPost(req, resp);
        } else if (servlet instanceof SaqueServlet) {
            ((SaqueServlet) servlet).doPost(req, resp);
        } else if (servlet instanceof EncerrarContaServlet) {
            ((EncerrarContaServlet) servlet).doPost(req, resp);
        } else {
            ((TransferenciaServlet) servlet).doPost(req, resp);
        }
        return atributos;
    }

    static BigDecimal saldo(String id) throws Exception {
        try (Connection con = conectar(); PreparedStatement ps = con.prepareStatement("SELECT saldo FROM conta WHERE id = ?")) {
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getBigDecimal(1);
        }
    }

    static String situacao(String id) throws Exception {
        try (Connection con = conectar(); PreparedStatement ps = con.prepareStatement("SELECT situacao FROM conta WHERE id = ?")) {
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getString(1);
        }
    }

    static int movimentacoes(String id) throws Exception {
        try (Connection con = conectar(); PreparedStatement ps = con.prepareStatement(
                "SELECT COUNT(*) FROM movimentacao WHERE conta_id = ?")) {
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1);
        }
    }

    @Test
    void abreContaComSaldoZero() throws Exception {
        Map<String, Object> r = chamar(new AbrirContaServlet(), "titular", "Eva");
        assertThat(r).containsKey("mensagem");
        try (Connection con = conectar(); Statement st = con.createStatement()) {
            ResultSet rs = st.executeQuery("SELECT saldo, situacao FROM conta WHERE titular = 'Eva'");
            assertThat(rs.next()).isTrue();
            assertThat(rs.getBigDecimal(1)).isEqualByComparingTo("0");
            assertThat(rs.getString(2)).isEqualTo("ATIVA");
        }
    }

    @Test
    void recusaAbrirContaSemTitular() throws Exception {
        assertThat(chamar(new AbrirContaServlet(), "titular", " ")).containsKey("erro");
    }

    @Test
    void deposita() throws Exception {
        assertThat(chamar(new DepositoServlet(), "conta", "c1", "valor", "25", "descricao", "x")).containsKey("mensagem");
        assertThat(saldo("c1")).isEqualByComparingTo("125");
        assertThat(movimentacoes("c1")).isEqualTo(1);
    }

    @Test
    void recusaDepositoInvalido() throws Exception {
        assertThat(chamar(new DepositoServlet(), "conta", "c1", "valor", "0")).containsKey("erro");
        assertThat(chamar(new DepositoServlet(), "conta", "c3", "valor", "10")).containsKey("erro");
        assertThat(chamar(new DepositoServlet(), "conta", "zzz", "valor", "10")).containsKey("erro");
        assertThat(saldo("c1")).isEqualByComparingTo("100");
    }

    @Test
    void saca() throws Exception {
        assertThat(chamar(new SaqueServlet(), "conta", "c1", "valor", "40", "descricao", "x")).containsKey("mensagem");
        assertThat(saldo("c1")).isEqualByComparingTo("60");
    }

    @Test
    void recusaSaqueSemSaldoOuEmContaEncerrada() throws Exception {
        assertThat(chamar(new SaqueServlet(), "conta", "c1", "valor", "150")).containsKey("erro");
        assertThat(chamar(new SaqueServlet(), "conta", "c3", "valor", "1")).containsKey("erro");
        assertThat(saldo("c1")).isEqualByComparingTo("100");
    }

    @Test
    void encerraContaSemSaldo() throws Exception {
        assertThat(chamar(new EncerrarContaServlet(), "conta", "c4")).containsKey("mensagem");
        assertThat(situacao("c4")).isEqualTo("ENCERRADA");
    }

    @Test
    void recusaEncerrarContaComSaldoOuJaEncerrada() throws Exception {
        assertThat(chamar(new EncerrarContaServlet(), "conta", "c1")).containsKey("erro");
        assertThat(chamar(new EncerrarContaServlet(), "conta", "c3")).containsKey("erro");
        assertThat(situacao("c1")).isEqualTo("ATIVA");
    }

    @Test
    void transfere() throws Exception {
        assertThat(chamar(new TransferenciaServlet(), "origem", "c1", "destino", "c2", "valor", "50", "descricao", "x"))
                .containsKey("mensagem");
        assertThat(saldo("c1")).isEqualByComparingTo("50");
        assertThat(saldo("c2")).isEqualByComparingTo("100");
        assertThat(movimentacoes("c1")).isEqualTo(1);
        assertThat(movimentacoes("c2")).isEqualTo(1);
    }

    @Test
    void recusaTransferenciasInvalidas() throws Exception {
        assertThat(chamar(new TransferenciaServlet(), "origem", "c1", "destino", "c2", "valor", "150")).containsKey("erro");
        assertThat(chamar(new TransferenciaServlet(), "origem", "c1", "destino", "c2", "valor", "0")).containsKey("erro");
        assertThat(chamar(new TransferenciaServlet(), "origem", "c1", "destino", "c1", "valor", "10")).containsKey("erro");
        assertThat(chamar(new TransferenciaServlet(), "origem", "c1", "destino", "c3", "valor", "10")).containsKey("erro");
        assertThat(chamar(new TransferenciaServlet(), "origem", "c1", "destino", "zzz", "valor", "10")).containsKey("erro");
        assertThat(saldo("c1")).isEqualByComparingTo("100");
        assertThat(saldo("c2")).isEqualByComparingTo("50");
    }

    @Test
    @SuppressWarnings("unchecked")
    void paginaOExtratoNaOrdemDeRegistro() throws Exception {
        for (int i = 1; i <= 3; i++) {
            chamar(new DepositoServlet(), "conta", "c1", "valor", String.valueOf(i), "descricao", "m" + i);
        }
        List<String> primeira = (List<String>) chamar(new ExtratoServlet(), "conta", "c1", "pagina", "0", "tamanho", "2").get("linhas");
        List<String> segunda = (List<String>) chamar(new ExtratoServlet(), "conta", "c1", "pagina", "1", "tamanho", "2").get("linhas");
        assertThat(primeira).hasSize(2);
        assertThat(segunda).hasSize(1);
        assertThat(primeira.get(0)).contains("m1");
        assertThat(primeira.get(1)).contains("m2");
        assertThat(segunda.get(0)).contains("m3");
    }

    @Test
    void oracleGravaDescricaoVaziaComoNulo() throws Exception {
        chamar(new DepositoServlet(), "conta", "c1", "valor", "5", "descricao", "");
        try (Connection con = conectar(); Statement st = con.createStatement()) {
            ResultSet rs = st.executeQuery("SELECT descricao FROM movimentacao WHERE conta_id = 'c1'");
            rs.next();
            assertThat(rs.getString(1)).isNull();
        }
    }
}
