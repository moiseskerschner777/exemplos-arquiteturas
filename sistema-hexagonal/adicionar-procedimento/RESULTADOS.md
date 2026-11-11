# Resultados da migração de adicionar procedimento ao atendimento

Origem: método `OsProcedimento.AddProcedimento` do sistema legado em ObjectScript com IRIS.
Destino: Java 17 com arquitetura hexagonal, Spring Boot 3.5.16, PostgreSQL 16 e Angular 20 com PrimeNG 20.

Recorte: adicionar um exame simples a um atendimento existente, sub-fluxos 1, 2, 4, 5 e 7.

## Inventário do lado legado

Fonte: `sistema-sem-arquitetura/inventario.mk`, medido por `sistema-sem-arquitetura/medicao/fechamento.py`.

| Medida | Valor |
|---|---|
| Linhas do método de entrada | 882, 874 de código |
| `If` e consultas SQL dinâmicas no método | 104 e 2 |
| Parâmetros do método | 24, entre eles o proxy da tela e oito listas da tela |
| Fechamento transitivo | 88 métodos, 3.565 linhas, 21 classes, profundidade 5 |
| Classe do método | 28.418 linhas, 622 métodos |
| Dados acessados | 28 classes persistentes, 12 tabelas em SQL dinâmico, 2 globais |

Linhas do método por tipo:

| Tipo | Linhas | % |
|---|---|---|
| Regra de negócio e cálculo | 339 | 38,8 |
| Estrutura e comentário | 251 | 28,7 |
| Tela: proxy, listas, formato de exibição e sessão | 232 | 26,5 |
| Dados: SQL, objetos persistentes e globais | 34 | 3,9 |
| Erro e infraestrutura | 18 | 2,1 |
| Transação | 0 | 0 |

## Linhas lidas no legado para entender o fluxo

| Leitura | Métodos | Linhas |
|---|---|---|
| Fechamento transitivo do método de entrada | 88 | 3.565 |
| Cálculo do prazo de entrega, lido na comparação | 26 | 1.016 |
| Total | 114 | 4.581 |

O cálculo do prazo de entrega fica fora do fechamento medido, porque é chamado por variável de objeto.
Fonte da segunda linha: `medicao/medir.py`.

## Regras

Fonte: `sistema-sem-arquitetura/regras.mk`.

- 30 regras no recorte, mais o erro inesperado
- 6 regras com cópia reimplementada em outras classes:
  - sexo compatível e categoria, com condição diferente
  - teto de valor do atendimento, teto de faturamento da fonte pagadora e saldo em recálculo, com bloqueio diferente
- 6 comportamentos ocultos registrados antes da migração

## Cenários de caracterização

Fonte: `sistema-sem-arquitetura/caracterizacao/cenarios.mk`.

- 32 cenários, 23 regras cobertas, 7 regras sem dado local para montar um cenário
- 6 cenários de prazo de entrega, C40 a C45, acrescentados na comparação
- 32 passam contra o IRIS, dentro de transação desfeita ao fim de cada cenário
- 32 passam contra o Java com PostgreSQL, com o banco restaurado ao fim de cada cenário

## Banco

Fonte: `banco/migrar.py` e `banco/conferir.py`.

| Medida | Valor |
|---|---|
| Tabelas migradas | 57 |
| Linhas migradas | 21.775 |
| Índices recriados | 249, 54 únicos, 100 com `upper()` para comparar sem diferenciar maiúsculas |
| Tabelas com sequência no id | 42 |
| Datas e horas em `$Horolog` convertidas | 103 |
| Números devolvidos pelo IRIS em colunas de texto | 21.987 |
| Tabelas com diferença entre IRIS e PostgreSQL | 0 |

## Diferenças entre legado com IRIS e Java com PostgreSQL

Fonte: `caracterizacao/comparar.py`, que compara a resposta inteira dos dois lados, campo a campo.

