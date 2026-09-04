# ADR 0000 — Decisões base do projeto

Status: aceito · 2026-09-04 · supera: —

## Contexto

O projeto demonstra três bounded contexts convivendo em um único deploy, com a fronteira
entre eles **verificada pelo build** — não documentada num README que ninguém lê.

## Decisões

### 1. Spring Modulith, e não módulos Maven

O projeto anterior da série já usa módulos Maven para separar camadas. Aqui a separação é
outra: contextos de negócio dentro do mesmo artefato.

Spring Modulith resolve dois problemas que módulos Maven não resolvem bem neste caso:

- **Interface nomeada.** Faturamento pode depender de `pedidos :: eventos` — o pacote de
  eventos — e de mais nada de pedidos. Com módulos Maven, isso exigiria quebrar pedidos em
  dois artefatos (`pedidos-api-eventos` e `pedidos`), o que é muita cerimônia para
  expressar "pode ouvir, não pode mandar".
- **Granularidade.** Três contextos que crescem juntos e são deployados juntos não
  justificam três artefatos com ciclo de build próprio.

Custo aceito: a fronteira é verificada por **teste**, e não pelo compilador. Um import
proibido compila e só quebra quando a suíte roda. Módulos Maven dariam erro de
compilação. Para o objetivo aqui — mostrar contextos e a comunicação entre eles — o teste
basta, desde que rode no CI e bloqueie merge.

### 2. Sem camada web

Não há REST. O projeto é sobre a fronteira entre módulos, e um controller só acrescentaria
superfície sem mudar nada do que se demonstra. O fluxo completo roda pelo
`DemonstracaoDeUso` e pelos testes.

### 3. H2 com três schemas, e `schema.sql` versionado

Um schema por contexto — `catalogo`, `pedidos`, `faturamento` — para que a separação seja
visível no banco e não só no código. Sem `ddl-auto`.

H2 aqui não mente sobre nada relevante: o que importa é a ausência de FK e de join entre
schemas, e isso é estrutural. Banco real com Testcontainers é assunto do projeto de
testes de integração da série.

### 4. Sem registro persistente de eventos

Spring Modulith oferece um *event publication registry* que grava os eventos em tabela e
os reprocessa depois de uma queda. Ele **não** foi ligado aqui, de propósito: seria
resolver o problema do outbox pela metade, com infraestrutura escondida atrás de uma
dependência.

A consequência está registrada no ADR 0002 e no README: se o processo cair entre o commit
do pedido e a execução do listener, o pedido fica sem fatura. Esse é exatamente o problema
do projeto de outbox da série, e ele merece ser sentido antes de ser resolvido.

### 5. Clock injetado

`ServicoDePedidos` recebe um `Clock`. Nenhum ponto do código chama `Instant.now()` direto.

## Consequências

- O repositório demonstra fronteira e comunicação, e não persistência nem API.
- A garantia de entrega do evento é fraca **de propósito**, e está dita em três lugares
  para ninguém copiar isto para produção achando que está resolvido.
