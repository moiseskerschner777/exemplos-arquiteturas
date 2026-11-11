package sistema.adaptador.jpa;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface ContaJpaRepository extends JpaRepository<ContaJpa, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from ContaJpa c where c.id = :id")
    Optional<ContaJpa> buscarParaAtualizar(String id);

}