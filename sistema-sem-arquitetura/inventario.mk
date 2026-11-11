inventário do fluxo: adicionar procedimento ao atendimento

origem:

    repositório do sistema legado, branch develop, commit 831f73917fa, igual ao compilado no IRIS local
    classe s01.iu.codigo.atendimento.cadastro.OsProcedimento, arquivo s01/s01.iu.codigo.atendimento.cadastro.OsProcedimento.cls.xml
    método AddProcedimento, linhas 3641 a 4522 do arquivo
    números de linha abaixo são do arquivo .cls.xml

pontos de entrada:

    - tela de atendimento: três chamadas em JavaScript na própria OsProcedimento, linhas 2713, 6010 e 21405
    - integração B2B: s01.util.b2b.lis.ensemble.Pedido, linhas 1059 e 1549
    - integração B2B v2: s01.util.b2b.lis.ensemblev2.Pedido, linha 1581
    - inclusão automatizada: s01.service.ordemServico.InclusaoAutomatizadaService, linha 585
    - testes de integração existentes: test.integrationtest.adapter.OsProcedimentoAdapter e test.mother.OrdemServicoMother

caminho da tela até o banco:

    página Zen OsProcedimento
    método JavaScript da página chama o ZenMethod AddProcedimento no servidor
    AddProcedimento lê e grava pelo objeto pProxyRetorno, que volta para a página
    os dados vêm de objetos persistentes dado.* e de SQL dinâmico com %SQL.Statement

assinatura:

    - ClassMethod e ZenMethod, retorna %Boolean
    - 24 parâmetros, entre eles:
        - pProxyRetorno, %ZEN.proxyObject: entrada e saída da tela, inclusive os erros
        - oito listas %ListOfDataTypes montadas pela tela: exames, perfis, fontes pagadoras, dados cadastrais e valores
        - pOnde e pOrigem: mudam o comportamento conforme quem chama
    - erros devolvidos em campos do proxy: temErroMnemo, de 1 a 6, e ErroDescricao, com 15 códigos de texto fixos e a mensagem de erro do prazo

medições do método:

    - 882 linhas no arquivo, 874 de código
    - 104 If e 2 consultas SQL dinâmicas
    - linhas por tipo, classificação por palavras-chave:
        - regra de negócio e cálculo: 339, 38,8%
        - estrutura e comentário: 251, 28,7%
        - tela: proxy, listas da tela, formato de exibição e sessão: 232, 26,5%
        - dados: SQL, objetos persistentes e globais: 34, 3,9%
        - erro e infraestrutura: 18, 2,1%
        - transação: 0
    - linhas que gravam o resultado de uma regra no proxy contam como tela

fechamento transitivo:

    medido por medicao/fechamento.py, seguindo chamadas ..Metodo() e ##class(Classe).Metodo() até o fim

    - 88 métodos, 3.565 linhas, 21 classes
    - profundidade máxima de 5 chamadas:
        - nível 1: 33 métodos
        - nível 2: 26
        - nível 3: 15
        - nível 4: 9
        - nível 5: 4
    - métodos por classe:
        - OsProcedimento: 40
        - s01.util.Exame: 13
        - shared.Lista, s00.util.DataHora e shared.util.Padrao: 4 cada
        - s01.util.OrdemServico e dado.Persistente: 3 cada
        - dado.Padrao, CalculoPreco e s00.util.Numero: 2 cada
        - Microbiologia, TblUnidadeExameRestricao, OsFontePagadora, OsPagamento e PrazoExames: 1 cada
    - maiores métodos percorridos:
        - AddProcedimento: 876 linhas
        - Exame.BuscarInfoPrecoAdicional: 140
        - OsProcedimento.GetInfoTabelaRecipiente: 124
        - OsProcedimento.PreencherListaTabelaRecipienteComMaterial: 117
        - Exame.BuscarCorrelacaoTuss: 112
    - dados acessados:
        - 28 classes persistentes dado.*, entre elas TblExame, TblConvenio, TblMaterial, TblOsProcedimento, TblPreco e TblTuss
        - 12 tabelas em SQL dinâmico, entre elas TblPrecoItem, TblOsCobranca, TblAgendaProcedimento e TblExameMateriaisPermitidos
        - 2 globais acessadas direto: ^FusoHorario e ^RestricaoUnidadeExame
    - limite da medição: chamadas em variáveis de objeto, como obj.Metodo(), ficam fora, então os números são o mínimo

sub-fluxos do AddProcedimento:

    - 1, identificação do procedimento, linhas 3668 a 3746:
        busca por mnemônico, sexo compatível, restrição por unidade, status ativo, material diversos, categoria humano ou animal
        erros em temErroMnemo, de 1 a 5
    - 2, validações do atendimento e da fatura, linhas 3747 a 3792:
        fatura fechada da fonte pagadora e da complementar, regras de percentual de cobrança, agenda específica do agendamento
    - 3, orçamento e exame composto, linhas 3828 a 3884:
        listas separadas por $CHAR(250), $CHAR(251) e $CHAR(252), no formato que a tela monta
    - 4, validações clínicas e de cobertura, linhas 3815 a 3826 e 3886 a 3928:
        isolado da microbiologia sem preço, procedimento ativo, antibiograma e cultura, cobertura da fonte pagadora,
        autorização online, mensagens de autorização e alertas
    - 5, exame simples, linhas 3929 a 4158:
        exames já incluídos, prazo de entrega, cálculo do valor com CalculoPreco, material, setor, cobrança variável e complemento
    - 6, perfil de exames, linhas 4159 a 4428:
        repete o cálculo de prazo, valor, material e setor para cada exame do perfil
    - 7, limites e recipiente, linhas 4429 a 4511:
        teto de valor do atendimento, teto de faturamento da fonte pagadora, procedimento repetido no recipiente, amostra encaminhada

recorte aprovado:

    adicionar um exame simples a um atendimento existente, pela tela de atendimento

    - entram os sub-fluxos 1, 2, 4, 5 e 7
    - ficam para depois o 3, orçamento, e o 6, perfil
    - pontos de entrada no recorte: tela de atendimento e testes de integração existentes
