package io.github.vmarins2005.loja.pedidos.internal;

import io.github.vmarins2005.loja.catalogo.Catalogo;
import io.github.vmarins2005.loja.catalogo.ProdutoResumo;
import io.github.vmarins2005.loja.pedidos.PedidoResumo;
import io.github.vmarins2005.loja.pedidos.Pedidos;
import io.github.vmarins2005.loja.pedidos.ProdutoIndisponivel;
import io.github.vmarins2005.loja.pedidos.eventos.PedidoConfirmado;
import java.time.Clock;
import java.util.NoSuchElementException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class ServicoDePedidos implements Pedidos {

    private final RepositorioDePedidos repositorio;
    private final Catalogo catalogo;
    private final ApplicationEventPublisher eventos;
    private final Clock relogio;

    ServicoDePedidos(RepositorioDePedidos repositorio, Catalogo catalogo,
                     ApplicationEventPublisher eventos, Clock relogio) {
        this.repositorio = repositorio;
        this.catalogo = catalogo;
        this.eventos = eventos;
        this.relogio = relogio;
    }

    @Override
    @Transactional
    public String abrir(String clienteId) {
        return repositorio.save(new PedidoEntidade(clienteId)).id();
    }

    /**
     * Chamada sincrona ao catalogo, de proposito: o pedido precisa da resposta agora para
     * decidir se aceita o item. Evento aqui seria "adicione o item e depois eu te aviso
     * se o produto existia", que nao e um contrato utilizavel.
     */
    @Override
    @Transactional
    public void adicionarItem(String pedidoId, String sku, int quantidade) {
        ProdutoResumo produto = catalogo.buscar(sku)
                .filter(ProdutoResumo::ativo)
                .orElseThrow(() -> new ProdutoIndisponivel(sku));

        carregar(pedidoId).adicionar(
                new ItemDoPedido(produto.sku(), produto.nome(), produto.preco(), quantidade));
    }

    /**
     * Publica dentro da transacao. O assinante roda depois do commit - ver ADR 0002 e a
     * limitacao registrada la sobre o que acontece se o processo cair no meio.
     */
    @Override
    @Transactional
    public void confirmar(String pedidoId) {
        PedidoEntidade pedido = carregar(pedidoId);
        pedido.confirmar();
        eventos.publishEvent(new PedidoConfirmado(
                pedido.id(), pedido.clienteId(), pedido.total(), relogio.instant()));
    }

    @Override
    @Transactional(readOnly = true)
    public PedidoResumo consultar(String pedidoId) {
        return carregar(pedidoId).paraResumo();
    }

    private PedidoEntidade carregar(String pedidoId) {
        return repositorio.findById(pedidoId)
                .orElseThrow(() -> new NoSuchElementException("pedido nao encontrado: " + pedidoId));
    }
}
