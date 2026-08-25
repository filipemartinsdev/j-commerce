CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE product_embedding (
    id          UUID PRIMARY KEY,
    product_id         VARCHAR(255) NOT NULL,
    category_id BIGINT NOT NULL,
    embedding   VECTOR(1536) NOT NULL
);

CREATE INDEX product_category_id_idx
    ON product_embedding(category_id);

CREATE INDEX product_embedding_idx_hnsw
    ON product_embedding
    USING hnsw (embedding vector_cosine_ops);
