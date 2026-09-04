package io.github.vmarins2005.loja.faturamento;

import java.math.BigDecimal;
import java.time.Instant;

public record FaturaResumo(String numero, String pedidoId, String clienteId, BigDecimal valor, Instant emitidaEm) {
}
