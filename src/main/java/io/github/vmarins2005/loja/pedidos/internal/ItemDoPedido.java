package io.github.vmarins2005.loja.pedidos.internal;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;

/**
 * Nome e preco vem copiados do catalogo no momento da inclusao, e nao por join.
 *
 * <p>Nao e desnormalizacao por preguica: o preco do pedido e o preco praticado naquele
 * instante. Se a consulta fizesse join com a tabela de produto, mudar o preco no catalogo
 * mudaria retroativamente o valor de pedidos ja fechados - o que e um defeito, nao uma
 * otimizacao. Ver ADR 0003.
 */
@Embeddable
class ItemDoPedido {

    @Column(nullable = false)
    private String sku;

    @Column(nullable = false)
    private String nome;

    @Column(name = "preco_unitario", nullable = false, precision = 12, scale = 2)
    private BigDecimal precoUnitario;

    @Column(nullable = false)
    private int quantidade;

    protected ItemDoPedido() {
        // exigido pelo JPA
    }

    ItemDoPedido(String sku, String nome, BigDecimal precoUnitario, int quantidade) {
        this.sku = sku;
        this.nome = nome;
        this.precoUnitario = precoUnitario;
        this.quantidade = quantidade;
    }

    BigDecimal subtotal() {
        return precoUnitario.multiply(BigDecimal.valueOf(quantidade));
    }

    String sku() {
        return sku;
    }

    String nome() {
        return nome;
    }

    BigDecimal precoUnitario() {
        return precoUnitario;
    }

    int quantidade() {
        return quantidade;
    }
}
