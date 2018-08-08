ALTER TABLE `remote_repository` ADD UNIQUE `uq_rr_site_remote_name` (`site_id`, `remote_name`) ;

ALTER TABLE `item_metadata` ADD COLUMN `submittedtoenvironment` VARCHAR(255) NULL ;

ALTER TABLE `site` ADD COLUMN `sandbox_branch` VARCHAR(255) NOT NULL DEFAULT 'master' ;

ALTER TABLE `_meta` ADD COLUMN `integrity` BIGINT(10) NULL ;

UPDATE _meta SET version = '3.0.17' ;
