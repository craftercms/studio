USE @crafter_schema_name ;

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

CREATE PROCEDURE tryLockPublishingForSite(
    IN siteId VARCHAR(50),
    IN lockOwnerId VARCHAR(255),
    IN ttl INT,
    OUT locked INT)
BEGIN
    DECLARE v_lock_owner_id VARCHAR(255);
    DECLARE v_lock_heartbeat DATETIME;
    SELECT publishing_lock_owner, publishing_lock_heartbeat INTO  v_lock_owner_id, v_lock_heartbeat FROM site
    WHERE site_id = siteId AND deleted = 0;
    SET locked = 0;
    IF (v_lock_owner_id IS NULL OR v_lock_owner_id = '' OR v_lock_owner_id = lockOwnerId OR DATE_ADD(v_lock_heartbeat, INTERVAL ttl MINUTE) < CURRENT_TIMESTAMP)
    THEN
        UPDATE site SET publishing_lock_owner = lockOwnerId, publishing_lock_heartbeat = CURRENT_TIMESTAMP WHERE site_id = siteId AND deleted = 0;
        SET locked = 1;
    END IF;
    SELECT locked;
END ;

CREATE TABLE _meta (
  `version` VARCHAR(10) NOT NULL,
  `integrity` BIGINT(10),
  `studio_id` VARCHAR(40) NOT NULL,
  PRIMARY KEY (`version`)
) ;

INSERT INTO _meta (version, studio_id) VALUES ('4.0.1.7', UUID()) ;

CREATE TABLE IF NOT EXISTS `audit` (
  `id`                        BIGINT(20)    NOT NULL AUTO_INCREMENT,
  `organization_id`           BIGINT(20)    NOT NULL,
  `site_id`                   BIGINT(20)    NOT NULL,
  `operation`                 VARCHAR(32)   NOT NULL,
  `operation_timestamp`       TIMESTAMP      NOT NULL,
  `origin`                    VARCHAR(16)   NOT NULL,
  `primary_target_id`         VARCHAR(1024)  NOT NULL,
  `primary_target_type`       VARCHAR(32)   NOT NULL,
  `primary_target_subtype`    VARCHAR(32)   NULL,
  `primary_target_value`      VARCHAR(1024)  NOT NULL,
  `actor_id`                  VARCHAR(255)  NOT NULL,
  `actor_details`             VARCHAR(255)  NULL,
  `cluster_node_id`           VARCHAR(255)  NULL,
  `commit_id`                 VARCHAR(50)   NULL,
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
    `target_id`         VARCHAR(1024)  NOT NULL,
    `target_type`       VARCHAR(32)   NOT NULL,
    `target_subtype`    VARCHAR(32)   NULL,
    `target_value`      VARCHAR(1024)  NOT NULL,
    PRIMARY KEY (`id`),
    FOREIGN KEY `audit_parameters_ix_audit_id` (`audit_id`) REFERENCES `audit` (`id`)
       ON DELETE CASCADE,
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
  `scheduleddate`     TIMESTAMP     NOT NULL,
  `state`             VARCHAR(50)  NOT NULL,
  `action`            VARCHAR(20)  NOT NULL,
  `contenttypeclass`  VARCHAR(20)  NULL,
  `submission_type`   VARCHAR(32)  NULL,
  `submissioncomment` TEXT         NULL,
  `commit_id`         VARCHAR(50)  NULL,
  `package_id`         VARCHAR(50)  NULL,
  `label`             VARCHAR(256) NULL,
  `published_on`      TIMESTAMP     NULL,
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
  `deleted`                         INT           NOT NULL DEFAULT 0,
  `last_commit_id`                  VARCHAR(50)   NULL,
  `system`                          INT           NOT NULL DEFAULT 0,
  `publishing_enabled`              INT           NOT NULL DEFAULT 1,
  `publishing_status`               VARCHAR(20)   NULL,
  `last_verified_gitlog_commit_id`  VARCHAR(50)   NULL,
  `sandbox_branch`                  VARCHAR(255)  NOT NULL DEFAULT 'master',
  `published_repo_created`          INT           NOT NULL DEFAULT 0,
  `publishing_lock_owner`           VARCHAR(255)  NULL,
  `publishing_lock_heartbeat`       TIMESTAMP      NULL,
  `state`                           VARCHAR(50)   NOT NULL DEFAULT 'INITIALIZING',
  `last_synced_gitlog_commit_id`   VARCHAR(50)   NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `id_unique` (`id` ASC),
  UNIQUE INDEX `site_uuid_site_id_unique` (`site_uuid` ASC, `site_id` ASC),
  INDEX `site_id_idx` (`site_id` ASC)
)

  ENGINE = InnoDB
  DEFAULT CHARSET = utf8
  ROW_FORMAT = DYNAMIC ;

