package io.github.vmarins2005.loja.pedidos;

public class ProdutoIndisponivel extends RuntimeException {

    public ProdutoIndisponivel(String sku) {
        super("produto indisponivel no catalogo: " + sku);
    }
}
