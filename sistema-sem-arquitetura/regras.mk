regras do fluxo: adicionar exame simples ao atendimento

origem:

    repositório do sistema legado, branch develop, commit 831f73917fa, igual ao compilado no IRIS local
    recorte aprovado na etapa 0: sub-fluxos 1, 2, 4, 5 e 7, exame simples pela tela de atendimento
    sem classe indicada, a origem é s01.iu.codigo.atendimento.cadastro.OsProcedimento
    números de linha do arquivo .cls.xml de cada classe
    retorno é o %Boolean do AddProcedimento, e os erros vão para campos do pProxyRetorno

sub-fluxo 1, identificação do procedimento:

    - busca do procedimento:
        procura pelo mnemônico, e se não achar, pelo id
        não encontrado: temErroMnemo = 2
        origem: AddProcedimento, linhas 3669, 3670 e 3737
    - sexo compatível:
        sexo do paciente vazio vale "N", e em atendimento de animal vale o sexo do animal
        compatível quando o sexo do procedimento é "A", ou "F" com paciente "F", ou "M" com paciente "M"
        incompatível: temErroMnemo = 5
        origem: AddProcedimento, linhas 3675 a 3682 e 3733
    - restrição por unidade de coleta:
        procedimento restrito para a unidade de coleta: temErroMnemo = 4
        origem: AddProcedimento, linhas 3683 a 3685 e 3729
        regra em s00.service.restricaoAcesso.procedimento.ProcedimentoAcessoService.IsProcedimentoRestritoParaUnidadeColeta
    - procedimento ativo:
        procedimento inativo: temErroMnemo = 1
        a global de processo ^||IgnorarValidacoesOs igual a 1 trata qualquer procedimento como ativo
        origem: AddProcedimento, linhas 3686 a 3688 e 3725; IgnorarValidacoesProcedimento, linhas 12398 a 12403
    - material diversos:
        procedimento da microbiologia usa o material informado, os demais usam o material do procedimento
        material do tipo diversos: devolve o id em verificaDiversosMaterias e a lista de materiais permitidos
        na tela, o método para nesse ponto para o usuário escolher o material
        origem: AddProcedimento, linhas 3690 a 3707 e 3742 a 3744
    - categoria do atendimento e tipo do procedimento:
        tipos do procedimento: 1 todos, 2 paciente, 3 animal, 4 análises especiais
        atendimento humano aceita os tipos 1 e 2, atendimento animal aceita os tipos 1 e 3
        tipo incompatível: temErroMnemo = 3
        atendimentos de outras categorias passam sem verificação
        origem: AddProcedimento, linhas 3710 a 3721

sub-fluxo 2, validações do atendimento e da fatura:

    - fatura fechada da fonte pagadora:
        fatura que não permite inclusão: ErroDescricao = FaturaFechada, retorno = 1
        origem: AddProcedimento, linhas 3748 a 3755; regra em s01.util.OrdemServico.FaturaNaoPermiteInclusaoProcedimento, linha 3148
    - fatura fechada da fonte pagadora complementar:
        a complementar vem da franquia compartilhada do atendimento
        fatura que não permite inclusão: ErroDescricao = FaturaFechadaComplementar, retorno = 1
        origem: AddProcedimento, linhas 3756 a 3765; GetFontePagadoraComplementar, linhas 28056 a 28065
    - percentual de cobrança agrupado por dia:
        quando o percentual de cobrança da fonte pagadora e do procedimento agrupa por dia,
        atendimentos do mesmo dia com fatura fechada para o procedimento bloqueiam a inclusão
        bloqueio: ErroDescricao = RegrasPercentualCobranca, ListaOs com os atendimentos, retorno = 1
        origem: AddProcedimento, linhas 3767 a 3774; ValidarRegrasPercentualCobranca, linhas 27594 a 27614
    - agenda específica:
        atendimento com agendamento e procedimento com agenda ativa: ErroDescricao = ProcedimentoAgendaEspecifica, pergunta ao usuário
        ConfirmouInclusaoAgendamento = 1 pula a pergunta
        origem: AddProcedimento, linhas 3776 a 3787, SQL em dado.TblAgendaProcedimento

