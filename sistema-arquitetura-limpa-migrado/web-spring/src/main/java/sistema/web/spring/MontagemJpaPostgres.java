package sistema.web.spring;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.PlatformTransactionManager;
import sistema.adaptador.jdbc.GeradorDeIdUuid;
import sistema.adaptador.jdbc.RelogioDoSistema;
import sistema.adaptador.jpa.ContaJpaRepository;
import sistema.adaptador.jpa.ContaRepositoryJpaPostgres;
import sistema.adaptador.jpa.MovimentacaoJpaRepository;
import sistema.adaptador.jpa.MovimentacaoRepositoryJpaPostgres;
import sistema.adaptador.jpa.TransacaoRepositorySpring;
import sistema.core.porta.out.ContaPortOut;
import sistema.core.porta.out.GeradorDeIdPortOut;
import sistema.core.porta.out.MovimentacaoPortOut;
import sistema.core.porta.out.RelogioPortOut;
import sistema.core.porta.out.TransacaoPortOut;

@Configuration
@Profile("postgres")
@EnableJpaRepositories(basePackages = "sistema.adaptador.jpa")
@EntityScan(basePackages = "sistema.adaptador.jpa")
public class MontagemJpaPostgres {

    @Bean
    TransacaoPortOut transacao(PlatformTransactionManager gerenciador) {
        return new TransacaoRepositorySpring(gerenciador);
    }

    @Bean
    ContaPortOut contas(ContaJpaRepository repositorio) {
        return new ContaRepositoryJpaPostgres(repositorio);
    }

    @Bean
    MovimentacaoPortOut movimentacoes(MovimentacaoJpaRepository repositorio) {
        return new MovimentacaoRepositoryJpaPostgres(repositorio);
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