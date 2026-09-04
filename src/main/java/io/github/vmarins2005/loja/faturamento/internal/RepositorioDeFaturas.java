package io.github.vmarins2005.loja.faturamento.internal;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

interface RepositorioDeFaturas extends JpaRepository<FaturaEntidade, String> {

    Optional<FaturaEntidade> findByPedidoId(String pedidoId);

    boolean existsByPedidoId(String pedidoId);
}
