DROP TABLE IF EXISTS notes;
DROP TABLE IF EXISTS refresh_tokens;
DROP TABLE IF EXISTS user_skills;
DROP TABLE IF EXISTS job_applications;
DROP TABLE IF EXISTS users;

CREATE TABLE users
(
    id            UUID         NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at    date,
    updated_at    date,
    email         VARCHAR(255) NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uc_users_email UNIQUE (email)
);

CREATE TABLE user_skills
(
    user_id UUID NOT NULL,
    name    VARCHAR(255),
    CONSTRAINT uc_a01583bb68b39765bda131e0b UNIQUE (user_id, name),
    CONSTRAINT fk_user_skills_on_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE job_applications
(
    id              UUID         NOT NULL,
    created_at      TIMESTAMP(6) WITHOUT TIME ZONE,
    updated_at      TIMESTAMP(6) WITHOUT TIME ZONE,
    owner_id        UUID         NOT NULL,
    company         VARCHAR(255) NOT NULL,
    position        VARCHAR(255) NOT NULL,
    url             VARCHAR(255),
    job_description TEXT,
    status          VARCHAR(255),
    applied_at      date,
    CONSTRAINT pk_job_applications PRIMARY KEY (id)
);

CREATE TABLE notes
(
    id                 UUID         NOT NULL,
    created_at         TIMESTAMP(6) WITHOUT TIME ZONE,
    updated_at         TIMESTAMP(6) WITHOUT TIME ZONE,
    value              TEXT         NOT NULL,
    job_application_id UUID         NOT NULL,
    CONSTRAINT pk_notes PRIMARY KEY (id),
    CONSTRAINT fk_notes_on_job_application FOREIGN KEY (job_application_id)
        REFERENCES job_applications (id) ON DELETE CASCADE
);

CREATE INDEX idx_notes_job_application_created
    ON notes (job_application_id, created_at);

CREATE TABLE refresh_tokens
(
    id         UUID                           NOT NULL,
    user_id    UUID                           NOT NULL,
    token_hash VARCHAR(64)                    NOT NULL,
    expires_at TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    revoked_at TIMESTAMP(6) WITHOUT TIME ZONE,
    created_at TIMESTAMP(6) WITHOUT TIME ZONE,
    updated_at TIMESTAMP(6) WITHOUT TIME ZONE,
    CONSTRAINT pk_refresh_tokens PRIMARY KEY (id),
    CONSTRAINT uc_refresh_tokens_token_hash UNIQUE (token_hash),
    CONSTRAINT fk_refresh_tokens_on_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_refresh_tokens_user ON refresh_tokens (user_id);
