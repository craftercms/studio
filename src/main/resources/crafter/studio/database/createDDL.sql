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

CREATE PROCEDURE update_parent_id(IN siteId VARCHAR(50), IN rootPath VARCHAR(2000))
BEGIN
    DECLARE v_parent_id BIGINT;
    DECLARE v_parent_path VARCHAR(2000);
    DECLARE v_parent_item_path VARCHAR(2000);
    DECLARE v_finished INTEGER DEFAULT 0;
    DECLARE parent_cursor CURSOR FOR SELECT i.id as parent_id, REPLACE(i.path, '/index.xml','') AS parent_path, i.path
        AS parent_item_path FROM item i WHERE i.site_id = siteId AND (path = rootPath OR path LIKE concat(rootPath, '/%'));
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET v_finished = 1;
    OPEN parent_cursor;
    update_parent: LOOP
        FETCH parent_cursor INTO v_parent_id, v_parent_path, v_parent_item_path;
        IF v_finished = 1 THEN LEAVE update_parent;
        END IF;
        UPDATE item SET parent_id = v_parent_id WHERE site_id = siteId
            AND path RLIKE (concat(v_parent_path, '/[^/]+/index\.xml|', v_parent_path,'/(?!index\.xml)[^/]+$'));
    END LOOP update_parent;
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

