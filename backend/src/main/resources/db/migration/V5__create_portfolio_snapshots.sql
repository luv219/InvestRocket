CREATE TABLE portfolio_snapshots (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    cash_balance NUMERIC(19, 2) NOT NULL,
    reserved_cash NUMERIC(19, 2) NOT NULL,
    holdings_value NUMERIC(19, 2) NOT NULL,
    total_portfolio_value NUMERIC(19, 2) NOT NULL,
    total_invested NUMERIC(19, 2) NOT NULL,
    unrealized_profit_loss NUMERIC(19, 2) NOT NULL,
    realized_profit_loss NUMERIC(19, 2) NOT NULL,
    daily_profit_loss NUMERIC(19, 2) NOT NULL,
    daily_profit_loss_percent NUMERIC(19, 4) NOT NULL,
    overall_return_percent NUMERIC(19, 4) NOT NULL,
    snapshot_date DATE NOT NULL,
    snapshot_time TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_portfolio_snapshots_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_portfolio_snapshots_user_time
    ON portfolio_snapshots (user_id, snapshot_time);

CREATE INDEX idx_portfolio_snapshots_user_date
    ON portfolio_snapshots (user_id, snapshot_date);
