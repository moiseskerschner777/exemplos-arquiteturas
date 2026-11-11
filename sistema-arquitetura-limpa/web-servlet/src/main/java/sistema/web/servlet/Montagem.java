package sistema.web.servlet;

import java.sql.SQLException;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import oracle.jdbc.pool.OracleDataSource;
import sistema.adaptador.jdbc.ContaRepositoryJdbcOracle;
import sistema.adaptador.jdbc.GeradorDeIdUuid;
import sistema.adaptador.jdbc.MovimentacaoRepositoryJdbcOracle;
import sistema.adaptador.jdbc.RelogioDoSistema;
import sistema.adaptador.jdbc.TransacaoRepositoryJdbc;
import sistema.core.casodeuso.AbrirConta;
import sistema.core.casodeuso.ConsultarExtrato;
import sistema.core.casodeuso.Depositar;
import sistema.core.casodeuso.EncerrarConta;
import sistema.core.casodeuso.Sacar;
import sistema.core.casodeuso.Transferir;
import sistema.core.porta.in.AbrirContaPortIn;
import sistema.core.porta.in.ConsultarExtratoPortIn;
import sistema.core.porta.in.DepositarPortIn;
import sistema.core.porta.in.EncerrarContaPortIn;
import sistema.core.porta.in.SacarPortIn;
import sistema.core.porta.in.TransferirPortIn;
import sistema.core.porta.out.ContaPortOut;
import sistema.core.porta.out.GeradorDeIdPortOut;
import sistema.core.porta.out.MovimentacaoPortOut;
import sistema.core.porta.out.RelogioPortOut;
import sistema.core.porta.out.TransacaoPortOut;

@WebListener
public class Montagem implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent evento) {
        ServletContext contexto = evento.getServletContext();
        TransacaoPortOut transacao = new TransacaoRepositoryJdbc(criarFonteDeDados());
        ContaPortOut contas = new ContaRepositoryJdbcOracle();
        GeradorDeIdPortOut geradorDeId = new GeradorDeIdUuid();
        MovimentacaoPortOut movimentacoes = new MovimentacaoRepositoryJdbcOracle();
        RelogioPortOut relogio = new RelogioDoSistema();

        contexto.setAttribute(AbrirContaPortIn.class.getName(), new AbrirConta(contas, geradorDeId, transacao));
        contexto.setAttribute(DepositarPortIn.class.getName(), new Depositar(contas, movimentacoes, relogio, transacao));
        contexto.setAttribute(SacarPortIn.class.getName(), new Sacar(contas, movimentacoes, relogio, transacao));
        contexto.setAttribute(TransferirPortIn.class.getName(), new Transferir(contas, movimentacoes, relogio, transacao));
        contexto.setAttribute(EncerrarContaPortIn.class.getName(), new EncerrarConta(contas, transacao));
        contexto.setAttribute(ConsultarExtratoPortIn.class.getName(), new ConsultarExtrato(contas, movimentacoes, transacao));
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