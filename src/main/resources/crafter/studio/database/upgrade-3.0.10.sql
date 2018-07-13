UPDATE `audit` SET `source` = 'API' WHERE `source` = 'UI' ;

ALTER TABLE `publish_request` ADD COLUMN `package_id` VARCHAR(50) NULL ;

ALTER TABLE `item_metadata` ADD COLUMN `submittedtoenvironment` VARCHAR(255) NULL ;

ALTER TABLE `site` ADD COLUMN `sandbox_branch` VARCHAR(255) NOT NULL DEFAULT 'master' ;

UPDATE _meta SET version = '3.0.15.1' ;