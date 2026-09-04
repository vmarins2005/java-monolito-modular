package io.github.vmarins2005.loja.catalogo.internal;

import io.github.vmarins2005.loja.catalogo.ProdutoResumo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/**
 * Tabela no schema {@code catalogo}. Nenhum outro modulo consulta esta tabela, e nenhuma
 * consulta de outro modulo faz join com ela - ver ADR 0003.
 */
@Entity
@Table(schema = "catalogo", name = "produto")
class ProdutoEntidade {

    @Id
    private String sku;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal preco;

    @Column(nullable = false)
    private boolean ativo;

    protected ProdutoEntidade() {
        // exigido pelo JPA
    }

    ProdutoEntidade(String sku, String nome, BigDecimal preco) {
        this.sku = sku;
        this.nome = nome;
        this.preco = preco;
        this.ativo = true;
    }

    void desativar() {
        this.ativo = false;
    }

    ProdutoResumo paraResumo() {
        return new ProdutoResumo(sku, nome, preco, ativo);
    }

    boolean estaAtivo() {
        return ativo;
    }
}
