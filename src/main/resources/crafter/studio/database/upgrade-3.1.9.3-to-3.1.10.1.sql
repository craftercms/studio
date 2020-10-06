ALTER TABLE `site` ADD COLUMN `publishing_lock_owner` VARCHAR(255) NULL ;

ALTER TABLE `site` ADD COLUMN `publishing_lock_heartbeat` DATETIME NULL ;

UPDATE _meta SET version = '3.1.10.1' ;