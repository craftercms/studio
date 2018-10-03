ALTER TABLE `gitlog` DROP COLUMN IF EXISTS `verified` ;

ALTER TABLE `gitlog` DROP COLUMN IF EXISTS `commit_date` ;

ALTER TABLE `site` ADD COLUMN IF NOT EXISTS `last_verified_gitlog_commit_id` VARCHAR(50) NULL ;

UPDATE _meta SET version = '3.0.2.1' ;