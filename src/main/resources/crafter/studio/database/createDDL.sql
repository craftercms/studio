USE crafter ;

CREATE PROCEDURE addColumnIfNotExists(
    IN schemaName tinytext,
    IN tableName tinytext,
    IN columnName tinytext,
    IN columnDefinition text)
  BEGIN
    IF NOT EXISTS (
            SELECT * FROM information_schema.COLUMNS
            WHERE column_name = columnName
              AND table_name = tableName
              AND table_schema = schemaName
        )
    THEN
        SET @addColumn=CONCAT('ALTER TABLE ', schemaName, '.', tableName,
                              ' ADD COLUMN ', columnName, ' ', columnDefinition);
        PREPARE statement FROM @addColumn;
        EXECUTE statement;
    END IF;
  END ;

CREATE PROCEDURE dropColumnIfExists(
    IN schemaName tinytext,
    IN tableName tinytext,
    IN columnName tinytext)
BEGIN
    IF EXISTS (
            SELECT * FROM information_schema.COLUMNS
            WHERE column_name = columnName
              AND table_name = tableName
              AND table_schema = schemaName
        )
    THEN
        SET @dropColumn=CONCAT('ALTER TABLE ', schemaName, '.', tableName,
                              ' DROP COLUMN ', columnName);
        PREPARE statement FROM @dropColumn;
        EXECUTE statement;
    END IF;
END ;

CREATE PROCEDURE addUniqueIfNotExists(
    IN schemaName tinytext,
    IN tableName tinytext,
    IN uniqueName tinytext,
    IN uniqueDefinition text)
BEGIN
    IF NOT EXISTS (
            SELECT * FROM information_schema.STATISTICS
            WHERE index_name = uniqueName
              AND table_name = tableName
              AND table_schema = schemaName
        )
    THEN
        SET @addUnique=CONCAT('ALTER TABLE ', schemaName, '.', tableName,
                              ' ADD UNIQUE ', uniqueName, ' ', uniqueDefinition);
        PREPARE statement FROM @addUnique;
        EXECUTE statement;
    END IF;
END ;

CREATE PROCEDURE dropIndexIfExists(
    IN schemaName tinytext,
    IN tableName tinytext,
    IN indexName tinytext)
BEGIN
    IF EXISTS (
            SELECT * FROM information_schema.STATISTICS
            WHERE index_name = indexName
              AND table_name = tableName
              AND table_schema = schemaName
        )
    THEN
        SET @dropIndex=CONCAT('ALTER TABLE ', schemaName, '.', tableName,
                               ' DROP INDEX ', indexName);
        PREPARE statement FROM @dropIndex;
        EXECUTE statement;
    END IF;
END ;

CREATE PROCEDURE addIndexIfNotExists(
    IN schemaName tinytext,
    IN tableName tinytext,
    IN indexName tinytext,
    IN indexDefinition text)
BEGIN
    IF NOT EXISTS (
            SELECT * FROM information_schema.STATISTICS
            WHERE index_name = indexName
              AND table_name = tableName
              AND table_schema = schemaName
        )
    THEN
        SET @addIndex=CONCAT('ALTER TABLE ', schemaName, '.', tableName,
                             ' ADD INDEX ', indexName, ' ', indexDefinition);
        PREPARE statement FROM @addIndex;
        EXECUTE statement;
    END IF;
END ;

CREATE TABLE _meta (
  `version` VARCHAR(10) NOT NULL,
  `integrity` BIGINT(10),
  `studio_id` VARCHAR(40) NOT NULL,
  PRIMARY KEY (`version`)
) ;

INSERT INTO _meta (version, studio_id) VALUES ('3.1.0.34', UUID()) ;

