CREATE TABLE job_analyses
(
    id                  UUID                           NOT NULL,
    created_at          TIMESTAMP(6) WITHOUT TIME ZONE,
    updated_at          TIMESTAMP(6) WITHOUT TIME ZONE,
    application_id      UUID                           NOT NULL,
    summary             TEXT,
    seniority           VARCHAR(255),
    required_skills     JSONB,
    nice_to_have_skills JSONB,
    responsibilities    JSONB,
    red_flags           JSONB,
    CONSTRAINT pk_job_analyses PRIMARY KEY (id),
    CONSTRAINT uc_job_analyses_application UNIQUE (application_id),
    CONSTRAINT fk_job_analyses_on_job_application FOREIGN KEY (application_id)
        REFERENCES job_applications (id) ON DELETE CASCADE
);
