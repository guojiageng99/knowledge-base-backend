USE kb_document;

ALTER TABLE kb_document
    ADD COLUMN IF NOT EXISTS content_id VARCHAR(64) DEFAULT NULL COMMENT 'MongoDB content ID' AFTER content;
