USE kb_document;

ALTER TABLE kb_document
    ADD COLUMN content_length INT DEFAULT NULL COMMENT 'Document content length' AFTER content_id;
