package sistema.web.servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sistema.core.dominio.RegraVioladaException;
import sistema.core.porta.in.AbrirContaPortIn;

@WebServlet("/abrir-conta")
public class AbrirContaServlet extends HttpServlet {

    private AbrirContaPortIn abrirConta;

    public AbrirContaServlet() {
    }

    public AbrirContaServlet(AbrirContaPortIn abrirConta) {
        this.abrirConta = abrirConta;
    }

    @Override
    public void init() {
        if (abrirConta == null) {
            abrirConta = (AbrirContaPortIn) getServletContext().getAttribute(AbrirContaPortIn.class.getName());
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/abrir-conta.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String id = abrirConta.executar(req.getParameter("titular"));
            req.setAttribute("mensagem", "conta aberta: " + id);
        } catch (RegraVioladaException e) {
            req.setAttribute("erro", e.getMessage());
        } catch (IllegalStateException e) {
            req.setAttribute("erro", "erro ao processar");
        }
        req.getRequestDispatcher("/abrir-conta.jsp").forward(req, resp);
    }

}