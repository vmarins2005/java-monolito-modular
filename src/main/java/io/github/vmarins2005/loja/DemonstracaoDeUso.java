package io.github.vmarins2005.loja;

import io.github.vmarins2005.loja.catalogo.Catalogo;
import io.github.vmarins2005.loja.faturamento.Faturamento;
import io.github.vmarins2005.loja.pedidos.PedidoResumo;
import io.github.vmarins2005.loja.pedidos.Pedidos;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Roda o fluxo completo entre os tres modulos e imprime o resultado.
 *
 * <pre>
 *   ./mvnw spring-boot:run -Dspring-boot.run.profiles=demo
 * </pre>
 */
@Component
@Profile("demo")
class DemonstracaoDeUso implements CommandLineRunner {

    private final Catalogo catalogo;
    private final Pedidos pedidos;
    private final Faturamento faturamento;

    DemonstracaoDeUso(Catalogo catalogo, Pedidos pedidos, Faturamento faturamento) {
        this.catalogo = catalogo;
        this.pedidos = pedidos;
        this.faturamento = faturamento;
    }

    @Override
    public void run(String... argumentos) throws InterruptedException {
        catalogo.cadastrar("TEC-01", "Teclado mecanico", new BigDecimal("450.00"));
        catalogo.cadastrar("MOU-01", "Mouse sem fio", new BigDecimal("180.00"));

        String pedidoId = pedidos.abrir("CLI-1");
        pedidos.adicionarItem(pedidoId, "TEC-01", 1);
        pedidos.adicionarItem(pedidoId, "MOU-01", 2);
        pedidos.confirmar(pedidoId);

        PedidoResumo pedido = pedidos.consultar(pedidoId);
        System.out.printf("%npedido %s | %s | total R$ %s%n", pedido.id(), pedido.status(), pedido.total());
        pedido.itens().forEach(item -> System.out.printf(
                "  %-8s %-20s %2dx R$ %s%n", item.sku(), item.nome(), item.quantidade(), item.precoUnitario()));

        // O faturamento reage ao evento em outra thread, depois do commit. Numa
        // demonstracao de console isso e uma espera; em teste, seria await com condicao -
        // sleep em teste e a receita mais comum de suite instavel.
        TimeUnit.MILLISECONDS.sleep(500);

        faturamento.porPedido(pedidoId).ifPresentOrElse(
                fatura -> System.out.printf("%nfatura %s emitida | R$ %s | %s%n",
                        fatura.numero(), fatura.valor(), fatura.emitidaEm()),
                () -> System.out.println("\nfatura ainda nao emitida"));

        System.out.println("""

                Repare no que acabou de acontecer:
                  - pedidos consultou o catalogo e COPIOU nome e preco para dentro de si;
                  - pedidos publicou um evento sem saber que faturamento existe;
                  - faturamento reagiu, em outra transacao, depois do commit.
                Nenhum join entre schemas, nenhuma FK atravessando contexto.
                """);
    }
}
