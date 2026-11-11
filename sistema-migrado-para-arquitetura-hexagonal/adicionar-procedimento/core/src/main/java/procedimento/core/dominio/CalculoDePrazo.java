package procedimento.core.dominio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public final class CalculoDePrazo {

    private static final int MEIA_NOITE = 86399;
    private static final int LIMITE_DE_VOLTAS = 3660;
    private static final DateTimeFormatter DIA_MES = DateTimeFormatter.ofPattern("dd/MM");

    private final Entrada entrada;
    private final PrazoProcedimento prazo;
    private final LocalDate dataAtual;
    private final int diaSemanaAtual;
    private int horaAtual;
    private LocalDate dataInicioRotina;
    private Integer tipoPrazoRotina;
    private boolean prazoPorPosto;
    private int posicaoPosto;
    private boolean utilizaRotina;

    private CalculoDePrazo(Entrada entrada, LocalDateTime inicio) {
        this.entrada = entrada;
        this.prazo = entrada.prazo();
        this.dataAtual = inicio.toLocalDate();
        this.diaSemanaAtual = diaDaSemana(dataAtual);
        this.horaAtual = inicio.toLocalTime().toSecondOfDay();
    }

    public static Resultado calcular(Entrada entrada, LocalDateTime inicio) {
        Resultado resultado = new CalculoDePrazo(entrada, inicio).controlePrazo();
        if (entrada.unidade().liberaPorProcedimento()) {
            return new Resultado(resultado.prazoMinimo(), resultado.prazoMinimo(), resultado.entregaMinimaSetor(),
                    resultado.entregaMinimaSetor());
        }
        return resultado;
    }

    private Resultado controlePrazo() {
        int horaInicial = horaAtual;
        posicaoPosto = prazo.prazoCodigoPosto().posicao(entrada.unidade().id());
        prazoPorPosto = posicaoPosto > 0;
        utilizaRotina = prazoPorPosto ? "1".equals(prazo.prazoRotinaPostoUtilizaRotina().valor(posicaoPosto))
                : prazo.prazoRotinaUtilizaRotina();

        RotinaPrazo rotina = null;
        LocalDate dataRotina;
        Integer tipoPrazo;
        if (utilizaRotina) {
            String codigoRotina = prazoPorPosto ? prazo.prazoCodigoRotinaPosto().valor(posicaoPosto) : prazo.prazoCodigoRotina();
            rotina = rotina(codigoRotina);
            dataRotina = verificaRotinaPrazo(rotina, false);
            dataInicioRotina = dataRotina;
            tipoPrazo = recuperaTipoPrazoExame(false);
        } else {
            tipoPrazo = recuperaTipoPrazoExame(false);
            dataRotina = verificaS01Parametro(false, tipoPrazo);
        }
        Integer tipoPrazoMinimo = recuperaTipoPrazoExame(true);
        LocalDate dataRotinaMinimo = verificaS01Parametro(true, tipoPrazoMinimo);
        dataInicioRotina = dataRotina;

        long prazoExame = numero(recuperaPrazoExame(rotina), "prazo");
        long prazoMinimoExame = numero(recuperaPrazoMinimoExame(), "prazo mínimo");
        horaAtual = horaInicial;

        long dias;
        Integer horaEntrega = null;
        switch (tipo(tipoPrazo)) {
            case 1 -> dias = verificarDataProducao(dataRotina, verificaDiasUteisCorridos(dataRotina, prazoExame, true));
            case 2 -> dias = prazoExame;
            case 3 -> {
                long[] horas = somarHoras(horaInicial, prazoExame);
                dias = horas[0];
                horaEntrega = (int) horas[1];
            }
            default -> throw new ErroDePrazo("Tipo de prazo não encontrado para o procedimento " + procedimento() + ".");
        }
        LocalDate dataEntregaSetor = dataRotina.plusDays(dias);
        dias = verificaDataEntrega(dataRotina, dias, tipoPrazo, 0);
        LocalDate dataEntrega = dataRotina.plusDays(dias);
        if (horaEntrega == null) {
            horaEntrega = horarioEntrega();
        }
        Integer horaEntregaSetor = horaEntrega;
        if (utilizaRotina) {
            int horaDaRotina = horaEntregaRotina(dataEntrega, rotina);
            if (horaDaRotina != 0) {
                horaEntrega = horaDaRotina;
            }
            horaEntregaSetor = horaEntrega;
            int horaDaRotinaSetor = horaEntregaRotina(dataEntregaSetor, rotina);
            if (horaDaRotinaSetor != 0) {
                horaEntregaSetor = horaDaRotinaSetor;
            }
        }

        long diasMinimo;
        Integer prazoMinimoHora = null;
        switch (tipo(tipoPrazoMinimo)) {
            case 1 -> diasMinimo = verificarDataProducao(dataRotinaMinimo, verificaDiasUteisCorridos(dataRotinaMinimo, prazoMinimoExame, true));
            case 2 -> diasMinimo = prazoMinimoExame;
            case 3 -> {
                long[] horas = somarHoras(horaInicial, prazoMinimoExame);
                diasMinimo = horas[0];
                prazoMinimoHora = (int) horas[1];
            }
            default -> throw new ErroDePrazo("Tipo de prazo não encontrado para o procedimento " + procedimento() + ".");
        }
        LocalDate dataEntregaMinimaSetor = dataRotinaMinimo.plusDays(diasMinimo);
        diasMinimo = verificaDataEntrega(dataRotinaMinimo, diasMinimo, tipoPrazoMinimo, 0);
        LocalDate prazoMinimo = dataRotinaMinimo.plusDays(diasMinimo);
        if (prazoMinimoHora == null) {
            prazoMinimoHora = horarioEntrega();
        } else if (dataEntrega.equals(prazoMinimo) && prazoMinimoHora > segundos(horaEntrega)) {
            prazoMinimoHora = horaEntrega;
        }

        return new Resultado(momento(dataEntrega, horaEntrega), momento(prazoMinimo, prazoMinimoHora),
                momento(dataEntregaSetor, horaEntregaSetor), momento(dataEntregaMinimaSetor, prazoMinimoHora));
    }

    private Integer recuperaTipoPrazoExame(boolean minimo) {
        if (prazoPorPosto) {
            if (!minimo) {
                if (utilizaRotina) {
                    RotinaPrazo rotina = rotina(prazo.prazoCodigoRotinaPosto().valor(posicaoPosto));
                    int posicaoDia = rotina.dias().posicao(String.valueOf(diaDaSemana(dataInicioRotina)));
                    return inteiro(rotina.tipoDias().valor(posicaoDia));
                }
                return inteiro(prazo.prazoRotinaPostoTipo().valor(posicaoPosto));
            }
            String tipo = prazo.prazoMinimoPostoTipo().valor(posicaoPosto);
            return tipo.isEmpty() ? prazo.prazoMinimoTipo() : inteiro(tipo);
        }
        if (tipoPrazoRotina != null && tipoPrazoRotina > 0 && !minimo) {
            return tipoPrazoRotina;
        }
        return minimo ? prazo.prazoMinimoTipo() : prazo.prazoRotinaTipo();
    }

    private LocalDate verificaRotinaPrazo(RotinaPrazo rotina, boolean minimo) {
        boolean virouSemana = false;
        String valorDia = "";
        int posicaoDia = rotina.dias().posicao(String.valueOf(diaSemanaAtual));
        if (posicaoDia > 0) {
            int horaLimite = segundos(rotina.horarioLimite().valor(posicaoDia));
            if (!minimo && horaAtual > horaLimite) {
                posicaoDia = 0;
            }
        }
        if (posicaoDia == 0) {
            int controlaLoop = diaSemanaAtual;
            int voltas = 0;
            do {
                posicaoDia = rotina.dias().proxima(controlaLoop);
                if (posicaoDia > 0) {
                    valorDia = rotina.dias().valor(posicaoDia);
                    tipoPrazoRotina = inteiro(rotina.tipoDias().valor(posicaoDia));
                    controlaLoop = valorDia.isEmpty() ? controlaLoop + 1 : -1;
                } else {
                    controlaLoop = 0;
                    virouSemana = true;
                }
                conferirVoltas(++voltas, "rotina de prazo " + rotina.id() + " sem dia definido");
            } while (controlaLoop != -1);
            if (posicaoDia == 7) {
                virouSemana = true;
            }
        } else {
            valorDia = rotina.dias().valor(posicaoDia);
            tipoPrazoRotina = inteiro(rotina.tipoDias().valor(posicaoDia));
        }
        LocalDate saida = somarDias(valorDia, virouSemana);
        while (feriado(saida, null)) {
            do {
                saida = saida.plusDays(1);
            } while (rotina.dias().posicao(String.valueOf(diaDaSemana(saida))) == 0);
        }
        return saida;
    }

    private LocalDate verificaS01Parametro(boolean minimo, Integer tipoPrazo) {
        ListaPosicional diasProducao = listaDiasProducao();
        boolean virouSemana = false;
        String valorDia = "";
        int proximoDia = 0;
        int posicaoDia = diasProducao.posicao(String.valueOf(diaSemanaAtual));
        if (posicaoDia > 0 && !minimo && tipo(tipoPrazo) != 3 && horaAtual > segundos(horarioLimite())) {
            posicaoDia = 0;
            proximoDia = 1;
        }
        if (posicaoDia == 0) {
            int controlaLoop = diaSemanaAtual + proximoDia;
            int voltas = 0;
            do {
                posicaoDia = diasProducao.proxima(controlaLoop);
                if (posicaoDia > 0) {
                    valorDia = diasProducao.valor(posicaoDia);
                    controlaLoop = valorDia.isEmpty() ? controlaLoop + 1 : -1;
                } else {
                    controlaLoop = 0;
                    virouSemana = true;
                }
                conferirVoltas(++voltas, "dias de produção sem dia definido");
            } while (controlaLoop != -1);
        } else {
            valorDia = diasProducao.valor(posicaoDia);
        }
        return somarDias(valorDia, virouSemana);
    }

    private String recuperaPrazoExame(RotinaPrazo rotina) {
        if (!prazoPorPosto && !utilizaRotina) {
            return prazo.prazoCodigoRotina();
        }
        if (prazoPorPosto && !utilizaRotina) {
            return daLista(prazo.prazoCodigoRotinaPosto().valor(posicaoPosto));
        }
        int diaSemana = diaDaSemana(dataInicioRotina);
        int posicaoEncontrada = rotina.dias().posicao(String.valueOf(diaSemana));
        if (posicaoEncontrada > 0) {
            return daLista(rotina.numDiasApos().valor(posicaoEncontrada));
        }
        horaAtual = 0;
        LocalDate dataRotina = verificaRotinaPrazo(rotina, false);
        return daLista(rotina.numDiasApos().valor(diaDaSemana(dataRotina)));
    }

    private String recuperaPrazoMinimoExame() {
        return prazoPorPosto ? daLista(prazo.prazoMinimoPosto().valor(posicaoPosto)) : prazo.prazoMinimo();
    }

    private long verificaDiasUteisCorridos(LocalDate dataInicio, long prazoDias, boolean uteis) {
        if (!uteis) {
            return prazoDias;
        }
        ListaPosicional diasProducao = listaDiasProducao();
        long meta = prazoDias;
        long trabalhados = 0;
        int i = 0;
        boolean produtivo;
        do {
            produtivo = true;
            LocalDate data = dataInicio.plusDays(i);
            if (diasProducao.posicao(String.valueOf(diaDaSemana(data))) == 0 || feriado(data, entrada.unidade().id())) {
                prazoDias++;
                produtivo = false;
            } else {
                trabalhados++;
            }
            conferirVoltas(++i, "dias úteis sem dia produtivo");
        } while ((trabalhados < meta && produtivo) || !produtivo);
        return prazoDias;
    }

    private long verificarDataProducao(LocalDate dataInicio, long prazoDias) {
        ListaPosicional diasProducao = listaDiasProducao();
        LocalDate dataEntregaSetor = dataInicio.plusDays(prazoDias);
        int tentativas = 0;
        boolean produtivo;
        do {
            produtivo = diasProducao.posicao(String.valueOf(diaDaSemana(dataEntregaSetor))) > 0
                    && !feriado(dataEntregaSetor, entrada.unidade().id());
            if (!produtivo) {
                dataEntregaSetor = dataEntregaSetor.plusDays(1);
                prazoDias++;
            }
            tentativas++;
        } while (!produtivo && tentativas <= 7);
        return prazoDias;
    }

    private long verificaDataEntrega(LocalDate dataInicio, long prazoDias, Integer tipoPrazo, int profundidade) {
        conferirVoltas(profundidade, "dias de entrega sem dia definido");
        ListaPosicional diasEntrega = entrada.unidade().diaEntrega().temValor() ? entrada.unidade().diaEntrega()
                : entrada.parametros().diaEntrega();
        LocalDate dataEntrega = dataInicio.plusDays(prazoDias);
        if (tipo(tipoPrazo) == 3) {
            return prazoDias;
        }
        int numeroDiaSemana = diaDaSemana(dataEntrega);
        if (diasEntrega.posicao(String.valueOf(numeroDiaSemana)) > 0) {
            long comFeriado = feriado(dataEntrega, entrada.unidade().id()) ? prazoDias + 1 : prazoDias;
            return prazoDias < comFeriado ? verificaDataEntrega(dataInicio, comFeriado, tipoPrazo, profundidade + 1) : prazoDias;
        }
        int posicaoDia = numeroDiaSemana;
        boolean virouSemana = false;
        String valorEntrega = "";
        boolean diaEntregaValido;
        int voltas = 0;
        do {
            diaEntregaValido = false;
            posicaoDia = diasEntrega.proxima(posicaoDia);
            if (posicaoDia > 0) {
                valorEntrega = diasEntrega.valor(posicaoDia);
                diaEntregaValido = !valorEntrega.isEmpty();
            } else {
                virouSemana = true;
            }
            conferirVoltas(++voltas, "dias de entrega sem dia definido");
        } while (!diaEntregaValido);
        long saida = virouSemana
                ? prazoDias + (7 - posicaoNaSemana(String.valueOf(numeroDiaSemana))) + posicaoNaSemana(valorEntrega)
                : prazoDias + (inteiroOuZero(valorEntrega) - numeroDiaSemana);
        long comFeriado = feriado(dataInicio.plusDays(saida), entrada.unidade().id()) ? saida + 1 : saida;
        if (saida < comFeriado) {
            return verificaDataEntrega(dataInicio, comFeriado, tipoPrazo, profundidade + 1);
        }
        return saida;
    }

    private boolean feriado(LocalDate data, String unidadeColetaId) {
        String diaMes = data.format(DIA_MES);
        String ano = String.valueOf(data.getYear());
        for (Feriado feriado : entrada.feriados()) {
            if ("G".equals(feriado.definicao()) && feriado.valeEm(diaMes, ano)) {
                return true;
            }
        }
        if (unidadeColetaId == null) {
            return false;
        }
        String unidadeAtendimentoId = textoOuVazio(entrada.unidade().unidadeAtendimentoId());
        for (Feriado feriado : entrada.feriados()) {
            if (!"U".equals(feriado.definicao()) || !feriado.valeEm(diaMes, ano)) {
                continue;
            }
            for (Feriado.UnidadeDoFeriado unidade : feriado.unidades()) {
                if (unidadeColetaId.equals(textoOuVazio(unidade.unidadeColetaId()))
                        || unidadeAtendimentoId.equals(textoOuVazio(unidade.unidadeAtendimentoId()))) {
                    return true;
                }
            }
        }
        return false;
    }

    private ListaPosicional listaDiasProducao() {
        ListaPosicional lista = entrada.diasProducaoSetor().temValor() ? entrada.diasProducaoSetor()
                : entrada.unidade().diaProducao().temValor() ? entrada.unidade().diaProducao() : entrada.parametros().diaProducao();
        if (!lista.temValor()) {
            throw new ErroDePrazo("Dias de produção não definido!");
        }
        return lista;
    }

    private Integer horarioLimite() {
        return entrada.unidade().horarioLimiteRotina() != null ? entrada.unidade().horarioLimiteRotina() : entrada.parametros().horarioLimite();
    }

    private Integer horarioEntrega() {
        return entrada.unidade().horarioEntrega() != null ? entrada.unidade().horarioEntrega() : entrada.parametros().horarioEntrega();
    }

    private static int horaEntregaRotina(LocalDate data, RotinaPrazo rotina) {
        int posicaoDia = rotina.dias().posicao(String.valueOf(diaDaSemana(data)));
        return posicaoDia > 0 ? segundos(rotina.horarioEntrega().valor(posicaoDia)) : 0;
    }

    private RotinaPrazo rotina(String codigo) {
        return entrada.rotinas().apply(codigo)
                .orElseThrow(() -> new ErroDePrazo("Rotina de prazo " + codigo + " não encontrada."));
    }

    private LocalDate somarDias(String valorDia, boolean virouSemana) {
        if (virouSemana) {
            return dataAtual.plusDays((7L - posicaoNaSemana(String.valueOf(diaSemanaAtual))) + posicaoNaSemana(valorDia));
        }
        return dataAtual.plusDays(inteiroOuZero(valorDia) - diaSemanaAtual);
    }

    private long numero(String valor, String nome) {
        if (valor == null) {
            if (prazoPorPosto) {
                throw new ErroDePrazo("Não há definição de " + nome + " para a unidade de coleta: \r\n" + entrada.unidade().descricao()
                        + " (" + procedimento() + ")");
            }
            throw new ErroDePrazo("Não há definição de " + nome + " para o procedimento: \r\n" + procedimento());
        }
        try {
            return new BigDecimal(valor.trim()).longValue();
        } catch (NumberFormatException erro) {
            throw new ErroDePrazo("O prazo definido na configuração do procedimento " + entrada.mnemonico() + " não é válido.\r\n"
                    + "Solicite a correção do prazo deste procedimento para um valor numérico.");
        }
    }

    private String procedimento() {
        return entrada.mnemonico() + " - " + entrada.descricao();
    }

    private static String daLista(String valor) {
        return valor.isEmpty() ? null : valor;
    }

    private static long[] somarHoras(int horaInicial, long prazoSegundos) {
        long horaFinal = horaInicial + prazoSegundos;
        long dias = 0;
        while (horaFinal > MEIA_NOITE) {
            horaFinal -= MEIA_NOITE + 1;
            dias++;
        }
        return new long[] {dias, horaFinal};
    }

    private static void conferirVoltas(int voltas, String motivo) {
        if (voltas > LIMITE_DE_VOLTAS) {
            throw new ErroDePrazo("Cálculo de prazo sem fim: " + motivo + ".");
        }
    }

    private static int posicaoNaSemana(String valor) {
        int dia = inteiroOuZero(valor);
        return valor != null && valor.matches("[0-6]") ? dia + 1 : 0;
    }

    private static int diaDaSemana(LocalDate data) {
        return data.getDayOfWeek().getValue() % 7;
    }

    private static int tipo(Integer tipo) {
        return tipo == null ? 0 : tipo;
    }

    private static int segundos(Integer valor) {
        return valor == null ? 0 : valor;
    }

    private static int segundos(String valor) {
        return inteiroOuZero(valor);
    }

    private static Integer inteiro(String valor) {
        return valor == null || valor.isBlank() ? null : Integer.valueOf(valor.trim());
    }

    private static int inteiroOuZero(String valor) {
        Integer inteiro = inteiro(valor);
        return inteiro == null ? 0 : inteiro;
    }

    private static String textoOuVazio(String valor) {
        return valor == null ? "" : valor;
    }

    private static LocalDateTime momento(LocalDate data, Integer segundos) {
        return data.atStartOfDay().plusSeconds(segundos(segundos));
    }

    public record Entrada(
            String mnemonico,
            String descricao,
            PrazoProcedimento prazo,
            PrazoDaUnidade unidade,
            ListaPosicional diasProducaoSetor,
            ParametrosDePrazo parametros,
            List<Feriado> feriados,
            Function<String, Optional<RotinaPrazo>> rotinas) {

    }

    public record Resultado(LocalDateTime entrega, LocalDateTime prazoMinimo, LocalDateTime entregaSetor, LocalDateTime entregaMinimaSetor) {

    }

}