# ADR 0002 — Comunicação síncrona ou por evento, entre módulos

Status: aceito · 2026-09-04 · supera: —

## Contexto

O sistema tem duas travessias de fronteira, e elas foram resolvidas de formas **opostas**
de propósito. A regra que decide não é "eventos são mais desacoplados" — é quem precisa da
resposta e quando.

| Travessia | Forma |
| --- | --- |
| pedidos → catálogo, ao adicionar item | chamada síncrona à API pública |
| pedidos → faturamento, ao confirmar | evento, assíncrono, depois do commit |

## Decisão 1 — catálogo é consultado de forma síncrona

Adicionar um item precisa saber **agora** se o produto existe e está ativo, porque a
resposta decide se o item entra. Evento aqui produziria o contrato "adicione o item e
depois eu te aviso se o produto existia", que não é utilizável: o pedido já teria seguido
adiante com um item que talvez não exista.

Regra geral: **quem precisa da resposta para decidir chama; quem só precisa reagir
escuta.**

O custo é acoplamento temporal — pedidos não funciona se catálogo estiver quebrado. Em um
único processo isso é aceitável, porque "catálogo quebrado" e "aplicação quebrada" são o
mesmo evento. Em serviços separados, essa mesma decisão exigiria timeout, circuit breaker
e um comportamento definido para degradação — que é o assunto do projeto de resiliência
da série.

## Decisão 2 — faturamento reage a evento, assíncrono, depois do commit

Emitir nota não precisa acontecer antes de responder ao cliente, e **não pode** desfazer a
confirmação do pedido se falhar. O cliente comprou; a nota sai depois.

`@ApplicationModuleListener` combina três comportamentos, e os três importam:

- **depois do commit** — não existe fatura para pedido que deu rollback;
- **transação própria** — falha ao faturar não desfaz a compra;
- **assíncrono** — o tempo de faturar não entra no tempo de resposta de quem confirmou.

A direção da dependência também é decisão: faturamento conhece pedidos, pedidos **não**
conhece faturamento. Quem publica não sabe quem escuta, e adicionar um quarto módulo que
também reage a `PedidoConfirmado` não toca em uma linha de pedidos.

## O que o evento carrega

`PedidoConfirmado` leva id do pedido, cliente, total e instante — o suficiente para o
assinante agir sem voltar a perguntar.

- Só o id economizaria payload e cobraria uma consulta de volta a pedidos em **todo**
  assinante, recriando o acoplamento que o evento deveria remover.
- Dados demais fariam o contrato do evento virar o modelo interno de pedidos, e qualquer
  mudança lá quebraria os assinantes.

O meio-termo escolhido: o evento carrega o que o negócio precisa para reagir, e não o
estado inteiro.

## Consequências

- \+ As duas formas convivem, cada uma onde faz sentido, e a razão está escrita.
- \+ Assinante novo não toca em pedidos.
- − **Perda de evento em queda do processo.** Se a aplicação cair entre o commit do pedido
  e a execução do listener, o evento se perde e o pedido fica sem fatura. Não há retry,
  não há registro persistente.

  Isso é deliberado — ver ADR 0000, decisão 4. É exatamente o problema que o padrão outbox
  resolve, e ele merece ser sentido antes de ser resolvido. Em produção, sem outbox ou sem
  o *event publication registry* do Modulith, este desenho está incompleto.
- − O listener precisa ser idempotente, porque em qualquer esquema de reentrega o mesmo
  evento chega duas vezes. `EmissorDeFaturas` verifica se já existe fatura para o pedido
  antes de emitir, e o banco tem `UNIQUE` em `pedido_id` como segunda linha de defesa.
- − Teste que envolve o listener não pode ser `@Transactional`: o rollback do teste
  impediria o `AFTER_COMMIT` de disparar, e o listener pareceria quebrado. Está anotado no
  Javadoc de `FluxoDeCompraTest`.
