package io.github.vmarins2005.loja.catalogo;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * API publica do modulo de catalogo. Esta interface e os records deste pacote sao a
 * unica superficie que os outros modulos enxergam.
 */
public interface Catalogo {

    void cadastrar(String sku, String nome, BigDecimal preco);

    void desativar(String sku);

    Optional<ProdutoResumo> buscar(String sku);

    List<ProdutoResumo> ativos();
}
