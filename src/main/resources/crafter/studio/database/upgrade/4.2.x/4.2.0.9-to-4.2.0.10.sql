/*
 * Copyright (C) 2007-2024 Crafter Software Corporation. All Rights Reserved.
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

CREATE TABLE IF NOT EXISTS `processed_commits`
(
    `id`        BIGINT(20)      NOT NULL AUTO_INCREMENT,
    `site_id`   BIGINT(20)      NOT NULL,
    `commit_id` CHAR(40)        NOT NULL,
    PRIMARY KEY(`id`),
    UNIQUE INDEX `ingested_commits_commit_id_site_id` (`commit_id`, `site_id`),
    FOREIGN KEY `ingested_commits_site_id` (`site_id`) REFERENCES `site` (`id`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8
  ROW_FORMAT = DYNAMIC ;

/* Populate processed_commits table */
INSERT INTO processed_commits (id, site_id, commit_id)
SELECT null, s.id, s.last_commit_id
FROM site s
WHERE system = 0
AND deleted = 0 ;


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

        -- processed_commits
        DELETE FROM processed_commits WHERE site_id = id;
    END IF;
END ;

DROP PROCEDURE IF EXISTS duplicate_site ;

CREATE PROCEDURE duplicate_site(IN sourceSiteId VARCHAR(50),
                                    IN siteId VARCHAR(2000),
                                    IN name VARCHAR(2000),
                                    IN description VARCHAR(2000),
                                    IN sandboxBranch VARCHAR(2000),
                                    IN uuid VARCHAR(2000))
BEGIN
    INSERT INTO site (id, site_uuid, site_id, name, description, deleted, last_commit_id, system, publishing_enabled, publishing_status, sandbox_branch, published_repo_created, state)
        SELECT null, uuid, siteId, name, description, s.deleted, s.last_commit_id, s.system, 1, 'ready', IFNULL(NULLIF(sandboxBranch, ''), 'master'), s.published_repo_created, 'INITIALIZING' FROM site s WHERE s.site_id = sourceSiteId AND s.deleted = 0;

    INSERT INTO remote_repository (id, site_id, remote_name, remote_url, authentication_type, remote_username, remote_password, remote_token, remote_private_key)
        SELECT null, siteId, r.remote_name, r.remote_url, r.authentication_type, r.remote_username, r.remote_password, r.remote_token, r.remote_private_key FROM remote_repository r WHERE r.site_id = sourceSiteId;

    INSERT INTO dependency (id, site, source_path, target_path, type)
        SELECT null, siteId, d.source_path, d.target_path, d.type FROM dependency d WHERE d.site = sourceSiteId;

    INSERT INTO item (id, record_last_updated, site_id, path, preview_url, state, locked_by, created_by, created_on, last_modified_by, last_modified_on, last_published_on, label, content_type_id, system_type, mime_type, locale_code, translation_source_id, size, parent_id, previous_path, ignored)
        SELECT null, i.record_last_updated, (SELECT id FROM site WHERE site_id = siteId AND deleted = 0), i.path, i.preview_url, i.state, i.locked_by, i.created_by, i.created_on, i.last_modified_by, i.last_modified_on, i.last_published_on, i.label, i.content_type_id, i.system_type, i.mime_type, i.locale_code, i.translation_source_id, i.size, i.parent_id, i.previous_path, i.ignored FROM item i inner join site s ON i.site_id = s.id WHERE s.site_id = sourceSiteId;

    /* parent_id points to original item parent */
    SELECT id FROM site WHERE site_id = siteId AND deleted = 0 INTO @siteNumericId;
    CALL populateItemParentId(@siteNumericId);

    INSERT INTO navigation_order_sequence (folder_id, site, path, max_count)
        SELECT UUID(), siteId, nos.path, nos.max_count FROM navigation_order_sequence nos WHERE nos.site = sourceSiteId;

    SELECT id FROM site WHERE site_id = sourceSiteId AND deleted = 0 INTO @sourceSiteNumericId;

    INSERT INTO processed_commits (id, site_id, commit_id)
        SELECT null, @siteNumericId, pc.commit_id
        FROM processed_commits pc
        WHERE site_id = @sourceSiteNumericId;
END ;

UPDATE `_meta` SET `version` = '4.2.0.10' ;