CREATE PROCEDURE populateItemTable(IN siteId VARCHAR(50))
BEGIN
    DECLARE v_site_id BIGINT;
    DECLARE v_path VARCHAR(2048);
    DECLARE v_state_str VARCHAR(255);
    DECLARE v_sys_process INT;
    DECLARE v_state BIGINT;
    DECLARE v_owner VARCHAR(255);
    DECLARE v_owned_by BIGINT;
    DECLARE v_creator VARCHAR(255);
    DECLARE v_created_by BIGINT;
    DECLARE v_created_on TIMESTAMP;
    DECLARE v_modifier VARCHAR(255);
    DECLARE v_last_modified_by BIGINT;
    DECLARE v_last_modified_on TIMESTAMP;
    DECLARE v_commit_id VARCHAR(128);
    DECLARE v_finished INTEGER DEFAULT 0;
    DECLARE item_cursor CURSOR FOR
        SELECT im.path as item_path, ist.state as item_state, ist.system_processing as item_sys_process,
               im.owner as item_owner, im.creator as item_creator, im.modifier as item_modifier,
               im.modified as item_modified, im.commit_id as item_commit_id
        FROM item_state ist LEFT OUTER JOIN item_metadata im ON ist.site = im.site AND ist.path = im.path
        WHERE ist.site = siteId
        UNION
        SELECT im.path as item_path, ist.state as item_state, ist.system_processing as item_sys_process,
               im.owner as item_owner, im.creator as item_creator, im.modifier as item_modifier,
               im.modified as item_modified, im.commit_id as item_commit_id
        FROM item_state ist RIGHT OUTER JOIN item_metadata im ON ist.site = im.site AND ist.path = im.path
        WHERE ist.site = siteId;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET v_finished = 1;
    SELECT id INTO v_site_id FROM site WHERE site_id = siteId AND deleted = 0;
    DELETE FROM item WHERE site_id = v_site_id;
    OPEN item_cursor;
    insert_item: LOOP
        FETCH item_cursor INTO v_path, v_state_str, v_sys_process, v_owner, v_creator, v_modifier, v_last_modified_on, v_commit_id;
        IF v_finished = 1 THEN LEAVE insert_item;
        END IF;
        CASE
            WHEN v_state = 'NEW_UNPUBLISHED_LOCKED' THEN SELECT 11 + 16 * v_sys_process INTO v_state;
            WHEN v_state = 'NEW_UNPUBLISHED_UNLOCKED' THEN SELECT 3 + 16 * v_sys_process INTO v_state;
            WHEN v_state = 'NEW_SUBMITTED_WITH_WF_SCHEDULED' THEN SELECT 99 + 16 * v_sys_process INTO v_state;
            WHEN v_state = 'NEW_SUBMITTED_WITH_WF_SCHEDULED_LOCKED' THEN SELECT 107 + 16 * v_sys_process INTO v_state;
            WHEN v_state = 'NEW_SUBMITTED_WITH_WF_UNSCHEDULED' THEN SELECT 35 + 16 * v_sys_process INTO v_state;
            WHEN v_state = 'NEW_SUBMITTED_WITH_WF_UNSCHEDULED_LOCKED' THEN SELECT 43 + 16 * v_sys_process INTO v_state;
            WHEN v_state = 'NEW_SUBMITTED_NO_WF_SCHEDULED' THEN SELECT 67 + 16 * v_sys_process INTO v_state;
            WHEN v_state = 'NEW_SUBMITTED_NO_WF_SCHEDULED_LOCKED' THEN SELECT 75 + 16 * v_sys_process INTO v_state;
            WHEN v_state = 'NEW_SUBMITTED_NO_WF_UNSCHEDULED' THEN SELECT 3 + 16 * v_sys_process INTO v_state;
            WHEN v_state = 'NEW_PUBLISHING_FAILED' THEN SELECT 131 + 16 * v_sys_process INTO v_state;
            WHEN v_state = 'NEW_DELETED' THEN SELECT 5 + 16 * v_sys_process INTO v_state;
            WHEN v_state = 'EXISTING_UNEDITED_LOCKED' THEN SELECT 520 + 16 * v_sys_process INTO v_state;
            WHEN v_state = 'EXISTING_UNEDITED_UNLOCKED' THEN SELECT 512 + 16 * v_sys_process INTO v_state;
            WHEN v_state = 'EXISTING_EDITED_LOCKED' THEN SELECT 10 + 16 * v_sys_process INTO v_state;
            WHEN v_state = 'EXISTING_EDITED_UNLOCKED' THEN SELECT 2 + 16 * v_sys_process INTO v_state;
            WHEN v_state = 'EXISTING_SUBMITTED_WITH_WF_SCHEDULED' THEN SELECT 98 + 16 * v_sys_process INTO v_state;
            WHEN v_state = 'EXISTING_SUBMITTED_WITH_WF_SCHEDULED_LOCKED' THEN SELECT 106 + 16 * v_sys_process INTO v_state;
            WHEN v_state = 'EXISTING_SUBMITTED_WITH_WF_UNSCHEDULED' THEN SELECT 34 + 16 * v_sys_process INTO v_state;
            WHEN v_state = 'EXISTING_SUBMITTED_WITH_WF_UNSCHEDULED_LOCKED' THEN SELECT 42 + 16 * v_sys_process INTO v_state;
            WHEN v_state = 'EXISTING_SUBMITTED_NO_WF_SCHEDULED' THEN SELECT 66 + 16 * v_sys_process INTO v_state;
            WHEN v_state = 'EXISTING_SUBMITTED_NO_WF_SCHEDULED_LOCKED' THEN SELECT 74 + 16 * v_sys_process INTO v_state;
            WHEN v_state = 'EXISTING_SUBMITTED_NO_WF_UNSCHEDULED' THEN SELECT 2 + 16 * v_sys_process INTO v_state;
            WHEN v_state = 'EXISTING_PUBLISHING_FAILED' THEN SELECT 130 + 16 * v_sys_process INTO v_state;
            WHEN v_state = 'EXISTING_DELETED' THEN SELECT 4 + 16 * v_sys_process INTO v_state;
            ELSE SELECT 0 INTO v_state;
            END CASE;
        SELECT a.id INTO v_owned_by FROM (SELECT id FROM user WHERE username = v_owner UNION SELECT id from user WHERE username = v_owner LIMIT 1) as a;
        SELECT a.id INTO v_created_by FROM (SELECT id FROM user WHERE username = v_creator UNION SELECT id from user WHERE username = v_creator LIMIT 1) as a;
        SELECT a.id INTO v_last_modified_by FROM (SELECT id FROM user WHERE username = v_modifier UNION SELECT id from user WHERE username = v_modifier LIMIT 1) as a;
        INSERT INTO item (site_id, path, preview_url, state, owned_by, created_by, created_on, last_modified_by, last_modified_on, commit_id)
        VALUES (v_site_id, v_path, v_path, v_state, v_owned_by, v_created_by, v_created_on, v_last_modified_by, v_last_modified_on, v_commit_id);
    end loop insert_item;
    SELECT COUNT(1) FROM item WHERE site_id = v_site_id;
END ;

CREATE TABLE _meta (
  `version` VARCHAR(10) NOT NULL,
  `integrity` BIGINT(10),
  `studio_id` VARCHAR(40) NOT NULL,
  PRIMARY KEY (`version`)
) ;

INSERT INTO _meta (version, studio_id) VALUES ('3.2.0.21', UUID()) ;

