-- Chapter 39: idempotent automatic-save metadata migration for kb_document.
DELIMITER $$
DROP PROCEDURE IF EXISTS upgrade_kb_document_autosave$$
CREATE PROCEDURE upgrade_kb_document_autosave()
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE() AND table_name = 'kb_document' AND column_name = 'team_id'
    ) THEN
        ALTER TABLE kb_document ADD COLUMN team_id BIGINT DEFAULT NULL AFTER category_id;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE() AND table_name = 'kb_document' AND column_name = 'auto_save_dismissed'
    ) THEN
        ALTER TABLE kb_document ADD COLUMN auto_save_dismissed TINYINT NOT NULL DEFAULT 0
            COMMENT 'whether automatic-save recovery was dismissed' AFTER remark;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.statistics
        WHERE table_schema = DATABASE() AND table_name = 'kb_document' AND index_name = 'idx_team_id'
    ) THEN
        ALTER TABLE kb_document ADD KEY idx_team_id (team_id);
    END IF;
END$$
CALL upgrade_kb_document_autosave()$$
DROP PROCEDURE IF EXISTS upgrade_kb_document_autosave$$
DELIMITER ;
