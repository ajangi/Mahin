ALTER TABLE content_version ADD COLUMN search_index_text VARCHAR(2500);

UPDATE content_version
SET search_index_text = LOWER(title)
WHERE search_index_text IS NULL;
