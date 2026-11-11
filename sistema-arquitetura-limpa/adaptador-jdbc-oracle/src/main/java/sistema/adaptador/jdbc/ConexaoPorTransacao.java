package sistema.adaptador.jdbc;

import java.sql.Connection;

public final class ConexaoPorTransacao {

    private static final ThreadLocal<Connection> ATUAL = new ThreadLocal<Connection>();

    private ConexaoPorTransacao() {
    }

    static void iniciar(Connection conexao) {
        ATUAL.set(conexao);
    }

    static void encerrar() {
        ATUAL.remove();
    }

    public static Connection atual() {
        Connection conexao = ATUAL.get();
        if (conexao == null) {
            throw new IllegalStateException("nenhuma transacao em andamento");
        }
        return conexao;
    }

}