USE kb_file;

ALTER TABLE tb_file
    ADD COLUMN duration INT NULL COMMENT 'Media duration in seconds',
    ADD COLUMN resolution VARCHAR(20) NULL COMMENT 'Media resolution',
    ADD COLUMN bitrate INT NULL COMMENT 'Media bitrate in kbps',
    ADD COLUMN transcode_status VARCHAR(20) NULL COMMENT 'PENDING/PROCESSING/DONE/FAILED',
    ADD COLUMN hls_path VARCHAR(500) NULL COMMENT 'HLS directory path',
    ADD COLUMN thumbnail_path VARCHAR(500) NULL COMMENT 'Thumbnail object path';

CREATE INDEX idx_transcode_status ON tb_file(transcode_status);
