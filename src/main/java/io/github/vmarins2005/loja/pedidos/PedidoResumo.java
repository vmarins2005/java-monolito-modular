package io.github.vmarins2005.loja.pedidos;

import java.math.BigDecimal;
import java.util.List;

public record PedidoResumo(String id, String clienteId, String status, BigDecimal total, List<Item> itens) {

    public record Item(String sku, String nome, BigDecimal precoUnitario, int quantidade) {
    }
}
