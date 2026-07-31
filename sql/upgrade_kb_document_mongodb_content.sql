USE kb_document;

ALTER TABLE kb_document
    ADD COLUMN content_id VARCHAR(64) DEFAULT NULL COMMENT 'MongoDB content ID' AFTER content;
