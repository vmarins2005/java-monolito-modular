package io.github.vmarins2005.loja.faturamento;

import java.util.List;
import java.util.Optional;

public interface Faturamento {

    Optional<FaturaResumo> porPedido(String pedidoId);

    List<FaturaResumo> todas();
}
