package sistema.web.servlet;

import java.io.IOException;
import java.math.BigDecimal;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sistema.core.dominio.ContaNaoEncontradaException;
import sistema.core.dominio.RegraVioladaException;
import sistema.core.porta.in.TransferirPortIn;

@WebServlet("/transferir")
public class TransferenciaServlet extends HttpServlet {

    private TransferirPortIn transferir;

    public TransferenciaServlet() {
    }

    public TransferenciaServlet(TransferirPortIn transferir) {
        this.transferir = transferir;
    }

    @Override
    public void init() {
        if (transferir == null) {
            transferir = (TransferirPortIn) getServletContext().getAttribute(TransferirPortIn.class.getName());
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/transferencia.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            BigDecimal valor = new BigDecimal(req.getParameter("valor"));
            transferir.executar(req.getParameter("origem"), req.getParameter("destino"), valor, req.getParameter("descricao"));
            req.setAttribute("mensagem", "transferencia realizada");
        } catch (NumberFormatException | NullPointerException e) {
            req.setAttribute("erro", "valor invalido");
        } catch (RegraVioladaException | ContaNaoEncontradaException e) {
            req.setAttribute("erro", e.getMessage());
        } catch (IllegalStateException e) {
            req.setAttribute("erro", "erro ao transferir");
        }
        req.getRequestDispatcher("/transferencia.jsp").forward(req, resp);
    }

}