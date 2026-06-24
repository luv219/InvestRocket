ALTER TABLE users
    ADD COLUMN phone_number VARCHAR(40),
    ADD COLUMN country VARCHAR(100),
    ADD COLUMN preferred_currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    ADD COLUMN profile_image_url VARCHAR(500),
    ADD COLUMN last_login_at TIMESTAMPTZ;

CREATE TABLE user_risk_settings (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE,
    max_order_value NUMERIC(19, 2) NOT NULL DEFAULT 25000.00,
    max_daily_trades INTEGER NOT NULL DEFAULT 50,
    allow_stop_loss_orders BOOLEAN NOT NULL DEFAULT TRUE,
    allow_limit_orders BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_user_risk_settings_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT chk_risk_max_order_value
        CHECK (max_order_value > 0 AND max_order_value <= 100000.00),
    CONSTRAINT chk_risk_max_daily_trades
        CHECK (max_daily_trades > 0 AND max_daily_trades <= 200)
);

CREATE TABLE audit_logs (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    action VARCHAR(30) NOT NULL,
    category VARCHAR(30) NOT NULL,
    description VARCHAR(500) NOT NULL,
    ip_address VARCHAR(64),
    user_agent VARCHAR(500),
    metadata TEXT,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_audit_logs_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_audit_logs_user_created_at
    ON audit_logs (user_id, created_at DESC);

CREATE INDEX idx_audit_logs_user_category
    ON audit_logs (user_id, category, created_at DESC);
