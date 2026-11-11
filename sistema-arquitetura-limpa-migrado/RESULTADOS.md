# Resultados do sistema com arquitetura limpa, estado migrado

Ponto de partida: cópia do estado legado, tag `limpa-legado`, em `sistema-arquitetura-limpa-legado`.

## Registro da migração

Cada linha foi escrita no momento em que o evento aconteceu.

| Etapa | Caso de uso | O que aconteceu | Como foi percebido | O que foi alterado |
|---|---|---|---|---|
| Apresentação | transferir | `web-spring` criado com Spring Boot 3.5.16 e Java 17, perfil oracle, ligado ao `adaptador-jdbc-oracle`; 4 testes do controlador e o ArchUnit passaram na primeira execução | `mvn package` | nenhum arquivo do core |
| Apresentação | transferir | a porta 8000 do roteador já estava em uso na máquina | erro do Docker ao subir o roteador | roteador na porta 8090 |
| Apresentação | transferir | rota `/transferir` passou para a tela Angular; as outras continuam no Tomcat; 5 respostas da API conferidas pelo roteador | `curl` no roteador e saldo no Oracle | `roteador/nginx.conf` |
| Apresentação | transferir | caminho de volta: sem a rota no roteador, `/transferir` volta ao Servlet e transfere | `curl` no roteador | a rota foi devolvida ao Angular |
| Apresentação | abrir conta, depositar, sacar, encerrar conta, consultar extrato | 9 dos 12 testes novos de controlador falharam: o Spring não achou o nome do parâmetro `{id}` | `mvn package`: "parameter name information not available via reflection" | `web-spring/pom.xml`: compilador com `-parameters`, que o `spring-boot-starter-parent` liga e o projeto não usa |
| Apresentação | os cinco | rotas migradas uma de cada vez no roteador, cada uma conferida pela API antes da seguinte | `curl` no roteador | `roteador/nginx.conf` |
| Apresentação | depositar, sacar, transferir | a data da movimentação passou a ser gravada com microssegundos (`08:41:52.503456`); o Tomcat com Java 8 grava milissegundos (`08:42:02.092`) | extrato pela API | nenhum: o mesmo `RelogioDoSistema` roda em outra JVM; a regra não usa a fração do segundo |
| Apresentação | todos | suíte completa: core 28, contrato JDBC 10, Servlet 14, web-spring 16, 68 testes aprovados | `mvn clean package` | nenhum |
| Persistência | todos | `adaptador-jpa-postgres` criado; a suíte de contrato do core rodou contra o PostgreSQL: 1 falha em `descricaoVaziaEGravadaComoNula` | `mvn package`: esperado nulo, lido texto vazio | `MovimentacaoRepositoryJpaPostgres` converte texto vazio em nulo; no Oracle o texto vazio já é gravado como nulo pelo próprio banco |
| Persistência | todos | 6 dos 10 testes de contrato não chegaram a rodar: o segundo contexto de teste tentou criar as tabelas de novo | `mvn package`: "Failed to execute SQL script statement #1" | só o teste: o esquema passou a ser criado uma vez, na subida do container |
| Persistência | todos | suíte de contrato no PostgreSQL: 10 testes aprovados, os mesmos 10 do Oracle | `mvn package` | nenhum |
| Persistência | todos | os 16 testes de controlador rodaram nos perfis oracle e postgres, sem alteração nos testes | `mvn package`, duas execuções do surefire | só a classe base dos testes escolhe o container pelo perfil |
| Persistência | todos | dados copiados tabela por tabela, primeiro `conta`, depois `movimentacao`, com o web-spring parado; contagem, somas e nulos conferidos, e as 10 linhas iguais linha a linha | `banco/migrar-dados.sh` e comparação das duas consultas | `seq_movimentacao` ajustada para continuar do último id |
| Persistência | todos | web-spring no perfil postgres: as respostas da API pelo roteador iguais às do perfil oracle; a próxima movimentação recebeu o id 7, depois das 6 migradas | `curl` no roteador e consulta no PostgreSQL | variável `PERFIL` no `docker-compose.yml` |
| Persistência | todos | caminho de volta: o perfil oracle sobe e responde, mas a conta aberta e as movimentações feitas depois da troca não estão no Oracle | `curl` no roteador: "conta nao encontrada" e total 4 em vez de 6 | nenhum: voltar exige copiar de volta o que foi gravado no PostgreSQL |
| Persistência | todos | suíte completa: core 28, contrato JDBC 10, contrato JPA 10, Servlet 14, web-spring 16 no Oracle e 16 no PostgreSQL, 94 testes aprovados | `mvn clean package` | nenhum |
| Remoção do legado | todos | `adaptador-jdbc-oracle` e `web-servlet` retirados do projeto, junto com o perfil oracle, a `MontagemJdbcOracle` e os serviços oracle e tomcat do docker compose; `RelogioDoSistema` e `GeradorDeIdUuid` movidos para o `adaptador-jpa-postgres` | `mvn clean package` | o roteador manda todas as telas para o Angular; a classe base dos testes passou a ser `PostgresNoContainer` |
| Remoção do legado | todos | suíte completa: core 28, contrato JPA 10, web-spring 16, 54 testes aprovados | `mvn clean package` | nenhum teste de caso de uso ou de controlador alterado |
| Remoção do legado | todos | a conferência da migração falhou com a tabela `movimentacao` vazia: no PostgreSQL a soma de uma tabela vazia concatenada dá nulo, no Oracle dá texto vazio | `banco/migrar-dados.sh`: "movimentacao: diferente, Oracle 0\|\|0, PostgreSQL" | `COALESCE` na soma das consultas de conferência |
| Remoção do legado | todos | dados lidos do Oracle do `sistema-arquitetura-limpa-legado`: 3 contas e 2 movimentações iguais nos dois bancos; extrato, transferência e telas Angular respondendo pelo roteador | `./executar.sh migrar` e `curl` no roteador | `banco/migrar-dados.sh` lê o Oracle pelo docker compose do legado |

