-- Um schema por bounded context. Nenhuma FK atravessa a fronteira, e nenhuma consulta
-- faz join entre schemas - ver ADR 0003.

CREATE SCHEMA IF NOT EXISTS catalogo;
CREATE SCHEMA IF NOT EXISTS pedidos;
CREATE SCHEMA IF NOT EXISTS faturamento;

CREATE TABLE IF NOT EXISTS catalogo.produto (
    sku   VARCHAR(64)    NOT NULL PRIMARY KEY,
    nome  VARCHAR(200)   NOT NULL,
    preco DECIMAL(12, 2) NOT NULL,
    ativo BOOLEAN        NOT NULL
);

CREATE TABLE IF NOT EXISTS pedidos.pedido (
    id         VARCHAR(64) NOT NULL PRIMARY KEY,
    cliente_id VARCHAR(64) NOT NULL,
    status     VARCHAR(20) NOT NULL
);

-- sku sem FK para catalogo.produto: referencia por identidade, entre contextos.
CREATE TABLE IF NOT EXISTS pedidos.item_pedido (
    pedido_id       VARCHAR(64)    NOT NULL REFERENCES pedidos.pedido (id),
    sku             VARCHAR(64)    NOT NULL,
    nome            VARCHAR(200)   NOT NULL,
    preco_unitario  DECIMAL(12, 2) NOT NULL,
    quantidade      INT            NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_item_pedido ON pedidos.item_pedido (pedido_id);

-- pedido_id sem FK para pedidos.pedido, pelo mesmo motivo.
CREATE TABLE IF NOT EXISTS faturamento.fatura (
    numero     VARCHAR(32)    NOT NULL PRIMARY KEY,
    pedido_id  VARCHAR(64)    NOT NULL UNIQUE,
    cliente_id VARCHAR(64)    NOT NULL,
    valor      DECIMAL(12, 2) NOT NULL,
    emitida_em TIMESTAMP      NOT NULL
);
