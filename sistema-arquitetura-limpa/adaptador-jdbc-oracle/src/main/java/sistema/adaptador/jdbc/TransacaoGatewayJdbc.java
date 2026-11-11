package sistema.adaptador.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;
import javax.sql.DataSource;
import sistema.core.porta.out.TransacaoPortOut;

public class TransacaoGatewayJdbc implements TransacaoPortOut {

    private final DataSource dataSource;

    public TransacaoGatewayJdbc(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public <T> T executar(Supplier<T> operacao) {
        try (Connection conexao = dataSource.getConnection()) {
            conexao.setAutoCommit(false);
            ConexaoPorTransacao.iniciar(conexao);
            try {
                T resultado = operacao.get();
                conexao.commit();
                return resultado;
            } catch (RuntimeException e) {
                conexao.rollback();
                throw e;
            } finally {
                ConexaoPorTransacao.encerrar();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("erro ao processar", e);
        }
    }

}