package sistema.web.spring;

import java.sql.SQLException;
import javax.sql.DataSource;
import oracle.jdbc.pool.OracleDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import sistema.adaptador.jdbc.ContaRepositoryJdbcOracle;
import sistema.adaptador.jdbc.GeradorDeIdUuid;
import sistema.adaptador.jdbc.MovimentacaoRepositoryJdbcOracle;
import sistema.adaptador.jdbc.RelogioDoSistema;
import sistema.adaptador.jdbc.TransacaoRepositoryJdbc;
import sistema.core.porta.out.ContaPortOut;
import sistema.core.porta.out.GeradorDeIdPortOut;
import sistema.core.porta.out.MovimentacaoPortOut;
import sistema.core.porta.out.RelogioPortOut;
import sistema.core.porta.out.TransacaoPortOut;

@Configuration
@Profile("oracle")
public class MontagemJdbcOracle {

    @Bean
    DataSource fonteDeDados(@Value("${db.url}") String url, @Value("${db.user}") String usuario,
            @Value("${db.password}") String senha) throws SQLException {
        OracleDataSource fonte = new OracleDataSource();
        fonte.setURL(url);
        fonte.setUser(usuario);
        fonte.setPassword(senha);
        return fonte;
    }

    @Bean
    TransacaoPortOut transacao(DataSource fonteDeDados) {
        return new TransacaoRepositoryJdbc(fonteDeDados);
    }

    @Bean
    ContaPortOut contas() {
        return new ContaRepositoryJdbcOracle();
    }

    @Bean
    MovimentacaoPortOut movimentacoes() {
        return new MovimentacaoRepositoryJdbcOracle();
    }

    @Bean
    GeradorDeIdPortOut geradorDeId() {
        return new GeradorDeIdUuid();
    }

    @Bean
    RelogioPortOut relogio() {
        return new RelogioDoSistema();
    }

}