ALTER TABLE `cstudio_objectmetadata` ADD COLUMN `submissioncomment` TEXT NULL;

ALTER TABLE `cstudio_objectmetadata` ADD COLUMN `launchdate` DATETIME NULL;

ALTER TABLE `cstudio_objectmetadata` MODIFY COLUMN `path` VARCHAR(2000) NULL;

ALTER TABLE `cstudio_objectmetadata` ADD CONSTRAINT `uq_om_site_path` UNIQUE (`site`, `path`(255));

ALTER TABLE `cstudio_objectstate` ADD CONSTRAINT `uq_os_site_path` UNIQUE (`site`, `path`(255));