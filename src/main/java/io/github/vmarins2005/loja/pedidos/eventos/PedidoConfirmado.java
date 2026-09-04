package io.github.vmarins2005.loja.pedidos.eventos;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Fato consumado, no participio. O evento carrega o que o assinante precisa para agir
 * sem voltar a perguntar - aqui, valor e cliente - e nao apenas o identificador.
 *
 * <p>Carregar so o id economiza payload e cobra uma consulta de volta ao modulo de
 * origem em todo assinante, o que recria o acoplamento que o evento deveria remover.
 * Carregar dados demais faz o contrato do evento virar o modelo inteiro. O meio-termo e
 * uma decisao, e esta registrada no ADR 0002.
 */
public record PedidoConfirmado(String pedidoId, String clienteId, BigDecimal total, Instant confirmadoEm) {
}
