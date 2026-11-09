package sistema;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovimentacaoRepository extends JpaRepository<Movimentacao, Long> {

    List<Movimentacao> findByContaId(String contaId);

    List<Movimentacao> findByContaIdOrderByIdAsc(String contaId, Pageable pageable);
}
