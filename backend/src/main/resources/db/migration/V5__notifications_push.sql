ALTER TABLE device_installation ADD COLUMN push_provider VARCHAR(32);
ALTER TABLE device_installation ADD COLUMN push_token_hash VARCHAR(64);
ALTER TABLE device_installation ADD COLUMN push_token_updated_at TIMESTAMP WITH TIME ZONE;

UPDATE app_meta SET meta_value = 'm7' WHERE meta_key = 'schema_bootstrap';
