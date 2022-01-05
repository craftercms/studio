ALTER TABLE `audit`
ADD COLUMN `commit_id` VARCHAR(50) NULL DEFAULT NULL AFTER `cluster_node_id`,
CHANGE COLUMN `operation_timestamp` `operation_timestamp` TIMESTAMP NOT NULL ;

ALTER TABLE `publish_request`
CHANGE COLUMN `scheduleddate` `scheduleddate` TIMESTAMP NOT NULL ;

ALTER TABLE `site`
ADD COLUMN `publishing_status` VARCHAR(20) NULL DEFAULT NULL AFTER `publishing_enabled`,
ADD COLUMN `publishing_lock_owner` VARCHAR(255) NULL DEFAULT NULL AFTER `published_repo_created`,
ADD COLUMN `publishing_lock_heartbeat` TIMESTAMP NULL DEFAULT NULL AFTER `publishing_lock_owner`,
ADD COLUMN `state` VARCHAR(50) NOT NULL DEFAULT 'INITIALIZING' AFTER `publishing_lock_heartbeat`,
ADD COLUMN `last_synced_gitlog_commit_id` VARCHAR(50) NULL DEFAULT NULL AFTER `state` ;

UPDATE site SET publishing_status = TRIM(SUBSTRING_INDEX(publishing_status_message, '|', 1)) ;

UPDATE site SET publishing_status_message = TRIM(SUBSTRING_INDEX(publishing_status_message, '|', -1)) ;

UPDATE site SET publishing_status = 'ready' WHERE publishing_status = 'started' ;

UPDATE site SET publishing_status = 'publishing' WHERE publishing_status = 'busy' ;

UPDATE site SET publishing_status = 'error' WHERE publishing_status_message LIKE 'Stopped%' ;

UPDATE site SET last_synced_gitlog_commit_id = last_commit_id ;

ALTER TABLE `user` 
CHANGE COLUMN `record_last_updated` `record_last_updated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ;

ALTER TABLE `organization` 
CHANGE COLUMN `record_last_updated` `record_last_updated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ;

ALTER TABLE `organization_user` 
CHANGE COLUMN `record_last_updated` `record_last_updated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ;

ALTER TABLE `group` 
CHANGE COLUMN `record_last_updated` `record_last_updated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ,
CHANGE COLUMN `group_name` `group_name` VARCHAR(512) NOT NULL ;

ALTER TABLE `group_user` 
CHANGE COLUMN `record_last_updated` `record_last_updated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ;

ALTER TABLE `gitlog`
ADD COLUMN `audited` INT(11) NOT NULL DEFAULT 0 AFTER `processed` ;

UPDATE gitlog SET audited = 1 ;

ALTER TABLE `cluster` 
ADD COLUMN `available` INT(11) NOT NULL DEFAULT 1 AFTER `heartbeat`,
CHANGE COLUMN `heartbeat` `heartbeat` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ;

CREATE TABLE IF NOT EXISTS `cluster_remote_repository` (
  `cluster_id` BIGINT(20) NOT NULL,
  `remote_repository_id` BIGINT(20) NOT NULL,
  `record_last_updated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`cluster_id`, `remote_repository_id`),
  INDEX `cluster_remote_ix_remote_id` (`remote_repository_id` ASC),
  CONSTRAINT `cluster_remote_ix_cluster_id`
    FOREIGN KEY (`cluster_id`)
    REFERENCES `cluster` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `cluster_remote_ix_remote_id`
    FOREIGN KEY (`remote_repository_id`)
    REFERENCES `remote_repository` (`id`)
    ON DELETE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
ROW_FORMAT = DYNAMIC ;

CREATE TABLE IF NOT EXISTS `cluster_site_sync_repo` (
  `cluster_node_id` BIGINT(20) NOT NULL,
  `site_id` BIGINT(20) NOT NULL,
  `node_last_commit_id` VARCHAR(50) NULL DEFAULT NULL,
  `node_last_verified_gitlog_commit_id` VARCHAR(50) NULL DEFAULT NULL,
  `node_last_synced_gitlog_commit_id` VARCHAR(50) NULL DEFAULT NULL,
  `site_state` VARCHAR(50) NOT NULL DEFAULT 'CREATING',
  `site_published_repo_created` INT(11) NOT NULL DEFAULT 0,
  PRIMARY KEY (`cluster_node_id`, `site_id`),
  INDEX `cluster_site_ix_remote_id` (`site_id` ASC),
  CONSTRAINT `cluster_site_ix_cluster_id`
    FOREIGN KEY (`cluster_node_id`)
    REFERENCES `cluster` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `cluster_site_ix_remote_id`
    FOREIGN KEY (`site_id`)
    REFERENCES `site` (`id`)
    ON DELETE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
ROW_FORMAT = DYNAMIC ;
 
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
