/*
 * Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
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

ALTER TABLE `site` ADD COLUMN `sync_repo_lock_owner` VARCHAR(255) NULL ;

ALTER TABLE `site` ADD COLUMN `sync_repo_lock_heartbeat` DATETIME NULL ;

CREATE TABLE IF NOT EXISTS cluster_site_sync_repo
(
    `cluster_node_id`                 BIGINT(20)    NOT NULL,
    `site_id`                         BIGINT(20)    NOT NULL,
    `node_last_commit_id`                  VARCHAR(50)   NULL,
    `node_last_verified_gitlog_commit_id`  VARCHAR(50)   NULL,
    PRIMARY KEY (`cluster_node_id`, `site_id`),
    FOREIGN KEY cluster_site_ix_cluster_id(`cluster_node_id`) REFERENCES `cluster` (`id`)
        ON DELETE CASCADE,
    FOREIGN KEY cluster_site_ix_remote_id(`site_id`) REFERENCES `site` (`id`)
        ON DELETE CASCADE
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8
    ROW_FORMAT = DYNAMIC ;

CREATE PROCEDURE tryLockSyncRepoForSite(
    IN siteId VARCHAR(50),
    IN lockOwnerId VARCHAR(255),
    IN ttl INT,
    OUT locked INT)
BEGIN
    DECLARE v_lock_owner_id VARCHAR(255);
    DECLARE v_lock_heartbeat DATETIME;
    SELECT sync_repo_lock_owner, sync_repo_lock_heartbeat INTO  v_lock_owner_id, v_lock_heartbeat FROM site
    WHERE site_id = siteId AND deleted = 0;
    SET locked = 0;
    IF (v_lock_owner_id IS NULL OR v_lock_owner_id = '' OR v_lock_owner_id = lockOwnerId OR DATE_ADD(v_lock_heartbeat, INTERVAL ttl MINUTE) < CURRENT_TIMESTAMP)
    THEN
        UPDATE site SET sync_repo_lock_owner = lockOwnerId, sync_repo_lock_heartbeat = CURRENT_TIMESTAMP WHERE site_id = siteId and deleted = 0;
        SET locked = 1;
    END IF;
    SELECT locked;
END ;

UPDATE _meta SET version = '3.2.0.13' ;

UPDATE _meta SET version = '3.1.10.11' ;