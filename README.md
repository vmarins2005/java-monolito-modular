# Monólito modular — três contextos, um deploy

Projeto de estudo: **três bounded contexts convivendo em um único artefato, com a
fronteira entre eles verificada pelo build.** Catálogo, pedidos e faturamento — cada um
com schema próprio, sem join e sem chave estrangeira atravessando contexto.

Quarto de uma série em que cada repositório isola um conceito.

## O que este projeto prova

- Fronteira entre contextos que **quebra o build** quando alguém a atravessa — não uma
  convenção documentada.
- Comunicação síncrona e por evento convivendo, cada uma onde faz sentido, com a regra
  escrita.
- Duplicação deliberada de dados entre contextos, e por que ela está certa.
- Um desenho que não impede a migração para microserviços: **prepara**.

## Como rodar

```bash
./mvnw test
```

9 testes. Sem Maven instalado (wrapper versionado), sem container, sem banco externo.
Só JDK 21.

O fluxo completo, com saída no console:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=demo
```

## Os módulos

```
io.github.vmarins2005.loja
├── catalogo          depende de: nada
│   ├── Catalogo, ProdutoResumo          ← API pública
│   └── internal/                        ← invisível para os outros módulos
├── pedidos           depende de: catalogo
│   ├── Pedidos, PedidoResumo            ← API pública
│   ├── eventos/  @NamedInterface         ← "pode ouvir isto aqui"
│   └── internal/
└── faturamento       depende de: pedidos :: eventos
    ├── Faturamento, FaturaResumo
    └── internal/
```

A declaração está em `package-info.java` de cada módulo. Faturamento pode referenciar
`PedidoConfirmado` e **nada mais** de pedidos — chamar `Pedidos.confirmar()` de dentro de
faturamento não passa no build. É a diferença entre *pode ouvir* e *pode mandar*,
declarada e verificada.

## A fronteira é verificada, não prometida

`EstruturaDosModulosTest.fronteirasRespeitadas` roda `ApplicationModules.verify()`.

Isso não é teoria. Injetando `Pedidos` dentro de `faturamento.internal.EmissorDeFaturas`,
o build reprova com:

```
Module 'faturamento' depends on module 'pedidos' via
  io.github.vmarins2005.loja.faturamento.internal.EmissorDeFaturas
  -> io.github.vmarins2005.loja.pedidos.Pedidos.
Allowed targets: pedidos :: eventos.
```

Vale fazer o teste: é o que separa "monólito modular" de "monólito com pastas
organizadas". Sem esse teste no CI bloqueando merge, a fronteira dura até a primeira
sexta-feira apertada.

## As duas travessias, resolvidas de formas opostas

| Travessia | Forma | Por quê |
| --- | --- | --- |
| pedidos → catálogo | chamada síncrona | precisa da resposta **agora** para decidir se aceita o item |
| pedidos → faturamento | evento assíncrono, após commit | só precisa reagir; falhar não pode desfazer a compra |

A regra: **quem precisa da resposta para decidir chama; quem só precisa reagir escuta.**
"Evento é mais desacoplado" não é critério — evento no lugar errado produz o contrato
"adicione o item e depois eu te aviso se o produto existia", que ninguém consegue usar.

Detalhe da direção: faturamento conhece pedidos; pedidos **não** conhece faturamento. Um
quarto módulo que também reaja a `PedidoConfirmado` entra sem tocar uma linha de pedidos.

## A duplicação é deliberada

Ao entrar no pedido, `nome` e `precoUnitario` são **copiados** do catálogo. Sem FK, sem
join.

Não é falta de normalização: **não é o mesmo dado.** `catalogo.produto.preco` é "quanto
custa hoje"; `pedidos.item_pedido.preco_unitario` é "quanto custou naquela compra". Com
join, um reajuste no catálogo mudaria retroativamente o valor de pedidos já faturados.

O teste `precoECopiadoNaInclusao` prova: item a R$ 100,00, catálogo reajustado para
R$ 300,00, pedido continua valendo R$ 100,00.

## Decisões registradas

| ADR | Assunto |
| --- | --- |
| [0000](docs/adr/0000-decisoes-base-do-projeto.md) | Stack, escopo e o que ficou de fora |
| [0001](docs/adr/0001-por-que-monolito-modular-em-vez-de-microservicos.md) | Monólito modular em vez de microserviços |
| [0002](docs/adr/0002-comunicacao-sincrona-ou-por-evento-entre-modulos.md) | Comunicação síncrona ou por evento |
| [0003](docs/adr/0003-duplicacao-deliberada-de-dados-do-catalogo-em-pedidos.md) | Duplicação deliberada entre contextos |

## Dívidas assumidas

- **O evento se perde se o processo cair.** Entre o commit do pedido e a execução do
  listener não há garantia nenhuma: sem retry, sem registro persistente. O pedido fica sem
  fatura e ninguém fica sabendo.

  É deliberado. Spring Modulith tem um *event publication registry* que resolveria parte
  disso com uma dependência, e ligá-lo aqui seria resolver o problema do outbox pela
  metade, com a infraestrutura escondida. O padrão outbox é o assunto do próximo projeto
  da série, e merece ser sentido antes de ser resolvido.

- **A fronteira é verificada por teste, não pelo compilador.** Um import proibido compila;
  só a suíte reprova. Módulos Maven dariam erro de compilação, ao custo de três artefatos
  e da impossibilidade de expressar "pode ouvir, não pode mandar" sem quebrar pedidos em
  dois. Só é garantia real porque o teste roda no CI e bloqueia merge.

- **H2 e sem camada web.** Banco real com Testcontainers e API REST são assunto de outros
  projetos.

## Exercícios

1. **Quebre a fronteira de propósito** e rode `./mvnw test -Dtest=EstruturaDosModulosTest`.
   Leia a mensagem inteira. É o feedback que você quer que um colega receba às seis da
   tarde de uma sexta.
2. **Adicione um módulo `notificacoes`** que também escuta `PedidoConfirmado` e manda
   e-mail. Quantas linhas de `pedidos` você precisou tocar? A resposta deveria ser zero.
3. **Tente fazer faturamento chamar `Pedidos.consultar`** para buscar os itens da nota.
   O build reprova. Agora decida: o evento deveria carregar os itens, ou faturamento
   deveria manter a própria cópia? Escreva o ADR que supera o 0002 com sua escolha.
4. **Extraia `faturamento` para um serviço próprio.** Você vai descobrir que o modelo de
   dados já está pronto — o trabalho é trocar o listener local por um consumidor de
   mensageria. É esse o valor do desenho, e vale sentir na prática.

## Regras de trabalho neste repositório

- `EstruturaDosModulosTest` é o guardião. Nunca desligue para "resolver rápido".
- Dependência nova entre módulos entra editando o `package-info.java`, com o motivo no
  commit — e não descobrindo depois que ela apareceu.
- Nenhuma FK, nenhum join entre schemas de contextos diferentes.

## O que eu faria diferente

_A preencher depois de usar._
