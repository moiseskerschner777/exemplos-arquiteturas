package procedimento.rest;

import java.time.Clock;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import procedimento.adaptador.postgres.AgendaRepositoryPostgres;
import procedimento.adaptador.postgres.AtendimentoRepositoryPostgres;
import procedimento.adaptador.postgres.AutorizacaoRepositoryPostgres;
import procedimento.adaptador.postgres.ContratoRepositoryPostgres;
import procedimento.adaptador.postgres.FaturaRepositoryPostgres;
import procedimento.adaptador.postgres.FontePagadoraRepositoryPostgres;
import procedimento.adaptador.postgres.MaterialRepositoryPostgres;
import procedimento.adaptador.postgres.PercentualCobrancaRepositoryPostgres;
import procedimento.adaptador.postgres.PrazoRepositoryPostgres;
import procedimento.adaptador.postgres.PrecoRepositoryPostgres;
import procedimento.adaptador.postgres.ProcedimentoRepositoryPostgres;
import procedimento.adaptador.postgres.RestricaoUnidadeRepositoryPostgres;
import procedimento.adaptador.postgres.SetorRepositoryPostgres;
import procedimento.adaptador.postgres.TetoFaturamentoRepositoryPostgres;
import procedimento.adaptador.postgres.UnidadeColetaRepositoryPostgres;
import procedimento.core.casodeuso.AdicionarProcedimento;
import procedimento.core.casodeuso.Repositorios;
import procedimento.core.porta.in.AdicionarProcedimentoPortIn;

@Configuration
public class Configuracao {

    @Bean
    public AdicionarProcedimentoPortIn adicionarProcedimento(DataSource dataSource) {
        Repositorios repositorios = new Repositorios(
                new ProcedimentoRepositoryPostgres(dataSource),
                new AtendimentoRepositoryPostgres(dataSource),
                new UnidadeColetaRepositoryPostgres(dataSource),
                new RestricaoUnidadeRepositoryPostgres(dataSource),
                new MaterialRepositoryPostgres(dataSource),
                new FontePagadoraRepositoryPostgres(dataSource),
                new FaturaRepositoryPostgres(dataSource),
                new PercentualCobrancaRepositoryPostgres(dataSource),
                new AgendaRepositoryPostgres(dataSource),
                new ContratoRepositoryPostgres(dataSource),
                new AutorizacaoRepositoryPostgres(dataSource),
                new PrazoRepositoryPostgres(dataSource),
                new PrecoRepositoryPostgres(dataSource),
                new SetorRepositoryPostgres(dataSource),
                new TetoFaturamentoRepositoryPostgres(dataSource));
        return new AdicionarProcedimento(repositorios, Clock.systemDefaultZone());
    }

}