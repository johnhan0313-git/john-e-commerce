-- V009: content / storefront decoration

CREATE TABLE t_page_template (
    id              BIGINT PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    name            VARCHAR(100) NOT NULL,
    template_type   VARCHAR(30) NOT NULL,
    config          JSONB NOT NULL DEFAULT '{}',
    status          SMALLINT NOT NULL DEFAULT 1,
    delete_flag     SMALLINT NOT NULL DEFAULT 0,
    created_at      BIGINT NOT NULL,
    created_by      BIGINT,
    updated_at      BIGINT NOT NULL,
    updated_by      BIGINT,
    idempotent_key  VARCHAR(64)
);

CREATE INDEX idx_t_page_template_tenant ON t_page_template (tenant_id);
CREATE INDEX idx_t_page_template_type ON t_page_template (tenant_id, template_type);

COMMENT ON TABLE t_page_template IS 'Page layout template';

CREATE TABLE t_banner (
    id              BIGINT PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    title           VARCHAR(100),
    image_url       VARCHAR(500) NOT NULL,
    link_url        VARCHAR(500),
    position        VARCHAR(30) NOT NULL DEFAULT 'HOME',
    sort_order      INT NOT NULL DEFAULT 0,
    status          SMALLINT NOT NULL DEFAULT 1,
    start_time      BIGINT,
    end_time        BIGINT,
    delete_flag     SMALLINT NOT NULL DEFAULT 0,
    created_at      BIGINT NOT NULL,
    created_by      BIGINT,
    updated_at      BIGINT NOT NULL,
    updated_by      BIGINT,
    idempotent_key  VARCHAR(64)
);

CREATE INDEX idx_t_banner_tenant ON t_banner (tenant_id);
CREATE INDEX idx_t_banner_position ON t_banner (tenant_id, position, status);

COMMENT ON TABLE t_banner IS 'Marketing banner';

CREATE TABLE t_navigation (
    id              BIGINT PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    name            VARCHAR(50) NOT NULL,
    icon_url        VARCHAR(500),
    link_url        VARCHAR(500),
    sort_order      INT NOT NULL DEFAULT 0,
    parent_id       BIGINT NOT NULL DEFAULT 0,
    status          SMALLINT NOT NULL DEFAULT 1,
    delete_flag     SMALLINT NOT NULL DEFAULT 0,
    created_at      BIGINT NOT NULL,
    created_by      BIGINT,
    updated_at      BIGINT NOT NULL,
    updated_by      BIGINT,
    idempotent_key  VARCHAR(64)
);

CREATE INDEX idx_t_navigation_tenant ON t_navigation (tenant_id);
CREATE INDEX idx_t_navigation_parent ON t_navigation (tenant_id, parent_id);

COMMENT ON TABLE t_navigation IS 'App navigation menu item';
