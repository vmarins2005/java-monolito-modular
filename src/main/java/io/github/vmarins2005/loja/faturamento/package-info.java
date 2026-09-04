/**
 * Faturamento.
 *
 * <p>Depende apenas da interface nomeada {@code pedidos :: eventos} - ou seja, pode
 * referenciar {@code PedidoConfirmado} e mais nada de pedidos. Chamar
 * {@code Pedidos.confirmar} daqui nao compila, e essa e a diferenca entre "pode ouvir" e
 * "pode mandar", declarada e verificada pelo build.
 *
 * <p>A direcao da dependencia importa: faturamento conhece pedidos, pedidos nao conhece
 * faturamento. Quem publica o evento nao sabe quem escuta.
 */
@org.springframework.modulith.ApplicationModule(
        displayName = "Faturamento",
        allowedDependencies = "pedidos :: eventos")
package io.github.vmarins2005.loja.faturamento;
