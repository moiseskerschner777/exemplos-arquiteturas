package sistema.web.servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sistema.core.dominio.ContaNaoEncontradaException;
import sistema.core.dominio.RegraVioladaException;
import sistema.core.porta.in.ConsultarExtratoPortIn;

@WebServlet("/extrato")
public class ExtratoServlet extends HttpServlet {

    private ConsultarExtratoPortIn consultarExtrato;

    public ExtratoServlet() {
    }

    public ExtratoServlet(ConsultarExtratoPortIn consultarExtrato) {
        this.consultarExtrato = consultarExtrato;
    }

    @Override
    public void init() {
        if (consultarExtrato == null) {
            consultarExtrato = (ConsultarExtratoPortIn) getServletContext().getAttribute(ConsultarExtratoPortIn.class.getName());
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/extrato.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            int pagina = Integer.parseInt(req.getParameter("pagina"));
            int tamanho = Integer.parseInt(req.getParameter("tamanho"));
            req.setAttribute("extrato", consultarExtrato.executar(req.getParameter("conta"), pagina, tamanho));
        } catch (NumberFormatException e) {
            req.setAttribute("erro", "pagina e tamanho invalidos");
        } catch (RegraVioladaException | ContaNaoEncontradaException e) {
            req.setAttribute("erro", e.getMessage());
        } catch (IllegalStateException e) {
            req.setAttribute("erro", "erro ao processar");
        }
        req.getRequestDispatcher("/extrato.jsp").forward(req, resp);
    }

}