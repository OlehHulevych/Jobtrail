CREATE TABLE cv_match_analyses
(
    id             UUID                           NOT NULL,
    created_at     TIMESTAMP(6) WITHOUT TIME ZONE,
    updated_at     TIMESTAMP(6) WITHOUT TIME ZONE,
    application_id UUID                           NOT NULL,
    cv_id          UUID                           NOT NULL,
    user_id        UUID                           NOT NULL,
    match_score    INTEGER,
    matched_skills JSONB,
    missing_skills JSONB,
    suggestions    TEXT,
    status         VARCHAR(255)                   NOT NULL,
    CONSTRAINT pk_cv_match_analyses PRIMARY KEY (id),
    CONSTRAINT fk_cv_match_analyses_on_application FOREIGN KEY (application_id)
        REFERENCES job_applications (id) ON DELETE CASCADE,
    CONSTRAINT fk_cv_match_analyses_on_cv FOREIGN KEY (cv_id)
        REFERENCES cv_documents (id) ON DELETE CASCADE,
    CONSTRAINT fk_cv_match_analyses_on_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_cv_match_analyses_application ON cv_match_analyses (application_id);
CREATE INDEX idx_cv_match_analyses_cv ON cv_match_analyses (cv_id);
CREATE INDEX idx_cv_match_analyses_user ON cv_match_analyses (user_id);
