DROP TABLE IF EXISTS `new_audit` ;

CREATE TABLE IF NOT EXISTS `new_audit` (
  `id`                        BIGINT(20)    NOT NULL AUTO_INCREMENT,
  `organization_id`           BIGINT(20)    NOT NULL,
  `site_id`                   BIGINT(20)    NOT NULL,
  `operation`                 VARCHAR(16)   NOT NULL,
  `operation_timestamp`       DATETIME      NOT NULL,
  `origin`                    VARCHAR(16)   NOT NULL,
  `primary_target_id`         VARCHAR(256)  NOT NULL,
  `primary_target_type`       VARCHAR(16)   NOT NULL,
  `primary_target_subtype`    VARCHAR(32)   NOT NULL,
  `primary_target_value`      VARCHAR(512)  NOT NULL,
  `actor_id`                  VARCHAR(32)   NOT NULL,
  `actor_details`             VARCHAR(64)   NOT NULL,
  PRIMARY KEY (`id`),
  KEY `audit_actor_idx` (`actor_id`),
  KEY `audit_site_idx` (`site_id`),
  KEY `audit_operation_idx` (`operation`),
  KEY `audit_origin_idx` (`origin`)
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
    DECLARE v_operation VARCHAR(16) DEFAULT '';
    DECLARE v_origin VARCHAR(16);
    DECLARE v_target_type VARCHAR(16);
    -- declare cursor for audit log
    DEClARE audit_cursor CURSOR FOR SELECT id, modified_date, creation_date, summary, summary_format, content_id,
    site_network, activity_type, content_type, post_user_id, source FROM audit;
    -- declare NOT FOUND handler
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET v_finished = 1;
    OPEN audit_cursor;
    get_audit_entry: LOOP
      FETCH audit_cursor INTO v_id, v_modified_date, v_creation_date, v_summary, v_summary_format, v_content_id, v_site_network, v_activity_type, v_content_type, v_post_user_id, v_source;
      IF v_finished = 1 THEN LEAVE get_audit_entry; END IF;
      select id INTO v_site_id from site where site_id = v_site_network;
      select case
        when v_activity_type = 'CREATED' then 'CREATE'
        when v_activity_type = 'UPDATED' then 'UPDATE'
        when v_activity_type = 'DELETED' then 'DELETE'
        when v_activity_type = 'MOVED' then 'MOVE'
        when v_activity_type = 'ADD_USER_TO_GROUP' then 'ADD_MEMBERS'
        when v_activity_type = 'REMOVE_USER_FROM_GROUP' then 'REMOVE_MEMBERS'
        when v_activity_type = 'LOGIN' then 'LOGIN'
        when v_activity_type = 'LOGIN_FAILED' then 'LOGIN_FAILED'
        when v_activity_type = 'LOGOUT' then 'LOGOUT'
        when v_activity_type = 'CREATE_SITE' then 'CREATE'
        when v_activity_type = 'DELETE_SITE' then 'DELETE'
        when v_activity_type = 'ADD_REMOTE' then 'ADD_REMOTE'
        when v_activity_type = 'REMOVE_REMOTE' then 'REMOVE_REMOTE'
        when v_activity_type = 'PUSH_TO_REMOTE' then 'PUSH_TO_REMOTE'
        when v_activity_type = 'PULL_FROM_REMOTE' then 'PULL_TO_REMOTE'
        when v_activity_type = 'REQUEST_PUBLISH' then 'REQUEST_PUBLISH'
        when v_activity_type = 'APPROVE' then 'APPROVE'
        when v_activity_type = 'APPROVE_SCHEDULED' then 'APPROVE_SCHEDULED'
        when v_activity_type = 'REJECT' then 'REJECT'
        when v_activity_type = 'PUBLISHED' then 'PUBLISHED'
        else 'UNKNOWN'
      end
      into v_operation;
      select case
        when v_source = 'API' then 'API'
        when v_source = 'REPOSITORY' then 'GIT'
      end
      into v_origin;
      select case
        when v_content_type = 'user' then 'User'
        when v_content_type = 'site' then 'Site'
        when v_content_type = 'group' then 'Group'
        when v_content_type = 'folder' then 'Folder'
        when v_content_type = 'page' then 'Content Item'
        when v_content_type = 'component' then 'Content Item'
        when v_content_type = 'asset' then 'Content Item'
        when v_content_type = 'renderingTemplate' then 'Content Item'
        when v_content_type = 'document' then 'Content Item'
        when v_content_type = 'taxonomy' then 'Content Item'
        when v_content_type = 'configuration' then 'Content Item'
        when v_content_type = 'remoteRepository' then 'Remote Repository'
        else 'unknown'
      end
      into v_target_type;
      insert into new_audit (organization_id, site_id, operation, operation_timestamp, origin, primary_target_id, primary_target_type, primary_target_subtype, primary_target_value, actor_id, actor_details)
      values (1, v_site_id, v_operation, v_creation_date, v_origin, CONCAT(v_site_network,':',v_content_id), v_target_type, v_content_type, v_content_id, v_post_user_id, v_post_user_id);
    END LOOP get_audit_entry;
    CLOSE audit_cursor;
  END ;

call migrate_audit() ;

ALTER TABLE `audit` RENAME `audit_old` ;

ALTER TABLE `new_audit` RENAME `audit` ;

drop procedure migrate_audit ;

UPDATE _meta SET version = '3.1.0.19' ;