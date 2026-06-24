CREATE TABLE watchlist_items (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    symbol VARCHAR(15) NOT NULL,
    company_name VARCHAR(200) NOT NULL,
    exchange VARCHAR(80),
    currency VARCHAR(3),
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_watchlist_items_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT uk_watchlist_items_user_symbol UNIQUE (user_id, symbol),
    CONSTRAINT chk_watchlist_items_symbol_uppercase CHECK (symbol = UPPER(symbol))
);

CREATE INDEX idx_watchlist_items_user_created_at
    ON watchlist_items (user_id, created_at DESC);
