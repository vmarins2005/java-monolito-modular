package io.github.vmarins2005.loja.catalogo.internal;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

interface RepositorioDeProdutos extends JpaRepository<ProdutoEntidade, String> {

    List<ProdutoEntidade> findByAtivoTrue();
}
