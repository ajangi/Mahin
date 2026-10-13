-- M17a: structured payloads for daily_tip, program, and collection content types.

ALTER TABLE content_version
    ADD COLUMN type_payload_json TEXT;

CREATE INDEX idx_content_version_content_type ON content_version (content_type);

UPDATE app_meta SET meta_value = 'm17a' WHERE meta_key = 'schema_bootstrap';
