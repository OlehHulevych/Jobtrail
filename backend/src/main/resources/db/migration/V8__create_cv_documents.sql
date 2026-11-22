CREATE TABLE cv_documents
(
    id                UUID                           NOT NULL,
    created_at        TIMESTAMP(6) WITHOUT TIME ZONE,
    updated_at        TIMESTAMP(6) WITHOUT TIME ZONE,
    user_id           UUID                           NOT NULL,
    object_key        VARCHAR(255)                   NOT NULL,
    original_name     VARCHAR(255)                   NOT NULL,
    content_type      VARCHAR(255)                   NOT NULL,
    extracted_text    TEXT,
    extraction_status VARCHAR(255)                   NOT NULL,
    CONSTRAINT pk_cv_documents PRIMARY KEY (id),
    CONSTRAINT fk_cv_documents_on_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_cv_documents_user ON cv_documents (user_id);
