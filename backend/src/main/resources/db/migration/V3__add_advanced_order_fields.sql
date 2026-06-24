ALTER TABLE wallets
    ADD COLUMN reserved_balance NUMERIC(19, 2) NOT NULL DEFAULT 0;

ALTER TABLE holdings
    ADD COLUMN locked_quantity INTEGER NOT NULL DEFAULT 0;

ALTER TABLE orders
    ADD COLUMN limit_price NUMERIC(19, 4),
    ADD COLUMN stop_price NUMERIC(19, 4),
    ADD COLUMN status_reason VARCHAR(255),
    ADD COLUMN cancelled_at TIMESTAMPTZ,
    ADD COLUMN expires_at TIMESTAMPTZ,
    ALTER COLUMN executed_price DROP NOT NULL,
    ALTER COLUMN executed_at DROP NOT NULL;

ALTER TABLE orders DROP CONSTRAINT chk_orders_status;
ALTER TABLE orders
    ADD CONSTRAINT chk_orders_status
        CHECK (status IN ('PENDING', 'EXECUTED', 'REJECTED', 'CANCELLED', 'EXPIRED'));

ALTER TABLE wallets
    ADD CONSTRAINT chk_wallets_reserved_balance CHECK (reserved_balance >= 0);

ALTER TABLE holdings
    ADD CONSTRAINT chk_holdings_locked_quantity
        CHECK (locked_quantity >= 0 AND locked_quantity <= quantity);

CREATE INDEX idx_orders_pending_status ON orders (status) WHERE status = 'PENDING';
