package io.github.vmarins2005.loja.pedidos;

public interface Pedidos {

    String abrir(String clienteId);

    void adicionarItem(String pedidoId, String sku, int quantidade);

    void confirmar(String pedidoId);

    PedidoResumo consultar(String pedidoId);
}
