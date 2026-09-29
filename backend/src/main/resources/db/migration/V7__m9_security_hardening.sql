ALTER TABLE deletion_request
    ADD COLUMN completed_at TIMESTAMP WITH TIME ZONE;

ALTER TABLE deletion_request
    ADD COLUMN failure_reason VARCHAR(256);

ALTER TABLE deletion_request
    ADD COLUMN attempt_count INTEGER NOT NULL DEFAULT 0;

ALTER TABLE deletion_request
    ADD COLUMN processing_started_at TIMESTAMP WITH TIME ZONE;

CREATE TABLE security_audit_event (
    id UUID PRIMARY KEY,
    actor_type VARCHAR(32) NOT NULL,
    actor_id VARCHAR(80) NOT NULL,
    action VARCHAR(64) NOT NULL,
    target_type VARCHAR(64),
    target_id VARCHAR(80),
    metadata_json TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_security_audit_created_at ON security_audit_event (created_at DESC);

UPDATE app_meta SET meta_value = 'm9' WHERE meta_key = 'schema_bootstrap';
