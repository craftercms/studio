ALTER TABLE `remote_repository` DROP COLUMN `remote_branch` ;

UPDATE `audit` SET `source` = 'API' WHERE `source` = 'UI' ;

ALTER TABLE `publish_request` ADD COLUMN `package_id` VARCHAR(50) NULL ;

ALTER TABLE `remote_repository` ADD UNIQUE `uq_rr_site_remote_name` (`site_id`, `remote_name`) ;

UPDATE _meta SET version = '3.0.11.3' ;