CREATE TABLE IF NOT EXISTS `user`
(
  `id`                    BIGINT(20)   NOT NULL AUTO_INCREMENT,
  `record_last_updated`   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `username`              VARCHAR(255)  NOT NULL,
  `password`              VARCHAR(128)  NOT NULL,
  `first_name`             VARCHAR(32)  NOT NULL,
  `last_name`              VARCHAR(32)  NOT NULL,
  `externally_managed`    INT          NOT NULL DEFAULT 0,
  `timezone`              VARCHAR(16)  NULL,
  `locale`                VARCHAR(8)   NULL,
  `email`                 VARCHAR(255) NOT NULL,
  `enabled`               INT          NOT NULL,
  `deleted`               INT          NOT NULL DEFAULT 0,
  `avatar`                TEXT         NULL, -- TODO: update the user service to include this new field
  PRIMARY KEY (`id`),
  INDEX `user_ix_record_last_updated` (`record_last_updated` DESC),
  UNIQUE INDEX `user_ix_username` (`username`),
  INDEX `user_ix_first_name` (`first_name`),
  INDEX `user_ix_last_name` (`last_name`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8
  ROW_FORMAT = DYNAMIC ;

CREATE TABLE IF NOT EXISTS `activity_stream` (
    `id`                        BIGINT(20)      NOT NULL AUTO_INCREMENT,
    `site_id`                   BIGINT(20)      NOT NULL,
    `user_id`                   BIGINT(20)      NOT NULL,
    `action`                    VARCHAR(32)     NOT NULL,
    `action_timestamp`          TIMESTAMP       NOT NULL,
    `item_id`                   BIGINT(20)      NULL,
    `item_path`                 VARCHAR(2048)   NULL,
    `item_label`                VARCHAR(256)    NULL,
    `package_id`                VARCHAR(50)     NULL,
    PRIMARY KEY (`id`),
    FOREIGN KEY `activity_user_idx` (`user_id`) REFERENCES `user`(`id`),
    FOREIGN KEY `activity_site_idx` (`site_id`) REFERENCES `site`(`id`),
    INDEX `activity_action_idx` (`action` ASC)
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8
    ROW_FORMAT = DYNAMIC ;

INSERT IGNORE INTO `user` (id, record_last_updated, username, password, first_name, last_name,
                           externally_managed, timezone, locale, email, enabled, deleted)
VALUES (1, CURRENT_TIMESTAMP, 'admin', 'vTwNOJ8GJdyrP7rrvQnpwsd2hCV1xRrJdTX2sb51i+w=|R68ms0Od3AngQMdEeKY6lA==',
        'admin', 'admin', 0, 'EST5EDT', 'en/US', 'evaladmin@example.com', 1, 0) ;

INSERT IGNORE INTO `user` (id, record_last_updated, username, password, first_name, last_name,
                           externally_managed, timezone, locale, email, enabled, deleted)
VALUES (2, CURRENT_TIMESTAMP, 'git_repo_user', '',
           'Git Repo', 'User', 0, 'EST5EDT', 'en/US', 'evalgit@example.com', 1, 0) ;

CREATE TABLE IF NOT EXISTS `user_properties`
(
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT(20) NOT NULL,
  `site_id` BIGINT(20) NOT NULL,
  `property_key` VARCHAR(255) NOT NULL,
  `property_value` TEXT NOT NULL,
  PRIMARY KEY (`id`),
  FOREIGN KEY `user_property_ix_user_id` (`user_id`) REFERENCES `user` (`id`),
  FOREIGN KEY `user_property_ix_site_id` (`site_id`) REFERENCES `site` (`id`),
  UNIQUE INDEX `user_property_ix_property_key` (`user_id`, `site_id`, `property_key`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8
  ROW_FORMAT = DYNAMIC ;

CREATE TABLE IF NOT EXISTS `organization`
(
  `id`                  BIGINT(20)  NOT NULL AUTO_INCREMENT,
  `record_last_updated` TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
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
  `record_last_updated` TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
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
  `record_last_updated` TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `org_id`              BIGINT(20)  NOT NULL,
  `group_name`          VARCHAR(512) NOT NULL,
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
  `record_last_updated` TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
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

CREATE TABLE IF NOT EXISTS `item` (
  `id`                      BIGINT          NOT NULL    AUTO_INCREMENT,
  `record_last_updated`     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `site_id`                 BIGINT          NOT NULL,
  `path`                    VARCHAR(2048)   NOT NULL,
  `preview_url`             VARCHAR(2048)   NULL,
  `state`                   BIGINT          NOT NULL,
  `locked_by`               BIGINT          NULL,
  `created_by`              BIGINT          NULL,
  `created_on`              TIMESTAMP       NOT NULL    DEFAULT CURRENT_TIMESTAMP,
  `last_modified_by`        BIGINT          NULL,
  `last_modified_on`        TIMESTAMP       NOT NULL    DEFAULT CURRENT_TIMESTAMP,
  `last_published_on`       TIMESTAMP       NULL,
  `label`                   VARCHAR(256)    NULL,
  `content_type_id`         VARCHAR(256)    NULL,
  `system_type`             VARCHAR(64)     NULL,
  `mime_type`               VARCHAR(96)     NULL,
  `locale_code`             VARCHAR(16)     NULL,
  `translation_source_id`   BIGINT          NULL,
  `size`                    BIGINT          NULL,
  `parent_id`               BIGINT          NULL,
  `commit_id`               VARCHAR(128)    NULL,
  `previous_path`           VARCHAR(2048)   NULL,
  `ignored`                 INT             NOT NULL    DEFAULT 0,
  PRIMARY KEY (`id`),
  FOREIGN KEY item_ix_created_by(`created_by`) REFERENCES `user` (`id`),
  FOREIGN KEY item_ix_last_modified_by(`last_modified_by`) REFERENCES `user` (`id`),
  FOREIGN KEY item_ix_locked_by(`locked_by`) REFERENCES `user` (`id`),
  FOREIGN KEY item_ix_site_id(`site_id`) REFERENCES `site` (`id`),
  FOREIGN KEY item_ix_parent(`parent_id`) REFERENCES `item` (`id`) ON DELETE CASCADE ,
  UNIQUE uq_i_site_path (`site_id`, `path`(900)),
  INDEX item_i_path (`path` ASC)
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8
    ROW_FORMAT = DYNAMIC ;

CREATE TABLE IF NOT EXISTS `item_translation` (
  `id`                      BIGINT(20) NOT NULL AUTO_INCREMENT,
  `record_last_updated`     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `source_id`               BIGINT(20)      NOT NULL,
  `translation_id`          BIGINT(20)      NOT NULL,
  `locale_code`             VARCHAR(16)     NOT NULL,
  `date_translated`         TIMESTAMP       NOT NULL,
  PRIMARY KEY (`id`),
  FOREIGN KEY `item_translation_ix_source`(`source_id`) REFERENCES `item` (`id`) ON DELETE CASCADE,
  FOREIGN KEY `item_translation_ix_translation`(`translation_id`) REFERENCES `item` (`id`) ON DELETE CASCADE
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8
    ROW_FORMAT = DYNAMIC ;

CREATE TABLE IF NOT EXISTS workflow
(
    `id`                    BIGINT(20)      NOT NULL AUTO_INCREMENT,
    `item_id`               BIGINT(20)      NOT NULL,
    `target_environment`    VARCHAR(20)     NOT NULL,
    `state`                 VARCHAR(16)     NOT NULL,
    `submitter_id`          BIGINT(20)      NULL,
    `submitter_comment`     TEXT            NULL,
    `reviewer_id`           BIGINT(20)      NULL,
    `reviewer_comment`      TEXT            NULL,
    `schedule`              TIMESTAMP       NULL,
    `publishing_package_id` VARCHAR(50)     NULL,
    `submission_type`       VARCHAR(32)     NULL,
    `notify_submitter`      INT             NOT NULL DEFAULT 0,
    `label`                 VARCHAR(256)    NULL,
    PRIMARY KEY (`id`),
    FOREIGN KEY `workflow_ix_item`(`item_id`) REFERENCES `item` (`id`) ON DELETE CASCADE,
    FOREIGN KEY `workflow_ix_submitter`(`submitter_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    FOREIGN KEY `workflow_ix_reviewer`(`reviewer_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
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
  `audited`     INT           NOT NULL DEFAULT 0,
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
  `heartbeat`           TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `available`           INT           NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`),
  UNIQUE `uq_cl_git_url` (`git_url`),
  UNIQUE `uq_cl_git_remote_name` (`git_remote_name`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8
  ROW_FORMAT = DYNAMIC ;

CREATE TABLE IF NOT EXISTS cluster_remote_repository
(
  `cluster_id`                  BIGINT(20)    NOT NULL,
  `remote_repository_id`        BIGINT(20)    NOT NULL,
  `record_last_updated`   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`cluster_id`, `remote_repository_id`),
  FOREIGN KEY cluster_remote_ix_cluster_id(`cluster_id`) REFERENCES `cluster` (`id`)
    ON DELETE CASCADE,
  FOREIGN KEY cluster_remote_ix_remote_id(`remote_repository_id`) REFERENCES `remote_repository` (`id`)
    ON DELETE CASCADE
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8
  ROW_FORMAT = DYNAMIC ;

CREATE TABLE IF NOT EXISTS `refresh_token`
(
    `user_id` BIGINT(20) PRIMARY KEY, -- Add FK
    `token` VARCHAR(50) NOT NULL,
    `last_updated_on` TIMESTAMP,
    `created_on` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8
    ROW_FORMAT = DYNAMIC ;

CREATE TABLE IF NOT EXISTS `access_token`
(
    `id`      BIGINT(20) PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT(20), -- Add FK,
    `label`   VARCHAR(2550) NOT NULL,
    `enabled` BOOLEAN DEFAULT true,
    `last_updated_on` TIMESTAMP,
    `created_on` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `expires_at` TIMESTAMP NULL DEFAULT NULL
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8
    ROW_FORMAT = DYNAMIC ;

INSERT IGNORE INTO site (site_id, name, description, system, state)
VALUES ('studio_root', 'Studio Root', 'Studio Root for global permissions', 1, 'READY') ;

INSERT IGNORE INTO group_user (user_id, group_id) VALUES (1, 1) ;

