CREATE TABLE holdings (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    symbol VARCHAR(15) NOT NULL,
    company_name VARCHAR(255) NOT NULL,
    quantity INTEGER NOT NULL,
    average_buy_price NUMERIC(19, 4) NOT NULL,
    total_invested NUMERIC(19, 2) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_holdings_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT uk_holdings_user_symbol UNIQUE (user_id, symbol),
    CONSTRAINT chk_holdings_quantity CHECK (quantity > 0),
    CONSTRAINT chk_holdings_average_price CHECK (average_buy_price >= 0),
    CONSTRAINT chk_holdings_total_invested CHECK (total_invested >= 0)
);

CREATE TABLE orders (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    symbol VARCHAR(15) NOT NULL,
    side VARCHAR(10) NOT NULL,
    order_type VARCHAR(20) NOT NULL,
    quantity INTEGER NOT NULL,
    requested_price NUMERIC(19, 4) NOT NULL,
    executed_price NUMERIC(19, 4) NOT NULL,
    status VARCHAR(20) NOT NULL,
    total_amount NUMERIC(19, 2) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    executed_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_orders_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT chk_orders_side CHECK (side IN ('BUY', 'SELL')),
    CONSTRAINT chk_orders_type CHECK (order_type IN ('MARKET', 'LIMIT', 'STOP_LOSS')),
    CONSTRAINT chk_orders_status CHECK (status IN ('PENDING', 'EXECUTED', 'REJECTED', 'CANCELLED')),
    CONSTRAINT chk_orders_quantity CHECK (quantity > 0),
    CONSTRAINT chk_orders_total_amount CHECK (total_amount >= 0)
);

CREATE TABLE trades (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL UNIQUE,
    user_id UUID NOT NULL,
    symbol VARCHAR(15) NOT NULL,
    side VARCHAR(10) NOT NULL,
    quantity INTEGER NOT NULL,
    price NUMERIC(19, 4) NOT NULL,
    trade_value NUMERIC(19, 2) NOT NULL,
    realized_profit_loss NUMERIC(19, 2) NOT NULL DEFAULT 0,
    executed_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_trades_order
        FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE,
    CONSTRAINT fk_trades_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT chk_trades_side CHECK (side IN ('BUY', 'SELL')),
    CONSTRAINT chk_trades_quantity CHECK (quantity > 0)
);

CREATE INDEX idx_orders_user_created_at ON orders (user_id, created_at DESC);
CREATE INDEX idx_trades_user_executed_at ON trades (user_id, executed_at DESC);
CREATE INDEX idx_holdings_user ON holdings (user_id);
