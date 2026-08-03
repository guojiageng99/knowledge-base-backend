USE kb_document;

ALTER TABLE kb_document
    ADD COLUMN IF NOT EXISTS content_length INT DEFAULT NULL COMMENT 'Document content length' AFTER content_id;

ALTER TABLE kb_document
    ADD COLUMN IF NOT EXISTS is_public TINYINT NOT NULL DEFAULT 1 COMMENT '0 private, 1 team, 2 public' AFTER status;
