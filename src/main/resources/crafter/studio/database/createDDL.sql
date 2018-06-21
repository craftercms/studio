CREATE DATABASE IF NOT EXISTS crafter
  DEFAULT CHARACTER SET utf8 ;

FLUSH PRIVILEGES ;

CREATE USER IF NOT EXISTS 'crafter'@'localhost'
  IDENTIFIED BY 'crafter' ;

GRANT ALL PRIVILEGES ON crafter.* TO 'crafter'@'localhost'
WITH GRANT OPTION ;

CREATE USER IF NOT EXISTS 'crafter'@'%'
  IDENTIFIED BY 'crafter' ;

GRANT ALL PRIVILEGES ON crafter.* TO 'crafter'@'%'
WITH GRANT OPTION ;

USE crafter ;

CREATE TABLE _meta (`version` VARCHAR(10) NOT NULL , PRIMARY KEY (`version`)) ;

INSERT INTO _meta (version) VALUES ('3.0.15') ;

CREATE TABLE IF NOT EXISTS `audit` (
  `id`             BIGINT(20)   NOT NULL AUTO_INCREMENT,
  `modified_date`  DATETIME     NOT NULL,
  `creation_date`  DATETIME     NOT NULL,
  `summary`        TEXT         NOT NULL,
  `summary_format` VARCHAR(255) NOT NULL,
  `content_id`     TEXT         NOT NULL,
  `site_network`   VARCHAR(50)  NOT NULL,
  `activity_type`  VARCHAR(255) NOT NULL,
  `content_type`   VARCHAR(255) NOT NULL,
  `post_user_id`   VARCHAR(255) NOT NULL,
  `source`         VARCHAR(255) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `audit_user_idx` (`post_user_id`),
  KEY `audit_site_idx` (`site_network`),
  KEY `audit_content_idx` (`content_id`(1000))
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8
  ROW_FORMAT = DYNAMIC ;

CREATE TABLE IF NOT EXISTS `dependency` (
  `id`          BIGINT(20)  NOT NULL AUTO_INCREMENT,
  `site`        VARCHAR(50) NOT NULL,
  `source_path` TEXT        NOT NULL,
  `target_path` TEXT        NOT NULL,
  `type`        VARCHAR(50) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `dependency_site_idx` (`site`),
  KEY `dependency_sourcepath_idx` (`source_path`(1000))
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8
  ROW_FORMAT = DYNAMIC ;

CREATE TABLE IF NOT EXISTS `item_state` (
  `object_id`         VARCHAR(255)  NOT NULL,
  `site`              VARCHAR(50)   NOT NULL,
  `path`              VARCHAR(2000) NOT NULL,
  `state`             VARCHAR(255)  NOT NULL,
  `system_processing` BIT(1)        NOT NULL,
  PRIMARY KEY (`object_id`),
  KEY `item_state_object_idx` (`object_id`),
  UNIQUE `uq_is_site_path` (`site`, `path`(900))
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8
  ROW_FORMAT = DYNAMIC ;

CREATE TABLE IF NOT EXISTS `navigation_order_sequence` (
  `folder_id` VARCHAR(100) NOT NULL,
  `site`      VARCHAR(50)  NOT NULL,
  `path`      TEXT         NOT NULL,
  `max_count` FLOAT        NOT NULL,
  PRIMARY KEY (`folder_id`),
  KEY `navigationorder_folder_idx` (`folder_id`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8
  ROW_FORMAT = DYNAMIC ;

CREATE TABLE IF NOT EXISTS `publish_request` (
  `id`                BIGINT       NOT NULL AUTO_INCREMENT,
  `site`              VARCHAR(50)  NOT NULL,
  `environment`       VARCHAR(20)  NOT NULL,
  `path`              TEXT         NOT NULL,
  `oldpath`           TEXT         NULL,
  `username`          VARCHAR(255) NULL,
  `scheduleddate`     DATETIME     NOT NULL,
  `state`             VARCHAR(50)  NOT NULL,
  `action`            VARCHAR(20)  NOT NULL,
  `contenttypeclass`  VARCHAR(20)  NULL,
  `submissioncomment` TEXT         NULL,
  `commit_id`         VARCHAR(50)  NULL,
  `package_id`         VARCHAR(50)  NULL,
  PRIMARY KEY (`id`),
  INDEX `publish_request_site_idx` (`site` ASC),
  INDEX `publish_request_environment_idx` (`environment` ASC),
  INDEX `publish_request_path_idx` (`path`(1000) ASC),
  INDEX `publish_request_sitepath_idx` (`site` ASC, `path`(900) ASC),
  INDEX `publish_request_state_idx` (`state` ASC)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8
  ROW_FORMAT = DYNAMIC ;

CREATE TABLE IF NOT EXISTS `site` (
  `id`                              BIGINT(20)    NOT NULL AUTO_INCREMENT,
  `site_id`                         VARCHAR(50)   NOT NULL,
  `name`                            VARCHAR(255)  NOT NULL,
  `description`                     TEXT          NULL,
  `status`                          VARCHAR(255)  NULL,
  `last_commit_id`                  VARCHAR(50)   NULL,
  `system`                          INT           NOT NULL DEFAULT 0,
  `publishing_enabled`              INT           NOT NULL DEFAULT 1,
  `publishing_status_message`       VARCHAR(2000) NULL,
  `last_verified_gitlog_commit_id`  VARCHAR(50)   NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `id_unique` (`id` ASC),
  UNIQUE INDEX `site_id_unique` (`site_id` ASC),
  INDEX `site_id_idx` (`site_id` ASC)
)

  ENGINE = InnoDB
  DEFAULT CHARSET = utf8
  ROW_FORMAT = DYNAMIC ;

CREATE TABLE IF NOT EXISTS `item_metadata` (
  `id`                      INT           NOT NULL AUTO_INCREMENT,
  `site`                    VARCHAR(50)   NOT NULL,
  `path`                    VARCHAR(2000) NOT NULL,
  `name`                    VARCHAR(255)  NULL,
  `modified`                DATETIME      NULL,
  `modifier`                VARCHAR(255)  NULL,
  `owner`                   VARCHAR(255)  NULL,
  `creator`                 VARCHAR(255)  NULL,
  `firstname`               VARCHAR(255)  NULL,
  `lastname`                VARCHAR(255)  NULL,
  `lockowner`               VARCHAR(255)  NULL,
  `email`                   VARCHAR(255)  NULL,
  `renamed`                 INT           NULL,
  `oldurl`                  TEXT          NULL,
  `deleteurl`               TEXT          NULL,
  `imagewidth`              INT           NULL,
  `imageheight`             INT           NULL,
  `approvedby`              VARCHAR(255)  NULL,
  `submittedby`             VARCHAR(255)  NULL,
  `submittedfordeletion`    INT           NULL,
  `sendemail`               INT           NULL,
  `submissioncomment`       TEXT          NULL,
  `launchdate`              DATETIME      NULL,
  `commit_id`               VARCHAR(50)   NULL,
  `submittedtoenvironment`  VARCHAR(255)  NULL,
  PRIMARY KEY (`id`),
  UNIQUE `uq__im_site_path` (`site`, `path`(900))
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8
  ROW_FORMAT = DYNAMIC ;

CREATE TABLE IF NOT EXISTS `user`
(
  `username`           VARCHAR(255) NOT NULL,
  `password`           VARCHAR(255) NOT NULL,
  `firstname`          VARCHAR(255) NOT NULL,
  `lastname`           VARCHAR(255) NOT NULL,
  `email`              VARCHAR(255) NOT NULL,
  `enabled`            INT          NOT NULL,
  `externally_managed` INT          NOT NULL DEFAULT 0,
  PRIMARY KEY (`username`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8
  ROW_FORMAT = DYNAMIC ;

INSERT IGNORE INTO `user` (username, password, firstname, lastname, email, enabled, externally_managed)
VALUES ('admin', 'vTwNOJ8GJdyrP7rrvQnpwsd2hCV1xRrJdTX2sb51i+w=|R68ms0Od3AngQMdEeKY6lA==', 'admin', 'admin',
        'evaladmin@example.com', 1, 0) ;

CREATE TABLE IF NOT EXISTS `group`
(
  `id`                 BIGINT(20)   NOT NULL AUTO_INCREMENT,
  `name`               VARCHAR(255) NOT NULL,
  `description`        VARCHAR(3000),
  `site_id`            BIGINT(20),
  `externally_managed` INT          NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  FOREIGN KEY group_site_fk(site_id) REFERENCES site (id)
    ON DELETE CASCADE,
  UNIQUE `uq_group_name_siteid` (`name`, `site_id`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8
  ROW_FORMAT = DYNAMIC ;

CREATE TABLE IF NOT EXISTS group_user
(
  `id`       BIGINT(20)   NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(255) NOT NULL,
  `groupid`  BIGINT       NOT NULL,
  PRIMARY KEY (`id`),
  FOREIGN KEY user_ug_foreign_key(username) REFERENCES `user` (username)
    ON DELETE CASCADE,
  FOREIGN KEY group_ug_foreign_key(groupid) REFERENCES `group` (id)
    ON DELETE CASCADE
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8
  ROW_FORMAT = DYNAMIC ;

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


INSERT IGNORE INTO site (site_id, name, description, system)
VALUES ('studio_root', 'Studio Root', 'Studio Root for global permissions', 1) ;

INSERT IGNORE INTO `group` (name, description, site_id) VALUES ('crafter-admin', 'crafter admin', 1) ;
INSERT IGNORE INTO `group` (name, description, site_id)
VALUES ('crafter-create-sites', 'crafter-create-sites', 1) ;

INSERT IGNORE INTO group_user (username, groupid) VALUES ('admin', 1) ;
INSERT IGNORE INTO group_user (username, groupid) VALUES ('admin', 2) ;

