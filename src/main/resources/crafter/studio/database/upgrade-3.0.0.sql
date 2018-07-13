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

CREATE TABLE IF NOT EXISTS gitlog
(
  `id`          BIGINT(20)    NOT NULL AUTO_INCREMENT,
  `site_id`     VARCHAR(50)   NOT NULL,
  `commit_id`   VARCHAR(50)   NOT NULL,
  `processed`   INT           NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE `uq_siteid_commitid` (`site_id`, `commit_id`),
  INDEX `gitlog_site_idx` (`site_id` ASC)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8
  ROW_FORMAT = DYNAMIC ;

ALTER TABLE `site` ADD COLUMN `last_verified_gitlog_commit_id` VARCHAR(50) NULL ;

CREATE TABLE IF NOT EXISTS remote_repository
(
  `id`                    BIGINT(20)    NOT NULL AUTO_INCREMENT,
  `site_id`               VARCHAR(50)   NOT NULL,
  `remote_name`           VARCHAR(50)   NOT NULL,
  `remote_url`            VARCHAR(2000)   NOT NULL,
  `authentication_type`   VARCHAR(16)   NOT NULL,
  `remote_username`       VARCHAR(255)   NULL,
  `remote_password`       VARCHAR(255)   NULL,
  `remote_token`          VARCHAR(255)   NULL,
  `remote_private_key`    TEXT           NULL,
  PRIMARY KEY (`id`),
  UNIQUE `uq_rr_site_remote_name` (`site_id`, `remote_name`),
  INDEX `remoterepository_site_idx` (`site_id` ASC)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8
  ROW_FORMAT = DYNAMIC ;

UPDATE `audit` SET `source` = 'API' WHERE `source` = 'UI' ;

ALTER TABLE `publish_request` ADD COLUMN `package_id` VARCHAR(50) NULL ;

ALTER TABLE `item_metadata` ADD COLUMN `submittedtoenvironment` VARCHAR(255) NULL ;

ALTER TABLE `site` ADD COLUMN `sandbox_branch` VARCHAR(255) NOT NULL DEFAULT 'master' ;

INSERT INTO _meta (version) VALUES ('3.0.15') ;
