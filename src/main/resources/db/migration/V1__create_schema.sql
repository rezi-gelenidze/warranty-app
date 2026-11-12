CREATE TABLE store (
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE tag (
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE purchase (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(120)   NOT NULL,
    price           NUMERIC(12, 2) NOT NULL CHECK (price >= 0),
    purchase_date   DATE           NOT NULL,
    warranty_months INTEGER        NOT NULL CHECK (warranty_months BETWEEN 0 AND 120),
    warranty_until  DATE           NOT NULL,
    notes           VARCHAR(1000),
    store_id        BIGINT         NOT NULL REFERENCES store (id),
    created_at      TIMESTAMPTZ    NOT NULL DEFAULT now()
);

CREATE INDEX idx_purchase_store ON purchase (store_id);
CREATE INDEX idx_purchase_warranty_until ON purchase (warranty_until);

CREATE TABLE purchase_tag (
    purchase_id BIGINT NOT NULL REFERENCES purchase (id) ON DELETE CASCADE,
    tag_id      BIGINT NOT NULL REFERENCES tag (id),
    PRIMARY KEY (purchase_id, tag_id)
);

CREATE TABLE document (
    id            BIGSERIAL PRIMARY KEY,
    purchase_id   BIGINT       NOT NULL REFERENCES purchase (id),
    type          VARCHAR(20)  NOT NULL,
    original_name VARCHAR(255) NOT NULL,
    object_key    VARCHAR(255) NOT NULL UNIQUE,
    content_type  VARCHAR(100) NOT NULL,
    size_bytes    BIGINT       NOT NULL,
    uploaded_at   TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_document_purchase ON document (purchase_id);
