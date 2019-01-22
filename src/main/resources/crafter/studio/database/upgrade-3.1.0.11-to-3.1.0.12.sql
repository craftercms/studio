ALTER TABLE `site` DROP INDEX IF EXISTS `site_id_unique` ;

ALTER TABLE `site` DROP INDEX IF EXISTS `site_id_idx` ;

ALTER TABLE `site` ADD COLUMN IF NOT EXISTS `site_uuid` VARCHAR(50) NOT NULL ;

ALTER TABLE `site` ADD COLUMN IF NOT EXISTS `deleted` INT NOT NULL DEFAULT 0 ;

ALTER TABLE `site` ADD UNIQUE IF NOT EXISTS `site_id_site_uuid_unique` (`site_id` ASC, `site_uuid` ASC) ;

ALTER TABLE `site` ADD INDEX IF NOT EXISTS `site_id_idx` (`site_id` ASC) ;

UPDATE `site` SET site_uuid = UUID() WHERE site_uuid IS NULL OR site_uuid = '' ;

UPDATE _meta SET version = '3.1.0.12' ;