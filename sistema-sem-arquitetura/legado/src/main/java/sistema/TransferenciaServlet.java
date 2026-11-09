package sistema;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/transferir")
public class TransferenciaServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/transferencia.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String origemId = req.getParameter("origem");
        String destinoId = req.getParameter("destino");
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
        if (origemId == null || origemId.equals(destinoId)) {
            erro(req, resp, "origem deve ser diferente do destino");
            return;
        }
        Connection con = null;
        try {
            con = DriverManager.getConnection(
                    System.getProperty("db.url", System.getenv("DB_URL")),
                    System.getProperty("db.user", System.getenv("DB_USER")),
                    System.getProperty("db.password", System.getenv("DB_PASSWORD")));
            con.setAutoCommit(false);

            PreparedStatement busca = con.prepareStatement(
                    "SELECT saldo, situacao FROM conta WHERE id = ? FOR UPDATE");
            busca.setString(1, origemId);
            ResultSet rsOrigem = busca.executeQuery();
            if (!rsOrigem.next()) {
                con.rollback();
                erro(req, resp, "conta de origem nao encontrada");
                return;
            }
            BigDecimal saldoOrigem = rsOrigem.getBigDecimal("saldo");
            String situacaoOrigem = rsOrigem.getString("situacao");
            rsOrigem.close();

            busca.setString(1, destinoId);
            ResultSet rsDestino = busca.executeQuery();
            if (!rsDestino.next()) {
                con.rollback();
                erro(req, resp, "conta de destino nao encontrada");
                return;
            }
            BigDecimal saldoDestino = rsDestino.getBigDecimal("saldo");
            String situacaoDestino = rsDestino.getString("situacao");
            rsDestino.close();
            busca.close();

            if ("ENCERRADA".equals(situacaoOrigem) || "ENCERRADA".equals(situacaoDestino)) {
                con.rollback();
                erro(req, resp, "conta encerrada");
                return;
            }
            if (saldoOrigem.compareTo(valor) < 0) {
                con.rollback();
                erro(req, resp, "saldo insuficiente");
                return;
            }

            PreparedStatement atualiza = con.prepareStatement("UPDATE conta SET saldo = ? WHERE id = ?");
            atualiza.setBigDecimal(1, saldoOrigem.subtract(valor));
            atualiza.setString(2, origemId);
            atualiza.executeUpdate();
            atualiza.setBigDecimal(1, saldoDestino.add(valor));
            atualiza.setString(2, destinoId);
            atualiza.executeUpdate();
            atualiza.close();

            PreparedStatement insere = con.prepareStatement(
                    "INSERT INTO movimentacao (id, conta_id, tipo, valor, descricao, data_hora) "
                            + "VALUES (seq_movimentacao.NEXTVAL, ?, ?, ?, ?, SYSTIMESTAMP)");
            insere.setString(1, origemId);
            insere.setString(2, "TRANSFERENCIA_ENVIADA");
            insere.setBigDecimal(3, valor);
            insere.setString(4, descricao);
            insere.executeUpdate();
            insere.setString(1, destinoId);
            insere.setString(2, "TRANSFERENCIA_RECEBIDA");
            insere.setBigDecimal(3, valor);
            insere.setString(4, descricao);
            insere.executeUpdate();
            insere.close();

            con.commit();
            req.setAttribute("mensagem", "transferencia realizada");
            req.getRequestDispatcher("/transferencia.jsp").forward(req, resp);
        } catch (SQLException e) {
            try {
                if (con != null) {
                    con.rollback();
                }
            } catch (SQLException ignorada) {
                ignorada.printStackTrace();
            }
            erro(req, resp, "erro ao transferir");
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
        req.getRequestDispatcher("/transferencia.jsp").forward(req, resp);
    }
}
