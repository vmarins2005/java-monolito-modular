/**
 * Interface nomeada: a parte da API de pedidos que outros modulos podem depender sem
 * depender do modulo inteiro.
 *
 * <p>Faturamento declara {@code allowedDependencies = "pedidos :: eventos"}. Isso
 * significa que ele pode referenciar {@code PedidoConfirmado} e mais nada de pedidos -
 * chamar {@code Pedidos.confirmar} de dentro de faturamento nao compila.
 *
 * <p>E a diferenca entre "pode ouvir" e "pode mandar", declarada e verificada.
 */
@org.springframework.modulith.NamedInterface("eventos")
package io.github.vmarins2005.loja.pedidos.eventos;
