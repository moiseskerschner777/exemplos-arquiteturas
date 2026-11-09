# Resultados: sistema sem arquitetura definida, seis casos de uso

Data da execução: 2026-11-09.
Plano de referência: `utils/studying/codigo/plano-sistema-sem-arquitetura.mk`.

## O que foi construído

Casos de uso nos dois estados: abrir conta, depositar, sacar, transferir, encerrar conta e consultar extrato.

| Estado | Tecnologias | Um caso de uso é | Linhas de código de produção |
|---|---|---|---|
| Legado | Java 8, Servlet, JSP, JDBC, Oracle | Um Servlet e um JSP | 629 em seis Servlets, mais os JSPs e o `schema.sql` |
| Migrado | Java 17, Spring Boot 3.3.5, JPA, PostgreSQL | Um controlador REST | 431 em onze classes, incluindo entidades e repositórios |

Nos dois estados a regra de negócio fica dentro do método que trata a requisição.
No legado cada Servlet abre a própria conexão, controla a transação e escreve o SQL.
No migrado cada controlador usa os repositórios JPA e `@Transactional`.

## Verificação

| Item | Resultado |
|---|---|
| Legado, compilação e geração do `.war` | Passou |
| Legado, cenários contra Oracle real (Testcontainers, `gvenzl/oracle-free:23-slim-faststart`) | 12 de 12 passaram |
| Migrado, compilação | Passou |
| Migrado, cenários contra PostgreSQL real (Testcontainers, `postgres:16`) | 16 de 16 passaram |

### Cenários por caso de uso

| Caso de uso | Cenários | Legado | Migrado |
|---|---|---|---|
| Abrir conta | Com titular abre com saldo zero e situação ATIVA; sem titular é recusado | Passou | Passou |
| Depositar | Aceita; valor zero, conta encerrada e conta inexistente são recusados | Passou | Passou |
| Sacar | Aceita; saldo insuficiente e conta encerrada são recusados | Passou | Passou |
| Transferir | Aceita; saldo insuficiente, valor zero, origem igual ao destino, conta encerrada e destino inexistente são recusados | Passou | Passou |
| Encerrar conta | Sem saldo encerra; com saldo e já encerrada são recusados | Passou | Passou |
| Consultar extrato | Paginação com três movimentações, páginas de tamanho 2, na ordem de registro | Passou | Passou |

### Diferenças de comportamento observadas entre os bancos

| Item | Oracle (legado) | PostgreSQL (migrado) |
|---|---|---|
| Descrição vazia gravada na movimentação | Gravada como `NULL` | Gravada como vazia |
| Paginação do extrato | `ROWNUM` com subconsultas aninhadas | `Pageable` do Spring Data, gerado como `LIMIT` e `OFFSET` |
| Geração do id da movimentação | `seq_movimentacao.NEXTVAL` no SQL | `@SequenceGenerator` na entidade |

A diferença da descrição vazia foi observada nos testes do Oracle.
No migrado, o teste da descrição vazia (`mantemDescricaoVazia`) confirma que ela é gravada como vazia.

## Análise da migração sistema

Esta seção vem da leitura do código. Entre os itens ocultos, só a descrição vazia foi exercitada nos testes.

### O que o desenvolvedor precisa cuidar ao ler

- **Regra repetida:** "valor maior que zero" está em 3 Servlets e "conta encerrada" em 4.
- **Regra misturada:** as validações ficam entre `SELECT`, `rollback` e `forward`, e a ordem delas define o erro exibido.
- **Contrato implícito:** o legado responde sempre HTTP 200, com o erro na página; o migrado usa 422 e 404.

### O que pode passar despercebido (compila e os cenários passam)

| Item | Legado | Migrado | Risco |
|---|---|---|---|
| Trava da conta | `FOR UPDATE` | `@Lock` | Trocar por `findById` tira a trava, e nenhum teste de concorrência acusa |
| Erro de regra | `rollback` explícito | `return` dentro de `@Transactional` faz commit | Se o saldo for alterado antes de validar, a alteração é gravada |
| Data da movimentação | `SYSTIMESTAMP`, relógio do banco | `LocalDateTime.now()`, relógio da aplicação | Horário pode mudar por fuso |
| Descrição vazia | `NULL`, e o extrato mostra "null" | Vazia | Muda o que o usuário vê |
| Sequência | `NEXTVAL` | `allocationSize = 1` | Valor errado, ou sequência sem ajuste após migrar dados, gera ids errados ou repetidos |
| Valor inválido | Mensagem "valor invalido" | 400 padrão do Spring, sem o campo `erro` | A tela perde a mensagem |

### Dificuldades na migração

- Cada caso reescreve HTTP, SQL, transação e regra de uma vez, sem separar o que é regra.
- Corrigir uma regra exige lembrar de todas as cópias, inclusive nos dois sistemas durante a convivência.
- Não existe hoje o estado intermediário de Spring com Oracle, necessário para migrar a tela antes do banco.

### Migração e teste caso a caso

| Pergunta | Sem arquitetura definida |
|---|---|
| Migrar a tela de um caso? | Sim, cada Servlet tem URL própria, mas reescrevendo o método inteiro, inclusive o SQL |
| Migrar o banco de um caso? | Não, a tabela `conta` é compartilhada |
| Testar um caso? | Sim, por HTTP ou Servlet, com banco real |
| Testar só a regra? | Não |
| O teste continua igual após migrar? | Não |

`CenariosLegadoTest` e `CenariosMigradoTest` são arquivos diferentes, com 12 e 16 testes. Um verifica atributos da requisição e o outro, status HTTP, então a equivalência depende de alguém comparar os dois.

## Pendências em relação ao plano

- Cenário "falha ao salvar o destino": exige simular falha no banco e não foi escrito.
- Marcação de cada linha por tipo (HTTP, SQL, transação, regra de negócio), que alimenta a medição de linhas de regra tocadas.
- Registro da migração: o `migrado` foi escrito a partir do `legado` em paralelo, sem histórico git, então não há diff entre os dois estados nem tags.
- Migração de dados do Oracle para o PostgreSQL, que não foi feita.
- Angular: existe somente o componente do `Transferir`, sem os dos outros cinco casos de uso e sem o projeto Angular em volta. A tela não foi executada.
- Medições com IA e tempo gasto em cada fase.

## Observações da execução

- O Testcontainers não encontrou o Docker desta máquina (API 1.56) com as versões do Spring Boot 3.3.5. Foi preciso usar Testcontainers 1.21.3 e rodar `mvn test -DargLine="-Dapi.version=1.44"`.
- A imagem do Oracle tem 5,11 GB e a primeira execução precisou baixá-la.
- Os testes do legado chamam os Servlets diretamente, com requisição simulada e Oracle real. Eles não passam pelo Tomcat nem renderizam os JSPs.
- Dois erros de configuração dos testes apareceram no caminho e foram corrigidos: o `commit` com auto-commit ligado no preparo do banco e o `TransferenciaServlet` que lia a conexão só de variável de ambiente.
