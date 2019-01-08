ALTER TABLE `site` DROP INDEX `site_id_unique` ;

ALTER TABLE `site` DROP INDEX `site_id_idx` ;

ALTER TABLE `site`
ADD COLUMN IF NOT EXISTS `repo_folder_name` VARCHAR(300) NOT NULL DEFAULT '' ;

UPDATE `site` SET `repo_folder_name` = `name` ;


UPDATE _meta SET version = '3.1.0.11' ;