-- V2: pgvector extension + stock metadata for semantic search

CREATE EXTENSION IF NOT EXISTS vector;

-- Stock metadata table for semantic search (embeddings stored here)
CREATE TABLE IF NOT EXISTS stock_metadata (
    id           BIGSERIAL PRIMARY KEY,
    symbol       VARCHAR(20)  UNIQUE NOT NULL,
    company_name VARCHAR(255) NOT NULL,
    sector       VARCHAR(100),
    industry     VARCHAR(100),
    market_cap   VARCHAR(50),
    description  TEXT,
    embedding    vector(1536),
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- HNSW index on stock embeddings for fast cosine similarity search
CREATE INDEX IF NOT EXISTS idx_stock_embedding ON stock_metadata
    USING hnsw (embedding vector_cosine_ops)
    WITH (m = 16, ef_construction = 64);

-- Spring AI vector_store table (auto-created by Spring AI, but defined here for clarity)
-- Spring AI will create this table itself if initialize-schema=true
-- This is just a reference comment.
