ALTER TABLE `site` DROP INDEX `site_id_unique` ;

ALTER TABLE `site` DROP INDEX `site_id_idx` ;

ALTER TABLE `site`
ADD COLUMN IF NOT EXISTS `repo_folder_name` VARCHAR(300) NOT NULL DEFAULT ''
ADD COLUMN IF NOT EXISTS `deleted` INT NOT NULL DEFAULT 0 ;

ALTER TABLE `site` ADD UNIQUE IF NOT EXISTS `site_id_name_unique` (`name` ASC, `site_id` ASC) ;

ALTER TABLE `site` ADD INDEX IF NOT EXISTS `site_name_idx` (`name` ASC) ;

UPDATE `site` SET `repo_folder_name` = `name` ;

UPDATE _meta SET version = '3.1.0.12' ;