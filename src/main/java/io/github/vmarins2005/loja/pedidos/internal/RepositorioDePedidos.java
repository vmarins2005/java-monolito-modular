package io.github.vmarins2005.loja.pedidos.internal;

import org.springframework.data.jpa.repository.JpaRepository;

interface RepositorioDePedidos extends JpaRepository<PedidoEntidade, String> {
}
