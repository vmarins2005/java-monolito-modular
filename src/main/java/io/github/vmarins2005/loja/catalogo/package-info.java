/**
 * Catalogo de produtos.
 *
 * <p>Nao depende de nenhum outro modulo, e e proposital: catalogo e o contexto mais
 * estavel do sistema, e o que mais e consultado. Um modulo que nao depende de ninguem
 * pode ser extraido para servico proprio a qualquer momento, sem arrastar nada junto.
 */
@org.springframework.modulith.ApplicationModule(
        displayName = "Catalogo",
        allowedDependencies = {})
package io.github.vmarins2005.loja.catalogo;
