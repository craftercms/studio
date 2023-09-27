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

DROP PROCEDURE IF EXISTS populateItemParentId ;
CREATE PROCEDURE populateItemParentId(IN siteId BIGINT)
BEGIN
    UPDATE item,
        (SELECT id, max(potential_parent_path) as calculated_parent_path,
						(SELECT p.id FROM item p WHERE (p.path = max(potential_parent_path)) and p.site_id = siteId) AS calculated_parent_id
        FROM
            (SELECT candidates.id, candidates.path, candidates.parent_id,
                    (SELECT p.id FROM item p WHERE (p.path = candidates.parent_path) AND p.site_id = siteId) AS potential_parent_id,
                    candidates.parent_path as potential_parent_path
            FROM (
					SELECT id, parent_id, path,
							reverse(substr(reverse(trim('/index.xml' from path)), locate('/', reverse(trim('/index.xml' from path)))+1)) AS parent_path
					FROM item
					WHERE site_id = siteId
				UNION
					SELECT id, parent_id, path,
							concat(reverse(substr(reverse(trim('/index.xml' from path)), locate('/', reverse(trim('/index.xml' from path)))+1)), '/index.xml') AS parent_path
					FROM item
					WHERE site_id = siteId
				) AS candidates
			) AS mapped
        WHERE potential_parent_id IS NOT NULL
        GROUP BY id
        ) AS updates
    SET item.parent_id = updates.calculated_parent_id
    WHERE item.id = updates.id;
END ;

DROP PROCEDURE IF EXISTS duplicate_site ;
CREATE PROCEDURE duplicate_site(IN sourceSiteId VARCHAR(50),
                                    IN siteId VARCHAR(2000),
                                    IN name VARCHAR(2000),
                                    IN description VARCHAR(2000),
                                    IN sandboxBranch VARCHAR(2000),
                                    IN uuid VARCHAR(2000))
BEGIN
    INSERT INTO site (id, site_uuid, site_id, name, description, deleted, last_commit_id, system, publishing_enabled, publishing_status, last_verified_gitlog_commit_id, sandbox_branch, published_repo_created, publishing_lock_owner, publishing_lock_heartbeat, state, last_synced_gitlog_commit_id)
        SELECT null, uuid, siteId, name, description, s.deleted, s.last_commit_id, s.system, s.publishing_enabled, s.publishing_status, s.last_verified_gitlog_commit_id, sandboxBranch, s.published_repo_created, s.publishing_lock_owner, s.publishing_lock_heartbeat, 'INITIALIZING', s.last_synced_gitlog_commit_id FROM site s WHERE s.site_id = sourceSiteId;

    INSERT INTO dependency (id, site, source_path, target_path, type)
        SELECT null, siteId, d.source_path, d.target_path, d.type FROM dependency d WHERE d.site = sourceSiteId;

    INSERT INTO gitlog (id, site_id, commit_id, processed, audited)
        SELECT null, siteId, gl.commit_id, gl.processed, gl.audited FROM gitlog gl WHERE gl.site_id = sourceSiteId;

    INSERT INTO item (id, record_last_updated, site_id, path, preview_url, state, locked_by, created_by, created_on, last_modified_by, last_modified_on, last_published_on, label, content_type_id, system_type, mime_type, locale_code, translation_source_id, size, parent_id, commit_id, previous_path, ignored)
        SELECT null, i.record_last_updated, (SELECT id FROM site WHERE site_id = siteId AND deleted = 0), i.path, i.preview_url, i.state, i.locked_by, i.created_by, i.created_on, i.last_modified_by, i.last_modified_on, i.last_published_on, i.label, i.content_type_id, i.system_type, i.mime_type, i.locale_code, i.translation_source_id, i.size, i.parent_id, i.commit_id, i.previous_path, i.ignored FROM item i inner join site s ON i.site_id = s.id WHERE s.site_id = sourceSiteId;

    /* parent_id points to original item parent */
    SELECT id FROM site WHERE site_id = siteId AND deleted = 0 INTO @siteNumericId;
    CALL populateItemParentId(@siteNumericId);

    INSERT INTO navigation_order_sequence (folder_id, site, path, max_count)
        SELECT UUID(), siteId, nos.path, nos.max_count FROM navigation_order_sequence nos WHERE nos.site = sourceSiteId;
END ;

UPDATE `_meta` SET `version` = '4.1.7' ;
