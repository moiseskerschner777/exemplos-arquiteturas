package procedimento.adaptador.postgres;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

public final class Consulta {

    @FunctionalInterface
    public interface Linha<T> {

        T ler(ResultSet resultado) throws SQLException;
    }

    private final DataSource dataSource;

    public Consulta(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> List<T> lista(String sql, Linha<T> linha, Object... parametros) {
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement comando = conexao.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                comando.setObject(i + 1, parametros[i]);
            }
            List<T> linhas = new ArrayList<>();
            try (ResultSet resultado = comando.executeQuery()) {
                while (resultado.next()) {
                    linhas.add(linha.ler(resultado));
                }
            }
            return linhas;
        } catch (SQLException erro) {
            throw new IllegalStateException("falha na consulta: " + sql + " " + Arrays.toString(parametros), erro);
        }
    }

    public <T> Optional<T> uma(String sql, Linha<T> linha, Object... parametros) {
        return lista(sql, linha, parametros).stream().findFirst();
    }

    public boolean existe(String sql, Object... parametros) {
        return uma(sql, resultado -> Boolean.TRUE, parametros).isPresent();
    }

    public static Integer inteiro(ResultSet resultado, String coluna) throws SQLException {
        Object valor = resultado.getObject(coluna);
        if (valor == null) {
            return null;
        }
        String texto = valor.toString().trim();
        return texto.isEmpty() ? null : new BigDecimal(texto).intValue();
    }

    public static boolean ligado(ResultSet resultado, String coluna) throws SQLException {
        Object valor = resultado.getObject(coluna);
        if (valor instanceof Boolean booleano) {
            return booleano;
        }
        return Integer.valueOf(1).equals(inteiro(resultado, coluna));
    }

    public static BigDecimal decimal(ResultSet resultado, String coluna) throws SQLException {
        Object valor = resultado.getObject(coluna);
        return valor == null ? null : new BigDecimal(valor.toString());
    }

    public static String texto(ResultSet resultado, String coluna) throws SQLException {
        Object valor = resultado.getObject(coluna);
        return valor == null ? null : valor.toString();
    }

    public static List<String> listaSeparada(String valor) {
        if (valor == null || valor.isBlank()) {
            return List.of();
        }
        return Arrays.stream(valor.split(",", -1)).map(String::trim).toList();
    }

}