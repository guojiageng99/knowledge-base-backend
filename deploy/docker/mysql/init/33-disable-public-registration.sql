USE kb_foundation;

UPDATE kb_system_config
SET config_value = 'false'
WHERE config_key = 'user.registration.enabled' AND deleted = 0;
