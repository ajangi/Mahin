-- M12: AI assistant foundation (consent + metadata-only interaction log). No conversation bodies.

CREATE TABLE assistant_consent (
    user_id UUID PRIMARY KEY,
    share_cycle_summary BOOLEAN NOT NULL DEFAULT FALSE,
    share_symptom_tags BOOLEAN NOT NULL DEFAULT FALSE,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE assistant_interaction_log (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    prompt_template_id VARCHAR(128) NOT NULL,
    provider_id VARCHAR(64) NOT NULL,
    model_version VARCHAR(64) NOT NULL,
    outcome_class VARCHAR(64) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_assistant_interaction_log_user_created
    ON assistant_interaction_log (user_id, created_at DESC);
