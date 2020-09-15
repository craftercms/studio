/*
 * Copyright (C) 2007-2020 Crafter Software Corporation. All Rights Reserved.
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
DROP PROCEDURE populateItemTable ;

CREATE PROCEDURE populateItemTable(
    IN siteId VARCHAR(50))
BEGIN
    DECLARE v_site_id BIGINT;
    SELECT id INTO v_site_id FROM site WHERE site_id = siteId AND deleted = 0;
    DECLARE v_path VARCHAR(2048);
    DECLARE v_preview_url VARCHAR(2048);
    DECLARE v_state BIGINT;
    DECLARE v_owned_by BIGINT;
    DECLARE v_created_by BIGINT;
    DECLARE v_created_on TIMESTAMP;
    DECLARE v_last_modified_by BIGINT;
    DECLARE v_last_modified_on TIMESTAMP;
    DECLARE v_label VARCHAR(256);
    DECLARE v_content_type_id VARCHAR(256);
    DECLARE v_system_type VARCHAR(64);
    DECLARE v_mime_type VARCHAR(64);
    DECLARE v_disabled INT;
    DECLARE v_locale_code VARCHAR(16);
    DECLARE v_translation_source_id BIGINT;
    DECLARE v_size INT;
    DECLARE v_parent_id BIGINT;
    DECLARE v_ commit_id VARCHAR(128);
    DECLARE item_cursor CURSOR FOR SELECT * FROM item_state ist LEFT OUTER JOIN item_metadata im ON ist.site = im
        .site AND ist.path = im.path WHERE ist.site = siteId UNION SELECT * FROM item_state ist RIGHT OUTER JOIN
            item_metadata im ON ist.site = im.site AND ist.path = im.path WHERE ist.site = siteId;
    OPEN item_cursor;
    insert_item: LOOP
        FETCH item_cursor INTO v_group_id;
        IF v_finished = 1 THEN LEAVE update_group_name;
        END IF;
        UPDATE `group` LEFT JOIN `site` ON `group`.site_id = `site`.id SET `group`.name = TRIM(CONCAT(CONCAT(TRIM(`site`.name), '_'), TRIM(`group`.name))) WHERE `group`.id = v_group_id;
    end loop insert_item;
END ;

UPDATE _meta SET version = '3.2.0.8' ;