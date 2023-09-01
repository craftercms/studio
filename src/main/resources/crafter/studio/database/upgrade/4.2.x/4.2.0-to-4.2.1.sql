/*
 * Copyright (C) 2007-2023 Crafter Software Corporation. All Rights Reserved.
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

ALTER TABLE `site`
    DROP COLUMN `last_verified_gitlog_commit_id`,
    DROP COLUMN `last_synced_gitlog_commit_id` ;

ALTER TABLE `item`
    DROP COLUMN `commit_id` ;

-- Remove gitlog table reference from this procedure
DROP PROCEDURE IF EXISTS deleteSiteRelatedItems ;

CREATE PROCEDURE deleteSiteRelatedItems(
    IN siteId VARCHAR(50))
BEGIN
	DECLARE id BIGINT(20);

    IF EXISTS (SELECT (1) FROM site WHERE site_id = siteId AND deleted = 0)
    THEN
		SELECT s.id into id
		FROM site s
		WHERE site_id = siteId AND deleted = 0;

		-- Item will cascade delete workflow
        DELETE FROM item WHERE site_id = id;

        -- user_properties
        DELETE FROM user_properties WHERE site_id = id;

        -- dependencies
        DELETE FROM dependency WHERE site = siteId;

        -- deployment data
        DELETE FROM publish_request WHERE site = siteId;

        -- sequences
        DELETE FROM navigation_order_sequence WHERE site = siteId;

        -- remote repositories
        DELETE FROM remote_repository WHERE site_id = siteId;

        -- audit log
        DELETE FROM audit WHERE site_id = id;
    END IF;
END ;

DROP TABLE IF EXISTS `gitlog` ;

UPDATE `_meta` SET `version` = '4.2.1' ;

