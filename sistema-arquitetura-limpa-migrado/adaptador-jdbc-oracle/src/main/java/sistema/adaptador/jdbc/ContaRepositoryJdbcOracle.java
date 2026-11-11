package sistema.adaptador.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import sistema.core.dominio.Conta;
import sistema.core.dominio.SituacaoConta;
import sistema.core.porta.out.ContaPortOut;

public class ContaRepositoryJdbcOracle implements ContaPortOut {

    private static final String BUSCAR_PARA_ATUALIZAR = "SELECT titular, saldo, situacao FROM conta WHERE id = ? FOR UPDATE";

    private static final String MERGE = "MERGE INTO conta c "
            + "USING (SELECT ? AS id, ? AS titular, ? AS saldo, ? AS situacao FROM dual) n ON (c.id = n.id) "
            + "WHEN MATCHED THEN UPDATE SET c.titular = n.titular, c.saldo = n.saldo, c.situacao = n.situacao "
            + "WHEN NOT MATCHED THEN INSERT (id, titular, saldo, situacao) "
            + "VALUES (n.id, n.titular, n.saldo, n.situacao)";

    @Override
    public Optional<Conta> buscarParaAtualizar(String id) {
        Connection conexao = ConexaoPorTransacao.atual();
        try (PreparedStatement ps = conexao.prepareStatement(BUSCAR_PARA_ATUALIZAR)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(Conta.reconstituir(id, rs.getString("titular"), rs.getBigDecimal("saldo"),
                        SituacaoConta.valueOf(rs.getString("situacao"))));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("erro ao processar", e);
        }
    }

    @Override
    public void salvar(Conta conta) {
        Connection conexao = ConexaoPorTransacao.atual();
        try (PreparedStatement ps = conexao.prepareStatement(MERGE)) {
            ps.setString(1, conta.getId());
            ps.setString(2, conta.getTitular());
            ps.setBigDecimal(3, conta.getSaldo());
            ps.setString(4, conta.getSituacao().name());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("erro ao processar", e);
        }
    }

}