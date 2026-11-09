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

@WebServlet("/abrir-conta")
public class AbrirContaServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/abrir-conta.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String titular = req.getParameter("titular");
        if (titular == null || titular.trim().isEmpty()) {
            erro(req, resp, "titular obrigatorio");
            return;
        }
        Connection con = null;
        try {
            con = abrir();
            con.setAutoCommit(false);
            String id = UUID.randomUUID().toString();
            PreparedStatement insere = con.prepareStatement(
                    "INSERT INTO conta (id, titular, saldo, situacao) VALUES (?, ?, 0, 'ATIVA')");
            insere.setString(1, id);
            insere.setString(2, titular.trim());
            insere.executeUpdate();
            insere.close();
            con.commit();
            req.setAttribute("mensagem", "conta aberta: " + id);
            req.getRequestDispatcher("/abrir-conta.jsp").forward(req, resp);
        } catch (SQLException e) {
            try {
                if (con != null) {
                    con.rollback();
                }
            } catch (SQLException ignorada) {
                ignorada.printStackTrace();
            }
            erro(req, resp, "erro ao processar");
        } finally {
            try {
                if (con != null) {
                    con.close();
                }
            } catch (SQLException ignorada) {
                ignorada.printStackTrace();
            }
        }
    }

    private void erro(HttpServletRequest req, HttpServletResponse resp, String mensagem)
            throws ServletException, IOException {
        req.setAttribute("erro", mensagem);
        req.getRequestDispatcher("/abrir-conta.jsp").forward(req, resp);
    }

    private Connection abrir() throws SQLException {
        return DriverManager.getConnection(
                System.getProperty("db.url", System.getenv("DB_URL")),
                System.getProperty("db.user", System.getenv("DB_USER")),
                System.getProperty("db.password", System.getenv("DB_PASSWORD")));
    }
}