sub-fluxo 4, validações clínicas e de cobertura:

    - isolado da microbiologia sem preço:
        cultura com isolado sem definição de preço: ErroDescricao = IsoladoSemDefPreco, retorno = 1
        origem: AddProcedimento, linhas 3816 a 3825; regra em s01.util.Microbiologia.VerificarExisteIsolado
    - segunda verificação de procedimento ativo:
        procedimento inativo: ErroDescricao = procedimentoInativo, retorno = 0
        repete a regra procedimento ativo com outra forma de erro, e também respeita ^||IgnorarValidacoesOs
        origem: AddProcedimento, linhas 3886 a 3892; IsProcedimentoAtivo, linhas 4571 a 4577
    - antibiograma exige a cultura:
        procedimento com TSA ativo exige o TSA entre os exames do atendimento
        sem o TSA: ErroDescricao = semCulturaAntibiograma, dados do TSA no proxy, retorno = 0
        o método segue, e a regra cobertura da fonte pagadora sobrescreve o retorno
        origem: AddProcedimento, linhas 3895 a 3902; VerificarAntibiogramaTsa, linhas 5382 a 5410
    - cultura exige o antibiograma:
        procedimento de antibiograma ativo exige no atendimento um exame que o tenha como TSA
        sem esse exame: ErroDescricao = antibiogramaPrecisaCultura, retorno = 0
        origem: AddProcedimento, linhas 3904 a 3908; VerificarAntibiogramaCultura, linhas 5412 a 5440
    - cobertura da fonte pagadora:
        CalculoPreco.Validar com status semCobertura, para o procedimento e para cada componente cobrado
        sem cobertura: ErroDescricao = fontePagadoraNaoCobreProcedimento, motivo no proxy, retorno = 0
        origem: AddProcedimento, linhas 3911 a 3915; VerificaCoberturaConvenio, linha 5511
    - autorização online:
        fonte pagadora com autorização online e sem autorização manual exige código AMB em cada cobrança
        sem código: ErroDescricao = codigoAmbNaoDefinido, retorno = 0
        origem: AddProcedimento, linhas 3918 a 3922; VerificarAutorizacaoOnline, linhas 5719 a 5767
    - frase de autorização:
        frase de autorização do procedimento para a fonte pagadora vai para ObsAutorizacao
        origem: AddProcedimento, linha 3924; VerificarProcedimentoAutorizacaoFp, linhas 5628 a 5664
    - alertas do procedimento:
        AlertaCadastro na tela e AlertaCadastroWeb na integração, devolvidos ao proxy
        origem: AddProcedimento, linha 3927; VerificarAlertasExame, linhas 5465 a 5509

sub-fluxo 5, exame simples:

    - exame já incluído:
        procedimento já na lista de exames: ErroDescricao = exameJaIncluido
        procedimento já incluído por um perfil: ErroDescricao = exameInclusoPerfil
        origem: AddProcedimento, linha 3930; VerificarExamesIncluidos, linhas 5315 a 5380
    - prazo de entrega:
        CalculoPrazo calcula o prazo a partir da unidade de coleta e da data de início do procedimento no atendimento
        erro no cálculo: ErroTipo = erroPrazo, mensagem em ErroDescricao
        origem: AddProcedimento, linhas 3933 a 3946; GetPrazoExames, linhas 5213 a 5276
    - valor do procedimento:
        CalculoPreco.Calcular com a fonte pagadora e os dados cadastrais do atendimento
        negociação especial: valor 0 e IsNegociacaoEspecial = 1
        valor -1: ErroDescricao = CalcularValorExame, e o erro sobe por exceção
        cobrança variável valida o preço de cada procedimento configurado na cobrança
        fonte pagadora com deve doc: calcula sem e com o deve doc, e as duas precisam de preço
        origem: AddProcedimento, linhas 3950 a 4016
    - material do procedimento:
        microbiologia com material informado usa esse material
        material do tipo diversos usa a descrição digitada
        origem: AddProcedimento, linhas 4018 a 4055
    - componentes do composto:
        procedimento composto ou de cobrança referenciada calcula o preço de cada componente cobrado
        componente sem preço: ErroDescricao = CalcularValorExame, por exceção
        origem: AddProcedimento, linhas 4072 a 4094
    - setor:
        setor do procedimento definido pela unidade de coleta
        origem: AddProcedimento, linha 4097; regra em s01.util.Exame.DefinirSetor
    - complemento obrigatório do material:
        material com complemento obrigatório: descrição seguida do complemento em minúsculas
        o indicador de complemento obrigatório também vai para o proxy, em MaterialComplementoObrigatorio
        origem: AddProcedimento, linhas 4148 a 4154; VerificarSeMaterialUtilizaComplementoObrigatorio, linhas 5847 a 5861

