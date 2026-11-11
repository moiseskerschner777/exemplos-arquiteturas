package sistema.web.servlet;

import java.sql.SQLException;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import oracle.jdbc.pool.OracleDataSource;
import sistema.adaptador.jdbc.ContaGatewayJdbcOracle;
import sistema.adaptador.jdbc.GeradorDeIdUuid;
import sistema.adaptador.jdbc.TransacaoGatewayJdbc;
import sistema.core.casodeuso.AbrirConta;
import sistema.core.porta.in.AbrirContaPortIn;
import sistema.core.porta.out.ContaPortOut;
import sistema.core.porta.out.GeradorDeIdPortOut;
import sistema.core.porta.out.TransacaoPortOut;

@WebListener
public class Montagem implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent evento) {
        ServletContext contexto = evento.getServletContext();
        TransacaoPortOut transacao = new TransacaoGatewayJdbc(criarFonteDeDados());
        ContaPortOut contas = new ContaGatewayJdbcOracle();
        GeradorDeIdPortOut geradorDeId = new GeradorDeIdUuid();

        contexto.setAttribute(AbrirContaPortIn.class.getName(), new AbrirConta(contas, geradorDeId, transacao));
    }

    private OracleDataSource criarFonteDeDados() {
        try {
            OracleDataSource fonte = new OracleDataSource();
            fonte.setURL(System.getProperty("db.url", System.getenv("DB_URL")));
            fonte.setUser(System.getProperty("db.user", System.getenv("DB_USER")));
            fonte.setPassword(System.getProperty("db.password", System.getenv("DB_PASSWORD")));
            return fonte;
        } catch (SQLException e) {
            throw new IllegalStateException("erro ao configurar o banco", e);
        }
    }

}