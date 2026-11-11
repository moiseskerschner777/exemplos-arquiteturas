package sistema.web.servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sistema.core.dominio.ContaNaoEncontradaException;
import sistema.core.dominio.RegraVioladaException;
import sistema.core.porta.in.EncerrarContaPortIn;

@WebServlet("/encerrar-conta")
public class EncerrarContaServlet extends HttpServlet {

    private EncerrarContaPortIn encerrarConta;

    public EncerrarContaServlet() {
    }

    public EncerrarContaServlet(EncerrarContaPortIn encerrarConta) {
        this.encerrarConta = encerrarConta;
    }

    @Override
    public void init() {
        if (encerrarConta == null) {
            encerrarConta = (EncerrarContaPortIn) getServletContext().getAttribute(EncerrarContaPortIn.class.getName());
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/encerrar-conta.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            encerrarConta.executar(req.getParameter("conta"));
            req.setAttribute("mensagem", "conta encerrada");
        } catch (RegraVioladaException | ContaNaoEncontradaException e) {
            req.setAttribute("erro", e.getMessage());
        } catch (IllegalStateException e) {
            req.setAttribute("erro", "erro ao processar");
        }
        req.getRequestDispatcher("/encerrar-conta.jsp").forward(req, resp);
    }

}