sub-fluxo 7, limites e recipiente:

    - teto de valor do atendimento:
        soma os valores da fonte pagadora no atendimento com o valor novo
        soma acima de ValorTetoOs da fonte pagadora: ErroDescricao = ValorTetoExcedido, retorno = 0
        o método segue para as verificações seguintes
        origem: AddProcedimento, linhas 4408 a 4435
    - teto de faturamento da fonte pagadora:
        vale quando a fonte pagadora usa controle de teto e bloqueia o cadastro com o teto atingido
        soma acima do saldo disponível: ErroDescricao = ValorTetoExcedido, e o retorno fica como estava
        origem: AddProcedimento, linhas 4437 a 4443
    - saldo em recálculo:
        saldo disponível igual a -2: ErroDescricao = ValorTetoExcedido, e o retorno fica como estava
        origem: AddProcedimento, linhas 4444 a 4447
    - procedimento repetido no recipiente:
        o mesmo procedimento duas vezes no mesmo recipiente: ErroDescricao = procedimentoRepetidoEmRecipiente, por exceção
        os recipientes consideram o material e a cobrança variável de cada procedimento do atendimento
        origem: AddProcedimento, linhas 4450 a 4489; TemProcedimentoRepetido, linhas 4524 a 4541
    - amostra encaminhada:
        separa os recipientes que vão como amostra encaminhada dos que não permitem, pela configuração da unidade
        origem: AddProcedimento, linhas 4491 a 4509

erro inesperado:

    exceção sem ErroDescricao preenchido: temErroMnemo = 6, retorno = 0
    origem: AddProcedimento, linhas 4511 a 4518

regras com cópia em outras classes:

    - sexo compatível:
        - s01.service.ordemServico.ValidacaoInclusaoAutomatizadaService.IsSexoProcedimentoOrdemSevicoValido, linha 329:
            mesma expressão, sem o padrão "N" para sexo vazio, sexo lido da pessoa física ou do animal
        - s01.util.b2b.lis.ensemblev2.ValidacaoIntegracao.ValidarProcedimentos, linha 88:
            compatível quando o sexo do procedimento é "A" ou igual ao do paciente
    - categoria do atendimento e tipo do procedimento:
        - ValidacaoInclusaoAutomatizadaService.IsCategoriaProcedimentoOrdemServicoValida, linhas 200 a 220:
            categoria do atendimento convertida: 1 em 2, 2 em 3, 3 em 4, 4 em 4
            procedimento do tipo 1 aceita qualquer categoria, os demais exigem o tipo convertido
            as categorias 3 e 4 são verificadas, ao contrário do AddProcedimento
    - exame já incluído:
        - ValidacaoInclusaoAutomatizadaService.ValidarProcedimentoNaoInclusoOs, linhas 342 a 360:
            lista vinda dos procedimentos do atendimento, erro por exceção
    - teto de valor do atendimento:
        - ValidacaoInclusaoAutomatizadaService.ValidarValorMaximoPedidoFontePagadora, linhas 491 a 496:
            soma acima do teto lança exceção e bloqueia a inclusão
    - teto de faturamento da fonte pagadora e saldo em recálculo:
        - ValidacaoInclusaoAutomatizadaService.ValidarValorMaximoPedidoFontePagadora, linhas 498 a 509:
            mesmas condições, cada uma lança exceção e bloqueia a inclusão
        - s01.iu.codigo.atendimento.cadastro.OsVacina.AdicionarVacina, linhas 418 a 421
        - s01.iu.codigo.atendimento.cadastro.OsPagamento.ValidarTetoFaturamentoAutorizacao, linhas 10818 a 10831

regras reaproveitadas por chamada:

    - fatura fechada da fonte pagadora: s01.service.fatura.FaturaAdapter.FaturaPermiteInclusaoProcedimento delega para OrdemServico
    - isolado da microbiologia sem preço: ValidacaoInclusaoAutomatizadaService, linha 296
    - antibiograma exige a cultura e cultura exige o antibiograma: ValidacaoInclusaoAutomatizadaService, linhas 442 e 446
    - cobertura da fonte pagadora: Orcamento, linhas 271 e 1406, e ValidacaoIntegracao, linha 70
    - autorização online: OsPagamento tem a verificação de autorização online da fonte pagadora, linhas 2490 e 3539

comportamentos ocultos:

    - as regras procedimento ativo e segunda verificação de procedimento ativo verificam o mesmo status duas vezes,
    com códigos de erro diferentes
    - as regras fatura fechada da fonte pagadora, fatura fechada da fonte pagadora complementar,
    percentual de cobrança agrupado por dia e isolado da microbiologia sem preço bloqueiam a inclusão com retorno = 1, igual ao de sucesso
    - a regra antibiograma exige a cultura marca retorno = 0, e a regra cobertura da fonte pagadora sobrescreve esse valor
    - a regra teto de valor do atendimento marca retorno = 0 e segue
    - as regras teto de faturamento da fonte pagadora e saldo em recálculo bloqueiam só pelo ErroDescricao
    - na inclusão automatizada, as regras de teto e o saldo em recálculo bloqueiam por exceção, e na tela só marcam o erro
    - a global de processo ^||IgnorarValidacoesOs desliga as regras procedimento ativo e segunda verificação de procedimento ativo,
    sem aparecer na assinatura do método

