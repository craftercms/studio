use crafter ;
-- Rename tables script
ALTER TABLE `cstudio_activity` RENAME `audit` ;
ALTER TABLE `cstudio_dependency` RENAME `dependency` ;
ALTER TABLE `cstudio_objectstate` RENAME `item_state` ;
ALTER TABLE `cstudio_pagenavigationordersequence` RENAME `navigation_order_sequence` ;
ALTER TABLE `cstudio_copytoenvironment` RENAME `publish_request` ;
ALTER TABLE `cstudio_site` RENAME `site` ;
ALTER TABLE `cstudio_objectmetadata` RENAME `item_metadata` ;
ALTER TABLE `cstudio_user` RENAME `user` ;
ALTER TABLE `cstudio_group` RENAME `group` ;
ALTER TABLE `cstudio_usergroup` RENAME `group_user` ;

ALTER TABLE `dependency` MODIFY COLUMN `type` VARCHAR(50) NOT NULL ;

CREATE TABLE _meta (`version` VARCHAR(10) NOT NULL , PRIMARY KEY (`version`)) ;

INSERT INTO _meta (version) VALUES ('3.0.1') ;
