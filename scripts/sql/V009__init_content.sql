CREATE TABLE t_page_template (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    name            VARCHAR(100) NOT NULL,
    template_type   VARCHAR(30) NOT NULL,
    config          JSONB NOT NULL DEFAULT '{}',
    status          SMALLINT NOT NULL DEFAULT 1,
    created_at      BIGINT NOT NULL,
    created_by      BIGINT,
    updated_at      BIGINT NOT NULL,
    updated_by      BIGINT,
    idempotent_key  VARCHAR(64)
);

CREATE TABLE t_banner (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    title           VARCHAR(100),
    image_url       VARCHAR(500) NOT NULL,
    link_url        VARCHAR(500),
    sort_order      INT DEFAULT 0,
    status          SMALLINT NOT NULL DEFAULT 1,
    created_at      BIGINT NOT NULL,
    created_by      BIGINT,
    updated_at      BIGINT NOT NULL,
    updated_by      BIGINT,
    idempotent_key  VARCHAR(64)
);

CREATE TABLE t_navigation (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    name            VARCHAR(50) NOT NULL,
    icon_url        VARCHAR(500),
    link_url        VARCHAR(500),
    sort_order      INT DEFAULT 0,
    parent_id       BIGINT DEFAULT 0,
    status          SMALLINT NOT NULL DEFAULT 1,
    created_at      BIGINT NOT NULL,
    created_by      BIGINT,
    updated_at      BIGINT NOT NULL,
    updated_by      BIGINT,
    idempotent_key  VARCHAR(64)
);
