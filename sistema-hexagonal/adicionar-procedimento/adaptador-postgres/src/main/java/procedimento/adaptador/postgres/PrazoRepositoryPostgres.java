package procedimento.adaptador.postgres;

import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import procedimento.core.dominio.Feriado;
import procedimento.core.dominio.ListaPosicional;
import procedimento.core.dominio.ParametrosDePrazo;
import procedimento.core.dominio.PrazoDaUnidade;
import procedimento.core.dominio.RotinaPrazo;
import procedimento.core.porta.out.PrazoRepositoryPortOut;

public class PrazoRepositoryPostgres implements PrazoRepositoryPortOut {

    private final Consulta consulta;

    public PrazoRepositoryPostgres(DataSource dataSource) {
        this.consulta = new Consulta(dataSource);
    }

    @Override
    public Optional<RotinaPrazo> buscarRotina(String rotinaPrazoId) {
        return consulta.uma("select id, dias, horario_limite, horario_entrega, num_dias_apos, tipo_dias from rotina_prazo where id = ?",
                linha -> new RotinaPrazo(
                        linha.getString("id"),
                        Consulta.listaPosicional(linha.getString("dias")),
                        Consulta.listaPosicional(linha.getString("horario_limite")),
                        Consulta.listaPosicional(linha.getString("horario_entrega")),
                        Consulta.listaPosicional(linha.getString("num_dias_apos")),
                        Consulta.listaPosicional(linha.getString("tipo_dias"))),
                rotinaPrazoId);
    }

    @Override
    public Optional<PrazoDaUnidade> buscarPrazoDaUnidade(String unidadeColetaId) {
        return consulta.uma("select id, descricao, unidade_atendimento_id, tipo_liberacao, dia_producao, dia_entrega, "
                        + "horario_limite_rotina, horario_entrega from unidade_coleta where id = ?",
                linha -> new PrazoDaUnidade(
                        Consulta.texto(linha, "id"),
                        linha.getString("descricao"),
                        Consulta.texto(linha, "unidade_atendimento_id"),
                        linha.getString("tipo_liberacao"),
                        Consulta.listaPosicional(linha.getString("dia_producao")),
                        Consulta.listaPosicional(linha.getString("dia_entrega")),
                        Consulta.segundos(linha, "horario_limite_rotina"),
                        Consulta.segundos(linha, "horario_entrega")),
                Long.valueOf(unidadeColetaId));
    }

    @Override
    public ListaPosicional buscarDiasProducaoDoSetor(String setorId) {
        return consulta.uma("select dia_producao from setor where id = ?",
                        linha -> Consulta.listaPosicional(linha.getString("dia_producao")), setorId)
                .orElse(ListaPosicional.vazia());
    }

    @Override
    public Optional<ParametrosDePrazo> buscarParametros() {
        return consulta.uma("select dia_producao, dia_entrega, horario_limite, horario_entrega from parametro_geral where id = 'UNICO'",
                linha -> new ParametrosDePrazo(
                        Consulta.listaPosicional(linha.getString("dia_producao")),
                        Consulta.listaPosicional(linha.getString("dia_entrega")),
                        Consulta.segundos(linha, "horario_limite"),
                        Consulta.segundos(linha, "horario_entrega")));
    }

    @Override
    public List<Feriado> buscarFeriados() {
        return consulta.lista("select id, data, ano_feriado, definicao from feriado order by id",
                linha -> new Feriado(
                        linha.getString("data"),
                        linha.getString("ano_feriado"),
                        linha.getString("definicao"),
                        unidadesDoFeriado(linha.getString("id"))));
    }

    private List<Feriado.UnidadeDoFeriado> unidadesDoFeriado(String feriadoId) {
        return consulta.lista("select unidade_coleta_id, unidade_atendimento_id from feriado_unidade where feriado_id = ? order by id",
                linha -> new Feriado.UnidadeDoFeriado(Consulta.texto(linha, "unidade_coleta_id"),
                        Consulta.texto(linha, "unidade_atendimento_id")),
                feriadoId);
    }

}