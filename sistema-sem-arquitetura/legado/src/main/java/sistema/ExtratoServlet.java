package sistema;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/extrato")
public class ExtratoServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String contaId = req.getParameter("conta");
        if (contaId == null) {
            req.getRequestDispatcher("/extrato.jsp").forward(req, resp);
            return;
        }
        int pagina;
        int tamanho;
        try {
            pagina = Integer.parseInt(req.getParameter("pagina"));
            tamanho = Integer.parseInt(req.getParameter("tamanho"));
        } catch (RuntimeException e) {
            erro(req, resp, "pagina e tamanho invalidos");
            return;
        }
        if (pagina < 0 || tamanho <= 0) {
            erro(req, resp, "pagina e tamanho invalidos");
            return;
        }
        try (Connection con = abrir()) {
            PreparedStatement consulta = con.prepareStatement(
                    "SELECT * FROM (SELECT m.*, ROWNUM rn FROM ("
                            + "SELECT id, tipo, valor, descricao FROM movimentacao WHERE conta_id = ? ORDER BY id) m "
                            + "WHERE ROWNUM <= ?) WHERE rn > ?");
            consulta.setString(1, contaId);
            consulta.setInt(2, (pagina + 1) * tamanho);
            consulta.setInt(3, pagina * tamanho);
            ResultSet rs = consulta.executeQuery();
            List<String> linhas = new ArrayList<String>();
            while (rs.next()) {
                linhas.add(rs.getLong("id") + " " + rs.getString("tipo") + " " + rs.getBigDecimal("valor")
                        + " " + rs.getString("descricao"));
            }
            rs.close();
            consulta.close();
            req.setAttribute("linhas", linhas);
            req.getRequestDispatcher("/extrato.jsp").forward(req, resp);
        } catch (SQLException e) {
            erro(req, resp, "erro ao consultar");
        }
    }

    private void erro(HttpServletRequest req, HttpServletResponse resp, String mensagem)
            throws ServletException, IOException {
        req.setAttribute("erro", mensagem);
        req.getRequestDispatcher("/extrato.jsp").forward(req, resp);
    }

    private Connection abrir() throws SQLException {
        return DriverManager.getConnection(
                System.getProperty("db.url", System.getenv("DB_URL")),
                System.getProperty("db.user", System.getenv("DB_USER")),
                System.getProperty("db.password", System.getenv("DB_PASSWORD")));
    }
}
