CREATE TABLE user_account (
    id UUID PRIMARY KEY,
    email VARCHAR(320) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT user_account_email_unique UNIQUE (email)
);

CREATE TABLE guest_installation (
    id UUID PRIMARY KEY,
    local_user_id UUID NOT NULL,
    linked_user_id UUID REFERENCES user_account (id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    converted_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT guest_installation_local_user_unique UNIQUE (local_user_id)
);

CREATE TABLE device_installation (
    id UUID PRIMARY KEY,
    guest_installation_id UUID REFERENCES guest_installation (id),
    owner_user_id UUID REFERENCES user_account (id),
    platform VARCHAR(32) NOT NULL,
    app_version VARCHAR(32),
    registered_at TIMESTAMP WITH TIME ZONE NOT NULL,
    last_seen_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT device_installation_owner_chk CHECK (
        (guest_installation_id IS NOT NULL AND owner_user_id IS NULL)
        OR (guest_installation_id IS NULL AND owner_user_id IS NOT NULL)
    )
);

CREATE TABLE auth_refresh_token (
    id UUID PRIMARY KEY,
    token_hash VARCHAR(64) NOT NULL,
    user_id UUID REFERENCES user_account (id),
    guest_installation_id UUID REFERENCES guest_installation (id),
    device_id UUID NOT NULL REFERENCES device_installation (id),
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT auth_refresh_token_hash_unique UNIQUE (token_hash),
    CONSTRAINT auth_refresh_token_subject_chk CHECK (
        (user_id IS NOT NULL AND guest_installation_id IS NULL)
        OR (user_id IS NULL AND guest_installation_id IS NOT NULL)
    )
);

CREATE TABLE sync_owner_state (
    owner_key VARCHAR(80) PRIMARY KEY,
    next_server_revision BIGINT NOT NULL DEFAULT 1
);

CREATE TABLE sync_entity_record (
    id UUID PRIMARY KEY,
    owner_scope_key VARCHAR(80) NOT NULL,
    guest_installation_id UUID REFERENCES guest_installation (id),
    owner_user_id UUID REFERENCES user_account (id),
    entity_type VARCHAR(64) NOT NULL,
    entity_id UUID NOT NULL,
    server_revision BIGINT NOT NULL,
    client_revision BIGINT,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    deleted_at TIMESTAMP WITH TIME ZONE,
    payload_json TEXT NOT NULL,
    CONSTRAINT sync_entity_record_owner_chk CHECK (
        (guest_installation_id IS NOT NULL AND owner_user_id IS NULL)
        OR (guest_installation_id IS NULL AND owner_user_id IS NOT NULL)
    )
);

CREATE UNIQUE INDEX sync_entity_record_natural_key ON sync_entity_record (
    owner_scope_key,
    entity_type,
    entity_id
);

CREATE INDEX sync_entity_record_user_revision_idx ON sync_entity_record (owner_user_id, server_revision);
CREATE INDEX sync_entity_record_guest_revision_idx ON sync_entity_record (guest_installation_id, server_revision);

CREATE TABLE sync_idempotency (
    owner_key VARCHAR(80) NOT NULL,
    idempotency_key VARCHAR(128) NOT NULL,
    mutation_fingerprint VARCHAR(64) NOT NULL,
    response_json TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    PRIMARY KEY (owner_key, idempotency_key)
);

CREATE TABLE deletion_request (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES user_account (id),
    status VARCHAR(32) NOT NULL,
    requested_at TIMESTAMP WITH TIME ZONE NOT NULL,
    scheduled_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE export_job (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES user_account (id),
    status VARCHAR(32) NOT NULL,
    requested_at TIMESTAMP WITH TIME ZONE NOT NULL,
    completed_at TIMESTAMP WITH TIME ZONE
);

UPDATE app_meta SET meta_value = 'm5' WHERE meta_key = 'schema_bootstrap';
