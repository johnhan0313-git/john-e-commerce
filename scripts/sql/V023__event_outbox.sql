-- Transactional outbox (no MQ): durable events + consumer inbox dedup.

CREATE TABLE IF NOT EXISTS t_event_outbox (
    id                  BIGINT PRIMARY KEY,
    tenant_id           BIGINT NOT NULL,
    event_type          VARCHAR(64) NOT NULL,
    aggregate_type      VARCHAR(64) NOT NULL,
    aggregate_id        BIGINT NOT NULL,
    payload             JSONB NOT NULL DEFAULT '{}',
    status              SMALLINT NOT NULL DEFAULT 0,
    next_retry_at       BIGINT,
    attempt_count       INT NOT NULL DEFAULT 0,
    last_error          TEXT,
    delete_flag         SMALLINT NOT NULL DEFAULT 0,
    created_at          BIGINT NOT NULL,
    created_by          BIGINT,
    updated_at          BIGINT NOT NULL,
    updated_by          BIGINT,
    idempotent_key      VARCHAR(128)
);

-- status: 0=PENDING 1=PROCESSING 2=DONE 3=DEAD
CREATE UNIQUE INDEX IF NOT EXISTS uk_t_event_outbox_idempotent
    ON t_event_outbox (idempotent_key) WHERE idempotent_key IS NOT NULL AND delete_flag = 0;
CREATE INDEX IF NOT EXISTS idx_t_event_outbox_dispatch
    ON t_event_outbox (tenant_id, status, next_retry_at)
    WHERE delete_flag = 0 AND status IN (0, 1);
CREATE INDEX IF NOT EXISTS idx_t_event_outbox_aggregate
    ON t_event_outbox (tenant_id, aggregate_type, aggregate_id) WHERE delete_flag = 0;

COMMENT ON TABLE t_event_outbox IS 'Transactional outbox; dispatcher polls PENDING/retry-due rows';

CREATE TABLE IF NOT EXISTS t_event_inbox (
    id                  BIGINT PRIMARY KEY,
    tenant_id           BIGINT NOT NULL,
    event_id            BIGINT NOT NULL,
    processed_at        BIGINT NOT NULL,
    delete_flag         SMALLINT NOT NULL DEFAULT 0,
    created_at          BIGINT NOT NULL,
    created_by          BIGINT,
    updated_at          BIGINT NOT NULL,
    updated_by          BIGINT,
    idempotent_key      VARCHAR(64)
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_t_event_inbox_event
    ON t_event_inbox (event_id) WHERE delete_flag = 0;
CREATE INDEX IF NOT EXISTS idx_t_event_inbox_tenant
    ON t_event_inbox (tenant_id) WHERE delete_flag = 0;

COMMENT ON TABLE t_event_inbox IS 'Consumer-side dedup for outbox event handlers';
