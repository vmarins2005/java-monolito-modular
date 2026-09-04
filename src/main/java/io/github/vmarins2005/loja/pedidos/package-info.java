/**
 * Pedidos.
 *
 * <p>Depende de {@code catalogo} e de mais nada. A dependencia e sincrona e explicita:
 * ao adicionar um item, o pedido consulta o catalogo e <b>copia</b> nome e preco para
 * dentro de si. A copia e deliberada - ver ADR 0003.
 *
 * <p>Faturamento nao aparece aqui. Pedidos nao sabe que faturamento existe: publica um
 * evento e segue a vida. Quem quiser reagir se inscreve.
 */
@org.springframework.modulith.ApplicationModule(
        displayName = "Pedidos",
        allowedDependencies = "catalogo")
package io.github.vmarins2005.loja.pedidos;