CREATE TABLE IF NOT EXISTS `audit` (
  `id`                        BIGINT(20)    NOT NULL AUTO_INCREMENT,
  `organization_id`           BIGINT(20)    NOT NULL,
  `site_id`                   BIGINT(20)    NOT NULL,
  `operation`                 VARCHAR(32)   NOT NULL,
  `operation_timestamp`       DATETIME      NOT NULL,
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
  `published_repo_created`          INT           NOT NULL DEFAULT 0,
  `publishing_lock_owner`           VARCHAR(255)  NULL,
  `publishing_lock_heartbeat`       DATETIME      NULL,
  `state`                           VARCHAR(50)   NOT NULL DEFAULT 'CREATING',
  `last_synced_gitlog_commit_id`   VARCHAR(50)   NULL,
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

CREATE TABLE IF NOT EXISTS `item` (
  `id`                      BIGINT          NOT NULL    AUTO_INCREMENT,
  `record_last_updated`     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `site_id`                 BIGINT          NOT NULL,
  `path`                    VARCHAR(2048)   NOT NULL,
  `preview_url`             VARCHAR(2048)   NULL,
  `state`                   BIGINT          NOT NULL,
  `owned_by`                BIGINT          NULL,
  `created_by`              BIGINT          NULL,
  `created_on`              TIMESTAMP       NOT NULL    DEFAULT CURRENT_TIMESTAMP,
  `last_modified_by`        BIGINT          NULL,
  `last_modified_on`        TIMESTAMP       NOT NULL    DEFAULT CURRENT_TIMESTAMP,
  `label`                   VARCHAR(256)    NULL,
  `content_type_id`         VARCHAR(256)    NULL,
  `system_type`             VARCHAR(64)     NULL,
  `mime_type`               VARCHAR(64)     NULL,
  `disabled`                INT             NOT NULL    DEFAULT 0,
  `locale_code`             VARCHAR(16)     NULL,
  `translation_source_id`   BIGINT          NULL,
  `size`                    INT             NULL,
  `parent_id`               BIGINT          NULL,
  `commit_id`               VARCHAR(128)    NULL,
  PRIMARY KEY (`id`),
  FOREIGN KEY item_ix_created_by(`created_by`) REFERENCES `user` (`id`),
  FOREIGN KEY item_ix_last_modified_by(`last_modified_by`) REFERENCES `user` (`id`),
  FOREIGN KEY item_ix_owned_by(`owned_by`) REFERENCES `user` (`id`),
  FOREIGN KEY item_ix_site_id(`site_id`) REFERENCES `site` (`id`),
  FOREIGN KEY item_ix_parent(`parent_id`) REFERENCES `item` (`id`) ON DELETE CASCADE ,
  UNIQUE uq_i_site_path (`site_id`, `path`(900))
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
  `heartbeat`           DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
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
  `record_last_updated`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`cluster_id`, `remote_repository_id`),
  FOREIGN KEY cluster_remote_ix_cluster_id(`cluster_id`) REFERENCES `cluster` (`id`)
    ON DELETE CASCADE,
  FOREIGN KEY cluster_remote_ix_remote_id(`remote_repository_id`) REFERENCES `remote_repository` (`id`)
    ON DELETE CASCADE
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8
  ROW_FORMAT = DYNAMIC ;

CREATE TABLE IF NOT EXISTS cluster_site_sync_repo
(
    `cluster_node_id`                       BIGINT(20)      NOT NULL,
    `site_id`                               BIGINT(20)      NOT NULL,
    `node_last_commit_id`                   VARCHAR(50)     NULL,
    `node_last_verified_gitlog_commit_id`   VARCHAR(50)     NULL,
    `node_last_synced_gitlog_commit_id`    VARCHAR(50)   NULL,
    `site_state`                                 VARCHAR(50)     NOT NULL DEFAULT 'CREATING',
    `site_published_repo_created`                INT             NOT NULL DEFAULT 0,
    PRIMARY KEY (`cluster_node_id`, `site_id`),
    FOREIGN KEY cluster_site_ix_cluster_id(`cluster_node_id`) REFERENCES `cluster` (`id`)
        ON DELETE CASCADE,
    FOREIGN KEY cluster_site_ix_remote_id(`site_id`) REFERENCES `site` (`id`)
        ON DELETE CASCADE
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8
    ROW_FORMAT = DYNAMIC ;


INSERT IGNORE INTO site (site_id, name, description, system, state)
VALUES ('studio_root', 'Studio Root', 'Studio Root for global permissions', 1, 'CREATED') ;

INSERT IGNORE INTO group_user (user_id, group_id) VALUES (1, 1) ;

