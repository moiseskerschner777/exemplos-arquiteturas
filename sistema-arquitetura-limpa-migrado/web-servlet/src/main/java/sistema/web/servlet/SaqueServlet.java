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
import sistema.core.porta.in.SacarPortIn;

@WebServlet("/sacar")
public class SaqueServlet extends HttpServlet {

    private SacarPortIn sacar;

    public SaqueServlet() {
    }

    public SaqueServlet(SacarPortIn sacar) {
        this.sacar = sacar;
    }

    @Override
    public void init() {
        if (sacar == null) {
            sacar = (SacarPortIn) getServletContext().getAttribute(SacarPortIn.class.getName());
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/saque.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            BigDecimal valor = new BigDecimal(req.getParameter("valor"));
            sacar.executar(req.getParameter("conta"), valor, req.getParameter("descricao"));
            req.setAttribute("mensagem", "operacao realizada");
        } catch (NumberFormatException | NullPointerException e) {
            req.setAttribute("erro", "valor invalido");
        } catch (RegraVioladaException | ContaNaoEncontradaException e) {
            req.setAttribute("erro", e.getMessage());
        } catch (IllegalStateException e) {
            req.setAttribute("erro", "erro ao processar");
        }
        req.getRequestDispatcher("/saque.jsp").forward(req, resp);
    }

}