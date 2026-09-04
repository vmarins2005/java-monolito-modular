package io.github.vmarins2005.loja.catalogo.internal;

import io.github.vmarins2005.loja.catalogo.Catalogo;
import io.github.vmarins2005.loja.catalogo.ProdutoResumo;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class CatalogoEmJpa implements Catalogo {

    private final RepositorioDeProdutos repositorio;

    CatalogoEmJpa(RepositorioDeProdutos repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    @Transactional
    public void cadastrar(String sku, String nome, BigDecimal preco) {
        repositorio.save(new ProdutoEntidade(sku, nome, preco));
    }

    @Override
    @Transactional
    public void desativar(String sku) {
        repositorio.findById(sku).ifPresent(ProdutoEntidade::desativar);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProdutoResumo> buscar(String sku) {
        return repositorio.findById(sku).map(ProdutoEntidade::paraResumo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProdutoResumo> ativos() {
        return repositorio.findByAtivoTrue().stream().map(ProdutoEntidade::paraResumo).toList();
    }
}
