package procedimento.core.porta.out;

import java.util.Optional;

import procedimento.core.dominio.ConfiguracaoPrazo;

public interface PrazoRepositoryPortOut {

    Optional<ConfiguracaoPrazo> buscarRotina(String rotinaPrazoId);

}