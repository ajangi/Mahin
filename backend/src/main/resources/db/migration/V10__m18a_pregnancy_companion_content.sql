-- M18a: pregnancy companion CMS types (care_schedule, checklist_template) use existing type_payload_json.
-- Bump schema bootstrap marker for real-schema migration tests.

UPDATE app_meta SET meta_value = 'm18a' WHERE meta_key = 'schema_bootstrap';
