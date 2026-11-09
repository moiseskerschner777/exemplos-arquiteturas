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

@WebServlet("/depositar")
public class DepositoServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/deposito.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String contaId = req.getParameter("conta");
        String descricao = req.getParameter("descricao");
        BigDecimal valor;
        try {
            valor = new BigDecimal(req.getParameter("valor"));
        } catch (RuntimeException e) {
            erro(req, resp, "valor invalido");
            return;
        }
        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            erro(req, resp, "valor deve ser maior que zero");
            return;
        }
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
                erro(req, resp, "conta encerrada");
                return;
            }
            PreparedStatement atualiza = con.prepareStatement("UPDATE conta SET saldo = ? WHERE id = ?");
            atualiza.setBigDecimal(1, saldo.add(valor));
            atualiza.setString(2, contaId);
            atualiza.executeUpdate();
            atualiza.close();
            PreparedStatement insere = con.prepareStatement(
                    "INSERT INTO movimentacao (id, conta_id, tipo, valor, descricao, data_hora) "
                            + "VALUES (seq_movimentacao.NEXTVAL, ?, ?, ?, ?, SYSTIMESTAMP)");
            insere.setString(1, contaId);
            insere.setString(2, "DEPOSITO");
            insere.setBigDecimal(3, valor);
            insere.setString(4, descricao);
            insere.executeUpdate();
            insere.close();
            con.commit();
            req.setAttribute("mensagem", "operacao realizada");
            req.getRequestDispatcher("/deposito.jsp").forward(req, resp);
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
        req.getRequestDispatcher("/deposito.jsp").forward(req, resp);
    }

    private Connection abrir() throws SQLException {
        return DriverManager.getConnection(
                System.getProperty("db.url", System.getenv("DB_URL")),
                System.getProperty("db.user", System.getenv("DB_USER")),
                System.getProperty("db.password", System.getenv("DB_PASSWORD")));
    }
}
