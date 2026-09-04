package io.github.vmarins2005.loja;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

import io.github.vmarins2005.loja.catalogo.Catalogo;
import io.github.vmarins2005.loja.faturamento.FaturaResumo;
import io.github.vmarins2005.loja.faturamento.Faturamento;
import io.github.vmarins2005.loja.pedidos.PedidoResumo;
import io.github.vmarins2005.loja.pedidos.Pedidos;
import io.github.vmarins2005.loja.pedidos.ProdutoIndisponivel;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Fluxo atravessando os tres modulos.
 *
 * <p>Os testes nao sao {@code @Transactional} de proposito: o assinante do evento roda
 * <b>depois do commit</b>, e um teste que faz rollback no fim nunca veria a fatura ser
 * emitida. E o tipo de detalhe que faz alguem passar uma tarde achando que o listener
 * esta quebrado.
 *
 * <p>A espera e por condicao ({@code await}) e nao por tempo ({@code sleep}): sleep em
 * teste assincrono e a receita mais comum de suite instavel - passa na maquina rapida,
 * falha no CI carregado.
 */
@SpringBootTest
class FluxoDeCompraTest {

    @Autowired
    private Catalogo catalogo;

    @Autowired
    private Pedidos pedidos;

    @Autowired
    private Faturamento faturamento;

    @Test
    @DisplayName("pedido confirmado gera fatura, sem que pedidos saiba que faturamento existe")
    void pedidoConfirmadoGeraFatura() {
        String sku = cadastrarProduto("450.00");
        String pedidoId = pedidos.abrir("CLI-1");
        pedidos.adicionarItem(pedidoId, sku, 2);

        pedidos.confirmar(pedidoId);

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() ->
                assertThat(faturamento.porPedido(pedidoId)).isPresent());

        FaturaResumo fatura = faturamento.porPedido(pedidoId).orElseThrow();
        assertThat(fatura.valor()).isEqualByComparingTo("900.00");
        assertThat(fatura.clienteId()).isEqualTo("CLI-1");
        assertThat(fatura.numero()).startsWith("NF-");
    }

    @Test
    @DisplayName("o preco vai copiado para o pedido: mudar o catalogo nao altera pedido antigo")
    void precoECopiadoNaInclusao() {
        String sku = cadastrarProduto("100.00");
        String pedidoId = pedidos.abrir("CLI-2");
        pedidos.adicionarItem(pedidoId, sku, 1);

        // Reajuste de preco no catalogo, depois do item ja estar no pedido.
        catalogo.cadastrar(sku, "Produto reajustado", new BigDecimal("300.00"));

        PedidoResumo pedido = pedidos.consultar(pedidoId);
        assertThat(pedido.total()).isEqualByComparingTo("100.00");
        assertThat(pedido.itens()).singleElement()
                .satisfies(item -> assertThat(item.precoUnitario()).isEqualByComparingTo("100.00"));
        assertThat(catalogo.buscar(sku).orElseThrow().preco()).isEqualByComparingTo("300.00");
    }

    @Test
    @DisplayName("produto inexistente e produto desativado sao recusados na inclusao")
    void produtoIndisponivel() {
        String pedidoId = pedidos.abrir("CLI-3");

        assertThatThrownBy(() -> pedidos.adicionarItem(pedidoId, "NAO-EXISTE", 1))
                .isInstanceOf(ProdutoIndisponivel.class);

        String sku = cadastrarProduto("50.00");
        catalogo.desativar(sku);

        assertThatThrownBy(() -> pedidos.adicionarItem(pedidoId, sku, 1))
                .isInstanceOf(ProdutoIndisponivel.class);
    }

    @Test
    @DisplayName("pedido sem item nao confirma, e nao gera fatura")
    void pedidoVazioNaoConfirma() {
        String pedidoId = pedidos.abrir("CLI-4");

        assertThatThrownBy(() -> pedidos.confirmar(pedidoId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sem itens");

        assertThat(faturamento.porPedido(pedidoId)).isEmpty();
    }

    @Test
    @DisplayName("pedido confirmado nao aceita item novo nem segunda confirmacao")
    void confirmadoEFinal() {
        String sku = cadastrarProduto("20.00");
        String pedidoId = pedidos.abrir("CLI-5");
        pedidos.adicionarItem(pedidoId, sku, 1);
        pedidos.confirmar(pedidoId);

        assertThatThrownBy(() -> pedidos.adicionarItem(pedidoId, sku, 1))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> pedidos.confirmar(pedidoId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("uma fatura por pedido: o mesmo pedido nao e faturado duas vezes")
    void umaFaturaPorPedido() {
        String sku = cadastrarProduto("10.00");
        String pedidoId = pedidos.abrir("CLI-6");
        pedidos.adicionarItem(pedidoId, sku, 3);
        pedidos.confirmar(pedidoId);

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() ->
                assertThat(faturamento.porPedido(pedidoId)).isPresent());

        Optional<FaturaResumo> fatura = faturamento.porPedido(pedidoId);
        assertThat(faturamento.todas())
                .filteredOn(nota -> nota.pedidoId().equals(pedidoId))
                .hasSize(1);
        assertThat(fatura.orElseThrow().valor()).isEqualByComparingTo("30.00");
    }

    private String cadastrarProduto(String preco) {
        String sku = "SKU-" + UUID.randomUUID().toString().substring(0, 8);
        catalogo.cadastrar(sku, "Produto de teste", new BigDecimal(preco));
        return sku;
    }
}
