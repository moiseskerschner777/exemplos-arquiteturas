package sistema.web.spring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

@Configuration
public class MontagemCasosDeUso {

    @Bean
    AbrirContaPortIn abrirConta(ContaPortOut contas, GeradorDeIdPortOut geradorDeId, TransacaoPortOut transacao) {
        return new AbrirConta(contas, geradorDeId, transacao);
    }

    @Bean
    DepositarPortIn depositar(ContaPortOut contas, MovimentacaoPortOut movimentacoes, RelogioPortOut relogio,
            TransacaoPortOut transacao) {
        return new Depositar(contas, movimentacoes, relogio, transacao);
    }

    @Bean
    SacarPortIn sacar(ContaPortOut contas, MovimentacaoPortOut movimentacoes, RelogioPortOut relogio,
            TransacaoPortOut transacao) {
        return new Sacar(contas, movimentacoes, relogio, transacao);
    }

    @Bean
    TransferirPortIn transferir(ContaPortOut contas, MovimentacaoPortOut movimentacoes, RelogioPortOut relogio,
            TransacaoPortOut transacao) {
        return new Transferir(contas, movimentacoes, relogio, transacao);
    }

    @Bean
    EncerrarContaPortIn encerrarConta(ContaPortOut contas, TransacaoPortOut transacao) {
        return new EncerrarConta(contas, transacao);
    }

    @Bean
    ConsultarExtratoPortIn consultarExtrato(ContaPortOut contas, MovimentacaoPortOut movimentacoes,
            TransacaoPortOut transacao) {
        return new ConsultarExtrato(contas, movimentacoes, transacao);
    }

}