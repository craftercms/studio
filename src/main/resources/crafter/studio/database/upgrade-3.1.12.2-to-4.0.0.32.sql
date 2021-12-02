ALTER TABLE `audit` 
CHANGE COLUMN `operation_timestamp` `operation_timestamp` TIMESTAMP NOT NULL ;

ALTER TABLE `publish_request`
CHANGE COLUMN `scheduleddate` `scheduleddate` TIMESTAMP NOT NULL ;

ALTER TABLE `site`
ADD COLUMN `publishing_status` VARCHAR(20) NULL DEFAULT NULL AFTER `publishing_enabled`,
CHANGE COLUMN `publishing_lock_heartbeat` `publishing_lock_heartbeat` TIMESTAMP NULL DEFAULT NULL ,
CHANGE COLUMN `state` `state` VARCHAR(50) NOT NULL DEFAULT 'INITIALIZING' ;

UPDATE site SET publishing_status = TRIM(SUBSTRING_INDEX(publishing_status_message, '|', 1)) ;

UPDATE site SET publishing_status_message = TRIM(SUBSTRING_INDEX(publishing_status_message, '|', -1)) ;

UPDATE site SET publishing_status = 'ready' WHERE publishing_status = 'started' ;

UPDATE site SET publishing_status = 'publishing' WHERE publishing_status = 'busy' ;

UPDATE site SET publishing_status = 'error' WHERE publishing_status_message LIKE 'Stopped%' ;

ALTER TABLE `user` 
CHANGE COLUMN `record_last_updated` `record_last_updated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ;

ALTER TABLE `organization` 
CHANGE COLUMN `record_last_updated` `record_last_updated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ;

ALTER TABLE `organization_user` 
CHANGE COLUMN `record_last_updated` `record_last_updated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ;

ALTER TABLE `group` 
CHANGE COLUMN `record_last_updated` `record_last_updated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ;

ALTER TABLE `group_user` 
CHANGE COLUMN `record_last_updated` `record_last_updated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ;

ALTER TABLE `cluster` 
CHANGE COLUMN `heartbeat` `heartbeat` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ;

ALTER TABLE `cluster_remote_repository` 
CHANGE COLUMN `record_last_updated` `record_last_updated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ;
