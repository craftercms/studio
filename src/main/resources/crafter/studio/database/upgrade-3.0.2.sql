ALTER TABLE `gitlog` DROP COLUMN `verified` ;

ALTER TABLE `gitlog` DROP COLUMN `commit_date` ;

ALTER TABLE `site` ADD COLUMN `last_verified_gitlog_commit_id` VARCHAR(50) NULL ;

UPDATE _meta SET version = '3.0.2.1' ;