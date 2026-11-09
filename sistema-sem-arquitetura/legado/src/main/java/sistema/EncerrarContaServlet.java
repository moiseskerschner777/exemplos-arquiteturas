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

@WebServlet("/encerrar-conta")
public class EncerrarContaServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/encerrar-conta.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String contaId = req.getParameter("conta");
        Connection con = null;
        try {
            con = abrir();
            con.setAutoCommit(false);
            PreparedStatement busca = con.prepareStatement(
                    "SELECT saldo, situacao FROM conta WHERE id = ? FOR UPDATE");
            busca.setString(1, contaId);
            ResultSet rs = busca.executeQuery();
            if (!rs.next()) {
                con.rollback();
                erro(req, resp, "conta nao encontrada");
                return;
            }
            BigDecimal saldo = rs.getBigDecimal("saldo");
            String situacao = rs.getString("situacao");
            rs.close();
            busca.close();
            if ("ENCERRADA".equals(situacao)) {
                con.rollback();
                erro(req, resp, "conta ja encerrada");
                return;
            }
            if (saldo.compareTo(BigDecimal.ZERO) != 0) {
                con.rollback();
                erro(req, resp, "conta com saldo nao pode ser encerrada");
                return;
            }
            PreparedStatement atualiza = con.prepareStatement("UPDATE conta SET situacao = 'ENCERRADA' WHERE id = ?");
            atualiza.setString(1, contaId);
            atualiza.executeUpdate();
            atualiza.close();
            con.commit();
            req.setAttribute("mensagem", "conta encerrada");
            req.getRequestDispatcher("/encerrar-conta.jsp").forward(req, resp);
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
        req.getRequestDispatcher("/encerrar-conta.jsp").forward(req, resp);
    }

    private Connection abrir() throws SQLException {
        return DriverManager.getConnection(
                System.getProperty("db.url", System.getenv("DB_URL")),
                System.getProperty("db.user", System.getenv("DB_USER")),
                System.getProperty("db.password", System.getenv("DB_PASSWORD")));
    }
}
