package sistema.adaptador.jpa;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovimentacaoJpaRepository extends JpaRepository<MovimentacaoJpa, Long> {

    Page<MovimentacaoJpa> findByContaIdOrderById(String contaId, Pageable pageable);

}