| Diferença | Causa | Decisão |
|---|---|---|
| A entrega devolvia o prazo mínimo em 14 dos 26 cenários | Regra: o campo de prazo é o prazo em dias quando o procedimento não usa rotina, e o cálculo usa dias úteis, dias de produção, dias de entrega, horário limite e feriados | Cálculo portado para `CalculoDePrazo` no core; prazo mínimo entra no contrato |
| Horário de entrega 3 horas adiantado | Tecnologia: o container da aplicação rodava em UTC, e o IRIS no fuso -03 | Fuso America/Sao_Paulo no container |
| Prazo gravado como texto vazio pelo SQL | Banco: o IRIS guarda o texto vazio como `$C(0)`, e o legado trata o prazo como inválido | O core trata nulo como prazo ausente e texto vazio como prazo inválido |
| Material diversos | Tecnologia: a tela troca o material da linha no navegador, sem chamar o método de novo | A descrição do material informada resolve a pendência no core |
| 39 campos do proxy sem correspondente no contrato | Estado da tela e partes fora do recorte | Ficam fora do contrato |

Diferenças tratadas na migração do banco, antes da comparação:

- texto vazio e nulo: nenhum campo migrado com texto vazio
- comparação de texto: o IRIS compara sem diferenciar maiúsculas
- datas e horas em `$Horolog`
- números guardados como texto e texto devolvido como número
- globais lidas direto, recalculadas de tabelas

Resultado final: 32 cenários, 0 diferenças.

Os 26 cenários passavam no Java antes da comparação com a entrega errada, porque o esperado dos cenários não tinha a entrega.
A diferença só apareceu ao comparar a resposta inteira.

## Erros da migração, na ordem em que apareceram

| Erro | Como foi percebido | Causa e decisão |
|---|---|---|
| Código medido no IRIS local anterior à versão em desenvolvimento | Comparação do método compilado com o repositório | Medidas refeitas: método de 857 para 882 linhas, fluxo de 82 para 88 métodos |
| Carga recusada pelo PostgreSQL: hora gravada como 54291 | Erro na carga | Hora no formato interno do IRIS convertida na migração |
| 25 das 45 tabelas diferentes depois da carga | `banco/conferir.py` | Números como texto e datas no formato interno; conversão pelo tipo da coluna |
| Índices com o mesmo nome | Erro na criação do esquema | Nomes cortados em 63 caracteres; sufixo no nome |
| Negociação especial com valor vazio em vez de 0 | 1 cenário fora do esperado no Java | Tradução feita com IA; corrigida |
| Valores de teste gravados no PostgreSQL | `banco/conferir.py`: 3 tabelas diferentes | Valores restaurados |
| Data de entrega do resultado do exame ao paciente igual ao prazo mínimo em 14 de 26 cenários | `caracterizacao/comparar.py` | Cálculo do prazo portado; 3 tabelas a mais |
| Horário de entrega 3 horas adiantado (14:51 no Java, 11:53 depois do fuso) | A mesma comparação | Fuso America/Sao_Paulo no container |
| Cenário do horário limite recusado pelo IRIS, SQLCODE -105 | Ao montar o cenário | Coluna de hora guarda segundos; cenário usa o número |
| Prazo gravado como texto vazio dá erro no legado | Resultado do legado diferente do esperado | Core diferencia nulo e texto vazio |

## Código do fluxo migrado

Fonte: `medicao/medir.py`. Linhas não vazias.

| Módulo | Arquivos | Linhas | Não vazias |
|---|---|---|---|
| core, domínio | 26 | 1.005 | 844 |
| core, caso de uso | 2 | 435 | 395 |
| core, portas | 18 | 240 | 150 |
| core, erros | 2 | 88 | 71 |
| core, testes | 3 | 581 | 467 |
| adaptador-postgres | 16 | 776 | 620 |
| rest | 8 | 205 | 164 |
| rest, testes | 1 | 32 | 25 |
| frontend | 8 | 540 | 487 |
| banco: migração e conferência | 3 | 441 | 369 |
| caracterização e comparação | 2 | 358 | 306 |

- regra no core: 1.239 linhas não vazias, domínio e caso de uso; o cálculo do prazo sozinho tem 473 linhas
- o core depende só de `java.lang`, `java.util`, `java.time` e `java.math`, conferido pelo `ArquiteturaTest`
- a tela, o banco e o contrato HTTP ficam fora do core: 1.271 linhas não vazias nos adaptadores e na tela

## Como reproduzir

```
./executar.sh subir
./executar.sh migrar
./executar.sh conferir
./executar.sh caracterizar
python3 caracterizacao/comparar.py
python3 medicao/medir.py
```
