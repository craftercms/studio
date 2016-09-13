CREATE TABLE IF NOT EXISTS `cstudio_activity` (
  `id`             BIGINT(20)   NOT NULL AUTO_INCREMENT,
  `modified_date`  DATETIME     NOT NULL,
  `creation_date`  DATETIME     NOT NULL,
  `summary`        TEXT         NOT NULL,
  `summary_format` VARCHAR(255) NOT NULL,
  `content_id`     TEXT         NOT NULL,
  `site_network`   VARCHAR(255) NOT NULL,
  `activity_type`  VARCHAR(255) NOT NULL,
  `content_type`   VARCHAR(255) NOT NULL,
  `post_user_id`   VARCHAR(255) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `cstudio_activity_user_idx` (`post_user_id`),
  KEY `cstudio_activity_site_idx` (`site_network`),
  KEY `cstudio_activity_content_idx` (`content_id`(1000))
)
  ENGINE =InnoDB
  DEFAULT CHARSET =utf8
  ROW_FORMAT=DYNAMIC ;

CREATE TABLE IF NOT EXISTS `cstudio_DEPENDENCY` (
  `id`          BIGINT(20)  NOT NULL AUTO_INCREMENT,
  `site`        VARCHAR(35) NOT NULL,
  `source_path` TEXT        NOT NULL,
  `target_path` TEXT        NOT NULL,
  `type`        VARCHAR(15) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `cstudio_dependency_site_idx` (`site`),
  KEY `cstudio_dependency_sourcepath_idx` (`source_path`(1000))
)
  ENGINE =InnoDB
  DEFAULT CHARSET =utf8
  ROW_FORMAT=DYNAMIC ;

CREATE TABLE IF NOT EXISTS `cstudio_objectstate` (
  `object_id`         VARCHAR(255)  NOT NULL,
  `site`              VARCHAR(50)   NOT NULL,
  `path`              VARCHAR(2000) NOT NULL,
  `state`             VARCHAR(255)  NOT NULL,
  `system_processing` BIT(1)        NOT NULL,
  PRIMARY KEY (`object_id`),
  KEY `cstudio_objectstate_object_idx` (`object_id`),
  UNIQUE `uq_os_site_path` (`site`, `path`(900))
)
  ENGINE =InnoDB
  DEFAULT CHARSET =utf8
  ROW_FORMAT=DYNAMIC ;

CREATE TABLE IF NOT EXISTS `cstudio_pagenavigationordersequence` (
  `folder_id` VARCHAR(100) NOT NULL,
  `site`      VARCHAR(50)  NOT NULL,
  `path`      TEXT NOT NULL,
  `max_count` FLOAT        NOT NULL,
  PRIMARY KEY (`folder_id`),
  KEY `cstudio_pagenavigationorder_folder_idx` (`folder_id`)
)
  ENGINE =InnoDB
  DEFAULT CHARSET =utf8
  ROW_FORMAT=DYNAMIC ;

CREATE TABLE IF NOT EXISTS `cstudio_copytoenvironment` (
  `id`               BIGINT       NOT NULL AUTO_INCREMENT,
  `site`             VARCHAR(50)  NOT NULL,
  `environment`      VARCHAR(20)  NOT NULL,
  `path`             TEXT         NOT NULL,
  `oldpath`          TEXT         NULL,
  `username`         VARCHAR(255) NULL,
  `scheduleddate`    DATETIME     NOT NULL,
  `state`            VARCHAR(50)  NOT NULL,
  `action`           VARCHAR(20)  NOT NULL,
  `contenttypeclass` VARCHAR(20)  NULL,
  `submissioncomment` TEXT        NULL,
  PRIMARY KEY (`id`),
  INDEX `cstudio_cte_site_idx` (`site` ASC),
  INDEX `cstudio_cte_environment_idx` (`environment` ASC),
  INDEX `cstudio_cte_path_idx` (`path`(1000) ASC),
  INDEX `cstudio_cte_sitepath_idx` (`site` ASC, `path`(900) ASC),
  INDEX `cstudio_cte_state_idx` (`state` ASC)
)
  ENGINE =InnoDB
  DEFAULT CHARSET =utf8
  ROW_FORMAT=DYNAMIC ;

CREATE TABLE IF NOT EXISTS `cstudio_publishtotarget` (
  `id`               BIGINT       NOT NULL AUTO_INCREMENT,
  `site`             VARCHAR(50)  NOT NULL,
  `environment`      VARCHAR(20)  NOT NULL,
  `path`             TEXT         NOT NULL,
  `oldpath`          TEXT         NULL,
  `username`         VARCHAR(255) NOT NULL,
  `version`          BIGINT       NOT NULL,
  `action`           VARCHAR(20)  NOT NULL,
  `contenttypeclass` VARCHAR(20)  NULL,
  PRIMARY KEY (`id`),
  INDEX `cstudio_ptt_site_idx` (`site` ASC),
  INDEX `cstudio_ptt_environment_idx` (`environment` ASC),
  INDEX `cstudio_ptt_path` (`path`(1000) ASC),
  INDEX `cstudio_ptt_sitepath_idx` (`site` ASC, `path`(900) ASC)
)
  ENGINE =InnoDB
  DEFAULT CHARSET =utf8
  ROW_FORMAT=DYNAMIC ;

