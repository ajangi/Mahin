CREATE TABLE play_subscription_record (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES user_account (id),
    product_id VARCHAR(128) NOT NULL,
    purchase_token_hash VARCHAR(64) NOT NULL,
    subscription_state VARCHAR(32) NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE,
    acknowledged_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_play_sub_user_token UNIQUE (user_id, purchase_token_hash)
);

CREATE INDEX idx_play_sub_user_updated ON play_subscription_record (user_id, updated_at DESC);

CREATE TABLE entitlement_grant (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES user_account (id),
    tier VARCHAR(32) NOT NULL,
    source VARCHAR(32) NOT NULL,
    starts_at TIMESTAMP WITH TIME ZONE NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_entitlement_grant_user_active ON entitlement_grant (user_id, revoked_at, expires_at);

UPDATE app_meta SET meta_value = 'm8' WHERE meta_key = 'schema_bootstrap';
