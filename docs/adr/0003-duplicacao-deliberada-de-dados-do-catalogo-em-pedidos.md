# ADR 0003 — Duplicação deliberada de dados do catálogo em pedidos

Status: aceito · 2026-09-04 · supera: —

## Contexto

Quando um item entra no pedido, `sku`, `nome` e `precoUnitario` são **copiados** do
catálogo para dentro da tabela `pedidos.item_pedido`. Não há chave estrangeira para
`catalogo.produto`, e nenhuma consulta de pedidos faz join com o schema do catálogo.

Para quem vem de modelagem relacional, isso parece erro de normalização — e é a primeira
coisa que um revisor bem-intencionado tenta "consertar".

## Por que a cópia está certa

**O preço do pedido é o preço praticado naquele instante.** Se a consulta fizesse join com
`catalogo.produto`, um reajuste no catálogo mudaria retroativamente o valor de pedidos já
fechados — inclusive de pedidos já faturados. Isso não é otimização: é defeito, e dos
caros, porque só aparece na conferência contábil.

O teste `precoECopiadoNaInclusao` fixa esse comportamento: adiciona item a R$ 100,00,
reajusta o catálogo para R$ 300,00 e verifica que o pedido continua valendo R$ 100,00.

O mesmo vale para `nome`: a nota fiscal precisa dizer o que foi vendido, com a descrição
vigente na venda. Produto renomeado no catálogo não reescreve o histórico.

Ou seja: **não é o mesmo dado.** `catalogo.produto.preco` é "quanto custa hoje";
`pedidos.item_pedido.preco_unitario` é "quanto custou naquela compra". Dois conceitos com
o mesmo nome, que é o disfarce mais comum de fronteira de contexto.

## Por que não há FK entre schemas

`pedidos.item_pedido.sku` e `faturamento.fatura.pedido_id` são referências por
identidade, sem constraint.

Uma FK entre contextos amarra os dois módulos no nível do banco: extrair faturamento para
serviço próprio passaria a exigir migração de dados, e não apenas mudança de deploy. Além
disso, a FK impediria apagar um produto do catálogo enquanto existisse qualquer pedido
histórico com ele — regra que ninguém decidiu e que o banco passaria a impor.

Dentro de um mesmo contexto, FK continua: `pedidos.item_pedido` referencia
`pedidos.pedido`, porque ali os dois vivem e morrem juntos.

## Consequências

- \+ Histórico de preço preservado sem tabela de versionamento.
- \+ Consulta de pedido não depende do catálogo — nem em código, nem em SQL.
- \+ Extrair um módulo para serviço próprio é mudança de deploy, não migração de dados.
- − Produto renomeado no catálogo não se reflete em pedidos antigos. É o comportamento
  desejado, mas precisa estar dito, senão vira chamado de suporte.
- − Não existe integridade referencial entre contextos. Um `sku` pode sumir do catálogo e
  continuar em pedidos antigos — o que é correto para o histórico e exige que a tela de
  pedido não tente buscar o produto para exibir.
- − A duplicação precisa estar documentada em lugar visível, senão a próxima pessoa
  "normaliza" isso e quebra o histórico de preços sem perceber. Está aqui, no README, e
  num comentário em `ItemDoPedido`.
