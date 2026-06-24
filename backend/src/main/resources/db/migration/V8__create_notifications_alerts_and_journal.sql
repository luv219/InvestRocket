CREATE TABLE notifications (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(150) NOT NULL,
    message VARCHAR(1000) NOT NULL,
    type VARCHAR(20) NOT NULL,
    category VARCHAR(20) NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    related_entity_type VARCHAR(50),
    related_entity_id UUID,
    created_at TIMESTAMPTZ NOT NULL,
    read_at TIMESTAMPTZ
);

CREATE TABLE price_alerts (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    symbol VARCHAR(15) NOT NULL,
    company_name VARCHAR(150) NOT NULL,
    target_price NUMERIC(19, 4) NOT NULL CHECK (target_price > 0),
    condition VARCHAR(10) NOT NULL,
    status VARCHAR(20) NOT NULL,
    triggered_price NUMERIC(19, 4),
    triggered_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE trading_journal_entries (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(150) NOT NULL,
    content VARCHAR(5000) NOT NULL,
    mood VARCHAR(20),
    strategy VARCHAR(150),
    symbol VARCHAR(15),
    order_id UUID REFERENCES orders(id) ON DELETE SET NULL,
    trade_id UUID REFERENCES trades(id) ON DELETE SET NULL,
    tags VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_is_read ON notifications(is_read);
CREATE INDEX idx_notifications_created_at ON notifications(created_at DESC);
CREATE INDEX idx_price_alerts_user_id ON price_alerts(user_id);
CREATE INDEX idx_price_alerts_symbol ON price_alerts(symbol);
CREATE INDEX idx_price_alerts_status ON price_alerts(status);
CREATE INDEX idx_journal_user_id ON trading_journal_entries(user_id);
CREATE INDEX idx_journal_symbol ON trading_journal_entries(symbol);
CREATE INDEX idx_journal_created_at ON trading_journal_entries(created_at DESC);
