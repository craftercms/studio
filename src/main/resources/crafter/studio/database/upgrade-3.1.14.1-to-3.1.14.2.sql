/*
 * Copyright (C) 2007-2021 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

DROP PROCEDURE tryLockPublishingForSite ;

CREATE PROCEDURE tryLockPublishingForSite(
    IN siteId VARCHAR(50),
    IN lockOwnerId VARCHAR(255),
    IN ttl INT,
    OUT locked INT)
BEGIN
    DECLARE v_lock_owner_id VARCHAR(255);
    DECLARE v_lock_heartbeat TIMESTAMP;
    SELECT publishing_lock_owner, publishing_lock_heartbeat INTO  v_lock_owner_id, v_lock_heartbeat FROM site
    WHERE site_id = siteId and deleted = 0;
    SET locked = 0;
    IF (v_lock_owner_id IS NULL OR v_lock_owner_id = '' OR v_lock_owner_id = lockOwnerId OR DATE_ADD(v_lock_heartbeat, INTERVAL ttl MINUTE) < CURRENT_TIMESTAMP)
    THEN
        UPDATE site SET publishing_lock_owner = lockOwnerId, publishing_lock_heartbeat = CURRENT_TIMESTAMP WHERE site_id = siteId and deleted = 0;
        SET locked = 1;
    END IF;
    SELECT locked;
END ;

ALTER TABLE `audit` MODIFY COLUMN `operation_timestamp` TIMESTAMP NOT NULL ;

ALTER TABLE `publish_request` MODIFY COLUMN `scheduleddate` TIMESTAMP NOT NULL ;

ALTER TABLE `site` MODIFY COLUMN `publishing_lock_heartbeat` TIMESTAMP NULL ;

ALTER TABLE `item_metadata` MODIFY COLUMN `modified` TIMESTAMP NULL ;

ALTER TABLE `item_metadata` MODIFY COLUMN `launchdate` TIMESTAMP NULL ;

ALTER TABLE `item_metadata` MODIFY COLUMN `published_date` TIMESTAMP NULL ;

ALTER TABLE `user` MODIFY COLUMN `record_last_updated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ;

ALTER TABLE `organization` MODIFY COLUMN `record_last_updated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ;

ALTER TABLE `organization_user` MODIFY COLUMN `record_last_updated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ;

ALTER TABLE `group` MODIFY COLUMN `record_last_updated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ;

ALTER TABLE `group_user` MODIFY COLUMN `record_last_updated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ;

ALTER TABLE `cluster` MODIFY COLUMN `heartbeat` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ;

ALTER TABLE `cluster_remote_repository` MODIFY COLUMN `record_last_updated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ;

UPDATE _meta SET version = '3.1.14.2' ;