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

## Etapa da apresentação

- core, `adaptador-jdbc-oracle` e `web-servlet` iguais ao estado legado: `diff -r` sem diferença
- testes dos casos de uso sem alteração
- caminho de volta: tirar a rota do caso de uso de `roteador/nginx.conf` e recarregar o nginx

| Módulo novo | Arquivos | Linhas não vazias | Linhas de teste |
|---|---|---|---|
| web-spring | 20 | 302 | 250 |
| frontend | 11 | 269 | 0 |
| roteador | 1 | 31 | 0 |