CREATE TABLE IF NOT EXISTS `audit` (
  `id`                        BIGINT(20)    NOT NULL AUTO_INCREMENT,
  `organization_id`           BIGINT(20)    NOT NULL,
  `site_id`                   BIGINT(20)    NOT NULL,
  `operation`                 VARCHAR(32)   NOT NULL,
  `operation_timestamp`       DATETIME      NOT NULL,
  `origin`                    VARCHAR(16)   NOT NULL,
  `primary_target_id`         VARCHAR(256)  NOT NULL,
  `primary_target_type`       VARCHAR(32)   NOT NULL,
  `primary_target_subtype`    VARCHAR(32)   NULL,
  `primary_target_value`      VARCHAR(512)  NOT NULL,
  `actor_id`                  VARCHAR(32)   NOT NULL,
  `actor_details`             VARCHAR(64)   NULL,
  `cluster_node_id`           VARCHAR(255)  NULL,
  PRIMARY KEY (`id`),
  KEY `audit_actor_idx` (`actor_id`),
  KEY `audit_site_idx` (`site_id`),
  KEY `audit_operation_idx` (`operation`),
  KEY `audit_origin_idx` (`origin`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8
  ROW_FORMAT = DYNAMIC ;

CREATE TABLE IF NOT EXISTS `audit_parameters` (
  `id`                BIGINT(20) NOT NULL AUTO_INCREMENT,
  `audit_id`          BIGINT(20) NOT NULL,
  `target_id`         VARCHAR(256)  NOT NULL,
  `target_type`       VARCHAR(32)   NOT NULL,
  `target_subtype`    VARCHAR(32)   NULL,
  `target_value`      VARCHAR(512)  NOT NULL,
  PRIMARY KEY (`id`),
  KEY `audit_parameters_audit_id_idx` (`audit_id`),
  KEY `audit_parameters_target_id_idx` (`target_id`),
  KEY `audit_parameters_target_value_idx` (`target_value`)
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
  `site_uuid`                       VARCHAR(50)   NOT NULL,
  `site_id`                         VARCHAR(50)   NOT NULL,
  `name`                            VARCHAR(255)  NOT NULL,
  `description`                     TEXT          NULL,
  `status`                          VARCHAR(255)  NULL,
  `deleted`                         INT           NOT NULL DEFAULT 0,
  `last_commit_id`                  VARCHAR(50)   NULL,
  `system`                          INT           NOT NULL DEFAULT 0,
  `publishing_enabled`              INT           NOT NULL DEFAULT 1,
  `publishing_status_message`       VARCHAR(2000) NULL,
  `last_verified_gitlog_commit_id`  VARCHAR(50)   NULL,
  `sandbox_branch`                  VARCHAR(255)  NOT NULL DEFAULT 'master',
  `search_engine`                   VARCHAR(20)   NOT NULL DEFAULT 'Elasticsearch',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `id_unique` (`id` ASC),
  UNIQUE INDEX `site_uuid_site_id_unique` (`site_uuid` ASC, `site_id` ASC),
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
  `id`                    BIGINT(20)   NOT NULL AUTO_INCREMENT,
  `record_last_updated`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `username`              VARCHAR(32)  NOT NULL,
  `password`              VARCHAR(128)  NOT NULL,
  `first_name`             VARCHAR(16)  NOT NULL,
  `last_name`              VARCHAR(16)  NOT NULL,
  `externally_managed`    INT          NOT NULL DEFAULT 0,
  `timezone`              VARCHAR(16)  NULL,
  `locale`                VARCHAR(8)   NULL,
  `email`                 VARCHAR(255) NOT NULL,
  `enabled`               INT          NOT NULL,
  `deleted`               INT          NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  INDEX `user_ix_record_last_updated` (`record_last_updated` DESC),
  UNIQUE INDEX `user_ix_username` (`username`),
  INDEX `user_ix_first_name` (`first_name`),
  INDEX `user_ix_last_name` (`last_name`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8
  ROW_FORMAT = DYNAMIC ;

INSERT IGNORE INTO `user` (id, record_last_updated, username, password, first_name, last_name,
                           externally_managed, timezone, locale, email, enabled, deleted)
VALUES (1, CURRENT_TIMESTAMP, 'admin', 'vTwNOJ8GJdyrP7rrvQnpwsd2hCV1xRrJdTX2sb51i+w=|R68ms0Od3AngQMdEeKY6lA==',
        'admin', 'admin', 0, 'EST5EDT', 'en/US', 'evaladmin@example.com', 1, 0) ;

CREATE TABLE IF NOT EXISTS `organization`
(
  `id`                  BIGINT(20)  NOT NULL AUTO_INCREMENT,
  `record_last_updated` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `org_name`            VARCHAR(32) NOT NULL,
  `org_desc`            TEXT        NULL,
  PRIMARY KEY (`id`),
  INDEX `organization_ix_record_last_updated` (`record_last_updated` DESC),
  UNIQUE INDEX `organization_ix_org_name` (`org_name`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8
  ROW_FORMAT = DYNAMIC ;

INSERT IGNORE INTO `organization` (id, record_last_updated, org_name, org_desc)
VALUES (1, CURRENT_TIMESTAMP, 'studio', 'studio default organization') ;


CREATE TABLE IF NOT EXISTS `organization_user`
(
  `user_id`   BIGINT(20) NOT NULL,
  `org_id`    BIGINT(20) NOT NULL,
  `record_last_updated` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`, `org_id`),
  FOREIGN KEY org_member_ix_user_id(user_id) REFERENCES `user` (`id`) ON DELETE CASCADE,
  FOREIGN KEY org_member_ix_org_id(org_id) REFERENCES `organization` (`id`) ON DELETE CASCADE,
  INDEX `org_member_ix_record_last_updated` (`record_last_updated` DESC)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8
  ROW_FORMAT = DYNAMIC ;

INSERT IGNORE INTO `organization_user` (user_id, org_id)
VALUES (1, 1) ;

CREATE TABLE IF NOT EXISTS `group`
(
  `id`                  BIGINT(20)  NOT NULL AUTO_INCREMENT,
  `record_last_updated` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `org_id`              BIGINT(20)  NOT NULL,
  `group_name`          VARCHAR(32) NOT NULL,
  `group_description`   TEXT,
  PRIMARY KEY (`id`),
  INDEX `group_ix_record_last_updated` (`record_last_updated` DESC),
  FOREIGN KEY group_ix_org_id(org_id) REFERENCES `organization` (`id`) ON DELETE CASCADE,
  UNIQUE INDEX `group_ix_group_name` (`group_name`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8
  ROW_FORMAT = DYNAMIC ;

INSERT IGNORE INTO `group` (id, record_last_updated, org_id, group_name, group_description)
VALUES (1, CURRENT_TIMESTAMP, 1, 'system_admin', 'System Administrator group') ;

INSERT IGNORE INTO `group` (id, record_last_updated, org_id, group_name, group_description)
VALUES (2, CURRENT_TIMESTAMP, 1, 'site_admin', 'Site Administrator group') ;

INSERT IGNORE INTO `group` (id, record_last_updated, org_id, group_name, group_description)
VALUES (3, CURRENT_TIMESTAMP, 1, 'site_author', 'Site Author group') ;

INSERT IGNORE INTO `group` (id, record_last_updated, org_id, group_name, group_description)
VALUES (4, CURRENT_TIMESTAMP, 1, 'site_publisher', 'Site Publisher group') ;

INSERT IGNORE INTO `group` (id, record_last_updated, org_id, group_name, group_description)
VALUES (5, CURRENT_TIMESTAMP, 1, 'site_developer', 'Site Developer group') ;

INSERT IGNORE INTO `group` (id, record_last_updated, org_id, group_name, group_description)
VALUES (6, CURRENT_TIMESTAMP, 1, 'site_reviewer', 'Site Reviewer group') ;

CREATE TABLE IF NOT EXISTS group_user
(
  `user_id`  BIGINT(20) NOT NULL,
  `group_id`  BIGINT(20)       NOT NULL,
  `record_last_updated` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`, `group_id`),
  FOREIGN KEY group_member_ix_user_id(`user_id`) REFERENCES `user` (`id`)
    ON DELETE CASCADE,
  FOREIGN KEY group_member_ix_group_id(`group_id`) REFERENCES `group` (`id`)
    ON DELETE CASCADE,
  INDEX `group_member_ix_record_last_updated` (`record_last_updated` DESC)
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

CREATE TABLE IF NOT EXISTS cluster
(
  `id`                  BIGINT(20)    NOT NULL AUTO_INCREMENT,
  `local_address`            VARCHAR(255)   NOT NULL,
  `state`              VARCHAR(50)   NOT NULL,
  `git_url`             VARCHAR(1024)  NOT NULL,
  `git_remote_name`     VARCHAR(255)   NOT NULL,
  `git_auth_type`       VARCHAR(16)   NOT NULL,
  `git_username`        VARCHAR(255)  NULL,
  `git_password`        VARCHAR(255)  NULL,
  `git_token`           VARCHAR(255)  NULL,
  `git_private_key`     TEXT          NULL,
  `heartbeat`           DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE `uq_cl_git_url` (`git_url`),
  UNIQUE `uq_cl_git_remote_name` (`git_remote_name`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8
  ROW_FORMAT = DYNAMIC ;


INSERT IGNORE INTO site (site_id, name, description, system)
VALUES ('studio_root', 'Studio Root', 'Studio Root for global permissions', 1) ;

INSERT IGNORE INTO group_user (user_id, group_id) VALUES (1, 1) ;

