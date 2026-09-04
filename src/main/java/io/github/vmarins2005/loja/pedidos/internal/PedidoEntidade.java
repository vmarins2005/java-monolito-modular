package io.github.vmarins2005.loja.pedidos.internal;

import io.github.vmarins2005.loja.pedidos.PedidoResumo;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(schema = "pedidos", name = "pedido")
class PedidoEntidade {

    @Id
    private String id;

    @Column(name = "cliente_id", nullable = false)
    private String clienteId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusPedido status;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(schema = "pedidos", name = "item_pedido",
            joinColumns = @JoinColumn(name = "pedido_id"))
    private List<ItemDoPedido> itens = new ArrayList<>();

    protected PedidoEntidade() {
        // exigido pelo JPA
    }

    PedidoEntidade(String clienteId) {
        this.id = UUID.randomUUID().toString();
        this.clienteId = clienteId;
        this.status = StatusPedido.RASCUNHO;
    }

    void adicionar(ItemDoPedido item) {
        exigirRascunho("adicionar item");
        itens.add(item);
    }

    void confirmar() {
        exigirRascunho("confirmar");
        if (itens.isEmpty()) {
            throw new IllegalStateException("pedido sem itens nao pode ser confirmado");
        }
        status = StatusPedido.CONFIRMADO;
    }

    BigDecimal total() {
        return itens.stream().map(ItemDoPedido::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    PedidoResumo paraResumo() {
        List<PedidoResumo.Item> resumoDosItens = itens.stream()
                .map(item -> new PedidoResumo.Item(
                        item.sku(), item.nome(), item.precoUnitario(), item.quantidade()))
                .toList();
        return new PedidoResumo(id, clienteId, status.name(), total(), resumoDosItens);
    }

    String id() {
        return id;
    }

    String clienteId() {
        return clienteId;
    }

    private void exigirRascunho(String acao) {
        if (status != StatusPedido.RASCUNHO) {
            throw new IllegalStateException("nao e possivel %s um pedido em %s".formatted(acao, status));
        }
    }
}
