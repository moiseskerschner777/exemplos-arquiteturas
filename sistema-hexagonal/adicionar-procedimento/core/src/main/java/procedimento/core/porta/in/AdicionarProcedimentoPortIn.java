package procedimento.core.porta.in;

import procedimento.core.porta.in.dto.AdicionarProcedimentoDto;
import procedimento.core.porta.in.dto.ProcedimentoAdicionadoDto;

public interface AdicionarProcedimentoPortIn {

    ProcedimentoAdicionadoDto executar(AdicionarProcedimentoDto dto);

}