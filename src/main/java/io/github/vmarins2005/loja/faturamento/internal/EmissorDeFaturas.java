package io.github.vmarins2005.loja.faturamento.internal;

import io.github.vmarins2005.loja.faturamento.FaturaResumo;
import io.github.vmarins2005.loja.faturamento.Faturamento;
import io.github.vmarins2005.loja.pedidos.eventos.PedidoConfirmado;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.modulith.ApplicationModuleListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class EmissorDeFaturas implements Faturamento {

    private static final Logger log = LoggerFactory.getLogger(EmissorDeFaturas.class);

    private final RepositorioDeFaturas repositorio;

    EmissorDeFaturas(RepositorioDeFaturas repositorio) {
        this.repositorio = repositorio;
    }

    /**
     * {@code @ApplicationModuleListener} e assincrono, roda depois do commit de quem
     * publicou e abre transacao propria. As tres coisas juntas sao o ponto:
     *
     * <ul>
     *   <li><b>Depois do commit</b>: nao existe fatura para pedido que deu rollback.</li>
     *   <li><b>Transacao propria</b>: falha aqui nao desfaz a confirmacao do pedido. O
     *       cliente comprou; a nota sai depois.</li>
     *   <li><b>Assincrono</b>: o tempo de emitir a nota nao entra no tempo de resposta de
     *       quem confirmou o pedido.</li>
     * </ul>
     *
     * <p>O preco esta no ADR 0002: se o processo cair entre o commit e a execucao daqui, o
     * evento se perde e o pedido fica sem fatura. E exatamente o problema que o padrao
     * outbox resolve, e o assunto do projeto de outbox da serie.
     */
    @ApplicationModuleListener
    void aoConfirmarPedido(PedidoConfirmado evento) {
        if (repositorio.existsByPedidoId(evento.pedidoId())) {
            // Reentrega do mesmo evento nao pode gerar duas notas fiscais.
            log.info("pedido {} ja faturado, nada a fazer", evento.pedidoId());
            return;
        }
        FaturaEntidade fatura = repositorio.save(new FaturaEntidade(
                evento.pedidoId(), evento.clienteId(), evento.total(), evento.confirmadoEm()));
        log.info("fatura {} emitida para o pedido {}", fatura.paraResumo().numero(), evento.pedidoId());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<FaturaResumo> porPedido(String pedidoId) {
        return repositorio.findByPedidoId(pedidoId).map(FaturaEntidade::paraResumo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FaturaResumo> todas() {
        return repositorio.findAll().stream().map(FaturaEntidade::paraResumo).toList();
    }
}
