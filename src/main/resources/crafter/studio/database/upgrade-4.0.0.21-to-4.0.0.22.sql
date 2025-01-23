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

call addColumnIfNotExists('crafter', 'publish_request', 'label', 'VARCHAR(256) NULL') ;

call addColumnIfNotExists('crafter', 'publish_request', 'published_on', 'TIMESTAMP NULL') ;

ALTER TABLE `audit` MODIFY COLUMN `operation_timestamp` TIMESTAMP NOT NULL ;

ALTER TABLE `publish_request` MODIFY COLUMN `scheduleddate` TIMESTAMP NOT NULL ;

ALTER TABLE `site` MODIFY COLUMN `publishing_lock_heartbeat` TIMESTAMP NULL ;

ALTER TABLE `user` MODIFY COLUMN `record_last_updated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ;

ALTER TABLE `organization` MODIFY COLUMN `record_last_updated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ;

ALTER TABLE `organization_user` MODIFY COLUMN `record_last_updated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ;

ALTER TABLE `group` MODIFY COLUMN `record_last_updated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ;

ALTER TABLE `group_user` MODIFY COLUMN `record_last_updated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ;

ALTER TABLE `cluster` MODIFY COLUMN `heartbeat` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ;

ALTER TABLE `cluster_remote_repository` MODIFY COLUMN `record_last_updated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ;

UPDATE _meta SET version = '4.0.0.22' ;