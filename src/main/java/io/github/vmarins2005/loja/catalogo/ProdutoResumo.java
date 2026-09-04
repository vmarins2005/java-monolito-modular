package io.github.vmarins2005.loja.catalogo;

import java.math.BigDecimal;

/**
 * O que o catalogo mostra para fora. A entidade de persistencia fica em
 * {@code catalogo.internal} e nenhum outro modulo consegue enxerga-la.
 */
public record ProdutoResumo(String sku, String nome, BigDecimal preco, boolean ativo) {
}