CREATE TABLE IF NOT EXISTS `cstudio_deploymentsynchistory` (
  `id`               BIGINT       NOT NULL AUTO_INCREMENT,
  `syncdate`         DATETIME     NOT NULL,
  `site`             VARCHAR(50)  NOT NULL,
  `environment`      VARCHAR(20)  NOT NULL,
  `path`             TEXT         NOT NULL,
  `target`           VARCHAR(50)  NOT NULL,
  `username`         VARCHAR(255) NOT NULL,
  `contenttypeclass` VARCHAR(25)  NULL,
  PRIMARY KEY (`id`),
  INDEX `cs_depsynchist_site_idx` (`site` ASC),
  INDEX `cs_depsynchist_env_idx` (`environment` ASC),
  INDEX `cs_depsynchist_path_idx` (`path`(1000) ASC),
  INDEX `cs_depsynchist_sitepath_idx` (`site` ASC, `path`(900) ASC),
  INDEX `cs_depsynchist_target_idx` (`target` ASC),
  INDEX `cs_depsynchist_user_idx` (`username` ASC),
  INDEX `cs_depsynchist_ctc_idx` (`contenttypeclass` ASC)
)
  ENGINE =InnoDB
  DEFAULT CHARSET =utf8
  ROW_FORMAT=DYNAMIC ;

CREATE TABLE IF NOT EXISTS `cstudio_site` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `site_id` VARCHAR(255) NOT NULL,
  `name` VARCHAR(255) NOT NULL,
  `description` TEXT NULL,
  `status` VARCHAR(255) NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `id_UNIQUE` (`id` ASC),
  UNIQUE INDEX `site_id_UNIQUE` (`site_id` ASC),
  INDEX `site_id_idx` (`site_id` ASC)
)

    ENGINE =InnoDB
  DEFAULT CHARSET =utf8
  ROW_FORMAT=DYNAMIC ;

CREATE TABLE IF NOT EXISTS `cstudio_objectmetadata` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `site` VARCHAR(50) NOT NULL,
  `path` VARCHAR(2000) NOT NULL,
  `name` VARCHAR(255) NULL,
  `modified` DATETIME NULL,
  `modifier` VARCHAR(255) NULL,
  `owner` VARCHAR(255) NULL,
  `creator` VARCHAR(255) NULL,
  `firstname` VARCHAR(255) NULL,
  `lastname` VARCHAR(255) NULL,
  `lockowner` VARCHAR(255) NULL,
  `email` VARCHAR(255) NULL,
  `renamed` INT NULL,
  `oldurl` TEXT NULL,
  `deleteurl` TEXT NULL,
  `imagewidth` INT NULL,
  `imageheight` INT NULL,
  `approvedby` VARCHAR(255) NULL,
  `submittedby` VARCHAR(255) NULL,
  `submittedfordeletion` INT NULL,
  `sendemail` INT NULL,
  `submissioncomment` TEXT NULL,
  `launchdate` DATETIME NULL,
  PRIMARY KEY (`id`),
  UNIQUE `uq__om_site_path` (`site`, `path`(900))
)
  ENGINE =InnoDB
  DEFAULT CHARSET =utf8
  ROW_FORMAT=DYNAMIC ;

CREATE TABLE IF NOT EXISTS `cstudio_user`
(
  `username` VARCHAR(255) NOT NULL ,
  `password` VARCHAR(255) NOT NULL,
  `firstname` VARCHAR(255) NOT NULL,
  `lastname` VARCHAR(255) NOT NULL,
  `email` VARCHAR(255) NOT NULL,
  PRIMARY KEY (`username`)
)
  ENGINE =InnoDB
  DEFAULT CHARSET =utf8
  ROW_FORMAT=DYNAMIC ;

INSERT INTO CSTUDIO_USER (USERNAME, PASSWORD, FIRSTNAME, LASTNAME, EMAIL)
VALUES ('admin', 'vTwNOJ8GJdyrP7rrvQnpwsd2hCV1xRrJdTX2sb51i+w=|R68ms0Od3AngQMdEeKY6lA==', 'admin', 'admin', 'evaladmin@example.com') ;

CREATE TABLE CSTUDIO_GROUP
(
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL,
  `description` VARCHAR(3000),
  PRIMARY KEY (`id`)
)
  ENGINE =InnoDB
  DEFAULT CHARSET =utf8
  ROW_FORMAT=DYNAMIC ;

CREATE TABLE CSTUDIO_USERGROUP
(
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(255) NOT NULL,
  `groupid` BIGINT NOT NULL,
  PRIMARY KEY (`id`),
  FOREIGN KEY USER_UG_FOREIGN_KEY(username) REFERENCES CSTUDIO_USER(username),
  FOREIGN KEY GROUP_UG_FOREIGN_KEY(groupid) REFERENCES CSTUDIO_GROUP(groupid)
)
  ENGINE =InnoDB
  DEFAULT CHARSET =utf8
  ROW_FORMAT=DYNAMIC ;

INSERT INTO CSTUDIO_GROUP (NAME, DESCRIPTION) VALUES ('crafter-admin', 'crafter admin') ;
INSERT INTO CSTUDIO_GROUP (NAME, DESCRIPTION) VALUES ('crafter-create-sites', 'crafter-create-sites') ;

INSERT INTO CSTUDIO_USERGROUP (USERNAME, GROUPID) VALUES ('admin', 1) ;
INSERT INTO CSTUDIO_USERGROUP (USERNAME, GROUPID) VALUES ('admin', 2)