cenários previstos para a etapa 3, um por regra:

    - busca do procedimento: mnemônico inexistente, temErroMnemo = 2
    - sexo compatível: procedimento "F" para paciente "M", temErroMnemo = 5; procedimento "F" para paciente sem sexo, temErroMnemo = 5
    - restrição por unidade de coleta: procedimento restrito na unidade do atendimento, temErroMnemo = 4
    - procedimento ativo: procedimento inativo, temErroMnemo = 1; o mesmo com ^||IgnorarValidacoesOs = 1, inclusão aceita
    - material diversos: procedimento com material diversos, verificaDiversosMaterias com o id do material
    - categoria do atendimento e tipo do procedimento: procedimento de animal em atendimento humano, temErroMnemo = 3
    - fatura fechada da fonte pagadora: atendimento com fatura fechada na fonte pagadora, ErroDescricao = FaturaFechada e retorno = 1
    - fatura fechada da fonte pagadora complementar: fatura fechada na fonte pagadora complementar, ErroDescricao = FaturaFechadaComplementar
    - percentual de cobrança agrupado por dia: atendimentos do mesmo dia com fatura fechada e percentual por dia,
    ErroDescricao = RegrasPercentualCobranca
    - agenda específica: atendimento agendado e procedimento com agenda ativa, ErroDescricao = ProcedimentoAgendaEspecifica
    - isolado da microbiologia sem preço: cultura com isolado sem preço, ErroDescricao = IsoladoSemDefPreco
    - segunda verificação de procedimento ativo: procedimento inativo pelo código, sem passar pela regra procedimento ativo,
    ErroDescricao = procedimentoInativo
    - antibiograma exige a cultura: antibiograma sem o TSA no atendimento,
    ErroDescricao = semCulturaAntibiograma e retorno conforme a regra cobertura da fonte pagadora
    - cultura exige o antibiograma: antibiograma ativo sem a cultura no atendimento, ErroDescricao = antibiogramaPrecisaCultura
    - cobertura da fonte pagadora: fonte pagadora sem cobertura do procedimento, ErroDescricao = fontePagadoraNaoCobreProcedimento
    - autorização online: fonte pagadora com autorização online e procedimento sem código AMB, ErroDescricao = codigoAmbNaoDefinido
    - frase de autorização: procedimento com frase de autorização, ObsAutorizacao preenchido e inclusão aceita
    - alertas do procedimento: procedimento com alerta de cadastro, alerta no proxy e inclusão aceita
    - exame já incluído: procedimento já na lista, ErroDescricao = exameJaIncluido
    - prazo de entrega: procedimento sem prazo configurado, ErroTipo = erroPrazo
    - valor do procedimento: procedimento sem preço na tabela, ErroDescricao = CalcularValorExame; negociação especial, valor 0
    - material do procedimento: procedimento da microbiologia com material informado, IdMaterial igual ao informado
    - componentes do composto: composto com componente sem preço, ErroDescricao = CalcularValorExame
    - setor: inclusão aceita, Setor igual ao de Exame.DefinirSetor para a unidade
    - complemento obrigatório do material: material com complemento obrigatório, MaterialDescricao com o complemento em minúsculas
    - teto de valor do atendimento: soma acima do teto do atendimento, ErroDescricao = ValorTetoExcedido e retorno = 0
    - teto de faturamento da fonte pagadora: soma acima do saldo do teto de faturamento, ErroDescricao = ValorTetoExcedido e retorno = 1
    - saldo em recálculo: saldo em recálculo, ErroDescricao = ValorTetoExcedido e retorno = 1
    - procedimento repetido no recipiente: procedimento repetido no mesmo recipiente, ErroDescricao = procedimentoRepetidoEmRecipiente
    - amostra encaminhada: unidade com amostra encaminhada, RecipienteAmostraEncaminhada preenchido
    - caminho feliz: exame simples ativo, com preço e prazo, ErroDescricao vazio e retorno = 1

resumo:

    - 30 regras no recorte, mais o erro inesperado
    - 6 regras com cópia reimplementada em outras classes:
        - sexo compatível e categoria com condição diferente
        - teto de valor do atendimento, teto de faturamento da fonte pagadora e saldo em recálculo com bloqueio diferente
    - 6 comportamentos ocultos para os testes de caracterização
