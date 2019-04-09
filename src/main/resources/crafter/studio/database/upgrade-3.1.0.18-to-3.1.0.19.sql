DROP TABLE IF EXISTS `new_audit` ;

CREATE TABLE IF NOT EXISTS `new_audit` (
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
  `target_type`       VARCHAR(16)   NOT NULL,
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

DROP PROCEDURE IF EXISTS migrate_audit ;

CREATE PROCEDURE migrate_audit ()
  BEGIN
    DECLARE v_finished INTEGER DEFAULT 0;
    DECLARE v_id BIGINT(20);
    DECLARE v_modified_date DATETIME;
    DECLARE v_creation_date DATETIME;
    DECLARE v_summary TEXT;
    DECLARE v_summary_format VARCHAR(255);
    DECLARE v_content_id TEXT;
    DECLARE v_site_network VARCHAR(50);
    DECLARE v_activity_type VARCHAR(255);
    DECLARE v_content_type VARCHAR(255);
    DECLARE v_post_user_id VARCHAR(255);
    DECLARE v_source VARCHAR(255);
    DECLARE v_site_id BIGINT(20);
    DECLARE v_operation VARCHAR(32) DEFAULT '';
    DECLARE v_origin VARCHAR(16);
    DECLARE v_target_type VARCHAR(32);
    -- declare cursor for audit log
    DEClARE audit_cursor CURSOR FOR SELECT id, modified_date, creation_date, summary, summary_format, content_id,
    site_network, activity_type, content_type, post_user_id, source FROM audit;
    -- declare NOT FOUND handler
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET v_finished = 1;
    OPEN audit_cursor;
    get_audit_entry: LOOP
      FETCH audit_cursor INTO v_id, v_modified_date, v_creation_date, v_summary, v_summary_format, v_content_id, v_site_network, v_activity_type, v_content_type, v_post_user_id, v_source;
      IF v_finished = 1 THEN LEAVE get_audit_entry; END IF;
      SELECT id INTO v_site_id FROM site WHERE site_id = v_site_network;
      SELECT CASE
        WHEN v_activity_type = 'CREATED' THEN 'CREATE'
        WHEN v_activity_type = 'UPDATED' THEN 'UPDATE'
        WHEN v_activity_type = 'DELETED' THEN 'DELETE'
        WHEN v_activity_type = 'MOVED' THEN 'MOVE'
        WHEN v_activity_type = 'ADD_USER_TO_GROUP' THEN 'ADD_MEMBERS'
        WHEN v_activity_type = 'REMOVE_USER_FROM_GROUP' THEN 'REMOVE_MEMBERS'
        WHEN v_activity_type = 'LOGIN' THEN 'LOGIN'
        WHEN v_activity_type = 'LOGIN_FAILED' THEN 'LOGIN_FAILED'
        WHEN v_activity_type = 'LOGOUT' THEN 'LOGOUT'
        WHEN v_activity_type = 'CREATE_SITE' THEN 'CREATE'
        WHEN v_activity_type = 'DELETE_SITE' THEN 'DELETE'
        WHEN v_activity_type = 'ADD_REMOTE' THEN 'ADD_REMOTE'
        WHEN v_activity_type = 'REMOVE_REMOTE' THEN 'REMOVE_REMOTE'
        WHEN v_activity_type = 'PUSH_TO_REMOTE' THEN 'PUSH_TO_REMOTE'
        WHEN v_activity_type = 'PULL_FROM_REMOTE' THEN 'PULL_FROM_REMOTE'
        WHEN v_activity_type = 'REQUEST_PUBLISH' THEN 'REQUEST_PUBLISH'
        WHEN v_activity_type = 'APPROVE' THEN 'APPROVE'
        WHEN v_activity_type = 'APPROVE_SCHEDULED' THEN 'APPROVE_SCHEDULED'
        WHEN v_activity_type = 'REJECT' THEN 'REJECT'
        WHEN v_activity_type = 'PUBLISHED' THEN 'PUBLISHED'
        ELSE 'UNKNOWN'
      END
      INTO v_operation;
      SELECT CASE
        WHEN v_source = 'API' THEN 'API'
        WHEN v_source = 'REPOSITORY' THEN 'GIT'
      END
      INTO v_origin;
      SELECT CASE
        WHEN v_content_type = 'user' THEN 'User'
        WHEN v_content_type = 'site' THEN 'Site'
        WHEN v_content_type = 'group' THEN 'Group'
        WHEN v_content_type = 'folder' THEN 'Folder'
        WHEN v_content_type = 'page' THEN 'Content Item'
        WHEN v_content_type = 'component' THEN 'Content Item'
        WHEN v_content_type = 'asset' THEN 'Content Item'
        WHEN v_content_type = 'renderingTemplate' THEN 'Content Item'
        WHEN v_content_type = 'document' THEN 'Content Item'
        WHEN v_content_type = 'taxonomy' THEN 'Content Item'
        WHEN v_content_type = 'configuration' THEN 'Content Item'
        WHEN v_content_type = 'remoteRepository' THEN 'Remote Repository'
        ELSE 'unknown'
      END
      INTO v_target_type;
      INSERT INTO new_audit (organization_id, site_id, operation, operation_timestamp, origin, primary_target_id, primary_target_type, primary_target_subtype, primary_target_value, actor_id, actor_details)
      VALUES (1, v_site_id, v_operation, v_creation_date, v_origin, CONCAT(v_site_network,':',v_content_id), v_target_type, v_content_type, v_content_id, v_post_user_id, v_post_user_id);
    END LOOP get_audit_entry;
    CLOSE audit_cursor;
  END ;

CALL migrate_audit() ;

ALTER TABLE `audit` RENAME `audit_old` ;

ALTER TABLE `new_audit` RENAME `audit` ;

DROP PROCEDURE migrate_audit ;

UPDATE _meta SET version = '3.1.0.19' ;