package io.github.vmarins2005.loja.faturamento.internal;

import io.github.vmarins2005.loja.faturamento.FaturaResumo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Tabela no schema {@code faturamento}.
 *
 * <p>{@code pedido_id} e uma referencia por identidade, e nao uma chave estrangeira para
 * {@code pedidos.pedido}. Nao ha FK entre schemas de contextos diferentes, de proposito:
 * a FK amarraria os dois modulos no nivel do banco, e extrair faturamento para servico
 * proprio passaria a exigir migracao de dados em vez de mudanca de deploy. Ver ADR 0003.
 */
@Entity
@Table(schema = "faturamento", name = "fatura")
class FaturaEntidade {

    @Id
    private String numero;

    @Column(name = "pedido_id", nullable = false, unique = true)
    private String pedidoId;

    @Column(name = "cliente_id", nullable = false)
    private String clienteId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    @Column(name = "emitida_em", nullable = false)
    private Instant emitidaEm;

    protected FaturaEntidade() {
        // exigido pelo JPA
    }

    FaturaEntidade(String pedidoId, String clienteId, BigDecimal valor, Instant emitidaEm) {
        this.numero = "NF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.pedidoId = pedidoId;
        this.clienteId = clienteId;
        this.valor = valor;
        this.emitidaEm = emitidaEm;
    }

    FaturaResumo paraResumo() {
        return new FaturaResumo(numero, pedidoId, clienteId, valor, emitidaEm);
    }
}
