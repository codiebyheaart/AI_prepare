-- V1: Core application tables

CREATE TABLE IF NOT EXISTS users (
    id         BIGSERIAL PRIMARY KEY,
    email      VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name  VARCHAR(255) NOT NULL,
    role       VARCHAR(50)  NOT NULL DEFAULT 'TRADER',
    created_at TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS portfolios (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name       VARCHAR(255) NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS holdings (
    id            BIGSERIAL PRIMARY KEY,
    portfolio_id  BIGINT         NOT NULL REFERENCES portfolios(id) ON DELETE CASCADE,
    symbol        VARCHAR(20)    NOT NULL,
    company_name  VARCHAR(255),
    quantity      DECIMAL(12, 4) NOT NULL,
    avg_buy_price DECIMAL(14, 2) NOT NULL,
    added_at      TIMESTAMP      NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS transactions (
    id           BIGSERIAL PRIMARY KEY,
    portfolio_id BIGINT         NOT NULL REFERENCES portfolios(id) ON DELETE CASCADE,
    symbol       VARCHAR(20)    NOT NULL,
    company_name VARCHAR(255),
    type         VARCHAR(10)    NOT NULL CHECK (type IN ('BUY', 'SELL')),
    quantity     DECIMAL(12, 4) NOT NULL,
    price        DECIMAL(14, 2) NOT NULL,
    total_amount DECIMAL(16, 2) NOT NULL,
    timestamp    TIMESTAMP      NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS price_alerts (
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    symbol       VARCHAR(20)    NOT NULL,
    target_price DECIMAL(14, 2) NOT NULL,
    condition    VARCHAR(10)    NOT NULL CHECK (condition IN ('ABOVE', 'BELOW')),
    active       BOOLEAN        NOT NULL DEFAULT TRUE,
    triggered_at TIMESTAMP,
    created_at   TIMESTAMP      NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS uploaded_documents (
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    file_name    VARCHAR(500) NOT NULL,
    doc_type     VARCHAR(100),
    file_size    BIGINT,
    chunk_count  INT          DEFAULT 0,
    status       VARCHAR(50)  NOT NULL DEFAULT 'PROCESSING',
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS ai_interactions (
    id               BIGSERIAL PRIMARY KEY,
    user_id          BIGINT       REFERENCES users(id) ON DELETE SET NULL,
    session_id       VARCHAR(100),
    query            TEXT,
    response         TEXT,
    tokens_used      INT,
    model            VARCHAR(100),
    interaction_type VARCHAR(50),
    created_at       TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS research_reports (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT       REFERENCES users(id) ON DELETE SET NULL,
    symbol     VARCHAR(20)  NOT NULL,
    report     TEXT,
    created_at TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_holdings_portfolio ON holdings(portfolio_id);
CREATE INDEX IF NOT EXISTS idx_holdings_symbol    ON holdings(symbol);
CREATE INDEX IF NOT EXISTS idx_transactions_portfolio ON transactions(portfolio_id);
CREATE INDEX IF NOT EXISTS idx_ai_session ON ai_interactions(session_id);
CREATE INDEX IF NOT EXISTS idx_research_user_symbol ON research_reports(user_id, symbol);