## Etapa da apresentação

- core, `adaptador-jdbc-oracle` e `web-servlet` iguais ao estado legado: `diff -r` sem diferença
- testes dos casos de uso sem alteração
- caminho de volta: tirar a rota do caso de uso de `roteador/nginx.conf` e recarregar o nginx

| Módulo novo | Arquivos | Linhas não vazias | Linhas de teste |
|---|---|---|---|
| web-spring | 20 | 302 | 250 |
| frontend | 11 | 269 | 0 |
| roteador | 1 | 31 | 0 |

## Etapa da persistência

- core igual ao estado legado e à tag `limpa-apresentacao`: `diff -r` sem diferença
- testes dos casos de uso e dos controladores sem alteração; a suíte de contrato do core é a mesma para Oracle e PostgreSQL
- `web-spring`: 2 arquivos alterados, a configuração e a classe base dos testes, e 4 novos: `MontagemJpaPostgres`, as propriedades de cada perfil e os dados de teste
- o gerador de id e o relógio continuam no módulo `adaptador-jdbc-oracle` e são usados também pelo perfil postgres
- caminho de volta: `PERFIL=oracle`, com os dados gravados depois da troca copiados de volta

| Módulo novo | Arquivos | Linhas não vazias | Linhas de teste |
|---|---|---|---|
| adaptador-jpa-postgres | 8 | 174 | 161 |
| banco/migrar-dados.sh | 1 | 49 | 0 |

## Remoção do legado

- o projeto ficou com core, `adaptador-jpa-postgres`, `web-spring`, `frontend` e roteador
- core igual ao do `sistema-arquitetura-limpa-legado`: `diff -r` com diferença só no nome do projeto pai no `pom.xml`
- o Oracle fica só no `sistema-arquitetura-limpa-legado`, como origem dos dados da migração

| Módulo retirado | Arquivos | Linhas não vazias |
|---|---|---|
| adaptador-jdbc-oracle | 11 | 437 |
| web-servlet | 21 | 1018 |
