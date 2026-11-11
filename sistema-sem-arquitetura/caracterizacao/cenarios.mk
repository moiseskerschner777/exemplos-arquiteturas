cenários de caracterização do AddProcedimento

como rodar:

    ./caracterizar.sh, com o contêiner lis-iris no ar
    copia src/ e cenarios.json para o contêiner, compila caracterizacao.Executor no namespace LIS e roda cada cenário
    cada cenário roda dentro de TSTART e TROLLBACK: os ajustes de dados e as gravações do método não ficam no banco
    resultado em resultados.json e log do terminal IRIS em ultima-execucao.log
    sai com erro quando algum cenário foge do valor esperado

como cada cenário chama o método:

    - como a tela de atendimento, linha 6010: pOnde = "antesVerificarDiversosMateriais" e pOrigem = 1
    - listas da tela montadas a partir de dado.TblOsProcedimento do atendimento
    - %session falsa, caracterizacao.SessaoFalsa, com Data("UsuarioId") = 1
    - proxy com Categoria do atendimento, SexoProcedimento do paciente e campos extras do cenário
    - esperado: retorno e 17 campos do proxy; datas e horas de entrega ficam fora, porque mudam a cada dia

base comum:

    - atendimento 15, unidade de coleta 2, atendimento humano, já com HEM
    - procedimento TGO, id 34, material Soro 00003, setor 003
    - fonte pagadora 1, Particular, salvo onde o cenário diz outra
    - TGO inativo e sem preço no banco local, então a base ativa o TGO e dá preço de 50 na tabela Particular

versão medida:

    branch develop, commit 831f73917fa, igual ao AddProcedimento compilado no IRIS local
    última execução: 26 cenários, 0 fora do esperado

cenários por regra:

    sem regra, caso base:

        - C00: exame simples ativo, com preço e prazo; retorno 1; Valor 50, Setor 003, IdMaterial 00003, Recipientes 4

    busca do procedimento:

        - C01: mnemônico XXXX inexistente; retorno 1; temErroMnemo 2

    sexo compatível:

        - C02a: TGO feminino, paciente masculino; retorno 1; temErroMnemo 5
        - C02b: TGO feminino, paciente sem sexo; retorno 1; temErroMnemo 5

    restrição por unidade de coleta:

        - C03: TGO restrito na unidade de coleta 2; retorno 1; temErroMnemo 4

    procedimento ativo:

        - C04a: TGO inativo; retorno 1; temErroMnemo 1
        - C04b: TGO inativo, ^; retorno ; IgnorarValidacoesOs = 1

    material diversos:

        - C05: material Soro do tipo diversos; retorno 1; verificaDiversosMaterias 00003, sem valor nem setor

    categoria:

        - C06: TGO só para animal, atendimento humano; retorno 1; temErroMnemo 3

    fatura fechada da fonte pagadora:

        - C07: fatura fechada na fonte pagadora 2, Cortesia; retorno 1; ErroDescricao FaturaFechada

    agenda específica:

        - C10a: atendimento agendado, TGO com agenda ativa; retorno 1; ErroDescricao ProcedimentoAgendaEspecifica
        - C10b: o mesmo, ConfirmouInclusaoAgendamento = 1; retorno 1; inclusão aceita

    antibiograma exige a cultura:

        - C13: TGO com TSA 3 ativo, sem o TSA no atendimento; retorno 1; ErroDescricao semCulturaAntibiograma, TsaCodigo 3

    cultura exige o antibiograma:

        - C14: TGO antibiograma, sem a cultura no atendimento; retorno 0; ErroDescricao antibiogramaPrecisaCultura

    cobertura da fonte pagadora:

        - C15: gestão de contrato: TGO não coberto, motivo Fora do rol; retorno 0; fontePagadoraNaoCobreProcedimento, NaoRealizado

    frase de autorização:

        - C17: gestão de contrato: autorização Exige guia médica; retorno 1; ObsAutorizacao com a frase, inclusão aceita

    alertas do procedimento:

        - C18: alerta de cadastro Jejum de 8 horas; retorno 1; AlertaCadastro com o alerta, inclusão aceita

    exame já incluído:

        - C19: HEM, que já está no atendimento; retorno 0; ErroDescricao exameJaIncluido

    valor do procedimento:

        - C21a: TGO sem preço na tabela Particular; retorno 0; ErroDescricao CalcularValorExame
        - C21b: gestão de contrato: negociação especial; retorno 1; IsNegociacaoEspecial 1, Valor 0

    material do procedimento:

        - C22: TGO da microbiologia, material 00002 informado; retorno 1; IdMaterial 00002, MaterialDescricao Sangue

    complemento obrigatório do material:

        - C25: Soro com complemento obrigatório, BRAÇO ESQUERDO; retorno 1; MaterialDescricao Soro braço esquerdo

    teto de valor do atendimento:

        - C26: teto do atendimento em 10, TGO custa 50; retorno 0; ErroDescricao ValorTetoExcedido

    teto de faturamento da fonte pagadora:

        - C27: teto de faturamento ligado, sem período configurado; retorno 1; ValorTetoExcedido, ErroMensagem acima do teto

    saldo em recálculo:

        - C28: teto de faturamento com saldo em recálculo; retorno 1; ValorTetoExcedido, ErroMensagem recalculando o saldo

    amostra encaminhada:

        - C30: unidade 2 encaminha todas as amostras; retorno 1; RecipienteAmostraEncaminhada 4

    a regra setor fica no C00: Setor 003 para a unidade 2

regras sem cenário:

    - fatura fechada da fonte pagadora complementar: o banco local não tem franquia compartilhada nem fonte pagadora
    complementar
    - percentual de cobrança agrupado por dia: o banco local não tem regra de percentual de cobrança
    - isolado da microbiologia sem preço: o banco local não tem isolado de microbiologia
    - segunda verificação de procedimento ativo: a regra procedimento ativo barra antes;
    com ^||IgnorarValidacoesOs as duas são puladas
    - autorização online: o banco local não tem fonte pagadora com autorização online, layout e código 97
    - prazo de entrega: o banco local não tem procedimento sem prazo; unidade de coleta 99 devolve retorno 0 sem erro no proxy
    - componentes do composto: o banco local não tem procedimento composto ativo
    - procedimento repetido no recipiente: o método sobrescreve pProxyRetorno.Exame,
    então uma chamada não repete o procedimento

comportamentos que a migração precisa manter:

    - retorno 1 com erro no proxy: temErroMnemo, FaturaFechada, ProcedimentoAgendaEspecifica, semCulturaAntibiograma,
      ValorTetoExcedido do teto de faturamento; a tela é quem decide bloquear
    - retorno 0 só em antibiograma sem cultura, sem cobertura, exame já incluído, sem preço e teto do atendimento
    - paciente sem sexo é recusado como sexo incompatível
    - material diversos interrompe sem valor e sem setor e devolve o id do material para a tela perguntar
    - Cortesia não passa pela definição de preço: o C07 roda sem ajuste de preço
    - teto de faturamento ligado sem período configurado dá saldo -1 e recusa como teto excedido
    - negociação especial aceita a inclusão com valor 0
    - complemento obrigatório entra na descrição do material em minúsculas

resumo:

    - 26 cenários, 22 regras cobertas, 8 sem dado local para montar
    - 26 passam contra o IRIS local em duas execuções seguidas
    - banco conferido depois das execuções: nenhum ajuste ficou gravado
