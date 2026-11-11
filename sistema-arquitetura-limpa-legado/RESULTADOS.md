# Resultados do sistema com arquitetura limpa

## Estado legado, tag limpa-legado

Java 8, Servlets e JSP no Tomcat 9, JDBC sobre Oracle.

| Módulo | Arquivos | Linhas não vazias | Linhas de teste |
|---|---|---|---|
| core | 24 | 395 | 609 |
| adaptador-jdbc-oracle | 6 | 168 | 199 |
| web-servlet | 13 | 459 | 496 |

Testes: 52, todos passando.

- core: 17 cenários dos casos de uso, 10 da suíte de contrato com repositórios em memória, 1 ArchUnit
- adaptador-jdbc-oracle: 10 da mesma suíte de contrato, com Oracle no Testcontainers
- web-servlet: 12 ponta a ponta, dois por caso de uso, com Oracle, e 2 ArchUnit

## Registro da migração

Cada evento é registrado quando acontece: o que deu errado, como foi percebido e o que foi alterado.

| Etapa | Evento | Como foi percebido | O que foi alterado |
|---|---|---|---|
| estado legado | encerrar conta e consultar extrato acrescentados; 52 testes passando na primeira execução | mvn package | nada |
