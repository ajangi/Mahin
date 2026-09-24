CREATE TABLE app_meta (
    meta_key VARCHAR(64) PRIMARY KEY,
    meta_value VARCHAR(512) NOT NULL
);

INSERT INTO app_meta (meta_key, meta_value) VALUES ('schema_bootstrap', 'm0');
