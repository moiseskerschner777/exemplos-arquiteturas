package sistema.adaptador.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import sistema.core.dominio.Movimentacao;
import sistema.core.dominio.Pagina;
import sistema.core.dominio.TipoMovimentacao;
import sistema.core.porta.out.MovimentacaoPortOut;

public class MovimentacaoRepositoryJdbcOracle implements MovimentacaoPortOut {

    private static final String INSERIR = "INSERT INTO movimentacao (id, conta_id, tipo, valor, descricao, data_hora) "
            + "VALUES (seq_movimentacao.NEXTVAL, ?, ?, ?, ?, ?)";

    private static final String LISTAR = "SELECT id, tipo, valor, descricao, data_hora FROM ("
            + "SELECT t.*, ROWNUM rn FROM ("
            + "SELECT id, tipo, valor, descricao, data_hora FROM movimentacao WHERE conta_id = ? ORDER BY id"
            + ") t WHERE ROWNUM <= ?) WHERE rn > ?";

    private static final String CONTAR = "SELECT COUNT(*) FROM movimentacao WHERE conta_id = ?";

    @Override
    public void registrar(Movimentacao movimentacao) {
        Connection conexao = ConexaoPorTransacao.atual();
        try (PreparedStatement ps = conexao.prepareStatement(INSERIR)) {
            ps.setString(1, movimentacao.getContaId());
            ps.setString(2, movimentacao.getTipo().name());
            ps.setBigDecimal(3, movimentacao.getValor());
            ps.setString(4, movimentacao.getDescricao());
            ps.setTimestamp(5, Timestamp.valueOf(movimentacao.getDataHora()));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("erro ao processar", e);
        }
    }

    @Override
    public Pagina<Movimentacao> listarPorConta(String contaId, int pagina, int tamanho) {
        Connection conexao = ConexaoPorTransacao.atual();
        try (PreparedStatement listar = conexao.prepareStatement(LISTAR);
                PreparedStatement contar = conexao.prepareStatement(CONTAR)) {
            listar.setString(1, contaId);
            listar.setInt(2, (pagina + 1) * tamanho);
            listar.setInt(3, pagina * tamanho);
            List<Movimentacao> itens = new ArrayList<Movimentacao>();
            try (ResultSet rs = listar.executeQuery()) {
                while (rs.next()) {
                    itens.add(Movimentacao.reconstituir(rs.getLong(1), contaId, TipoMovimentacao.valueOf(rs.getString(2)),
                            rs.getBigDecimal(3), rs.getString(4), rs.getTimestamp(5).toLocalDateTime()));
                }
            }
            contar.setString(1, contaId);
            try (ResultSet rs = contar.executeQuery()) {
                rs.next();
                return new Pagina<Movimentacao>(itens, pagina, tamanho, rs.getLong(1));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("erro ao processar", e);
        }
    }

}