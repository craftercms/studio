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
ALTER TABLE `group` MODIFY `group_name` VARCHAR(512) NOT NULL ;

DROP PROCEDURE IF EXISTS populateItemTable ;

CREATE PROCEDURE populateItemTable(IN siteId VARCHAR(50))
BEGIN
    DECLARE v_site_id BIGINT;
    DECLARE v_path VARCHAR(2048);
    DECLARE v_state_str VARCHAR(255);
    DECLARE v_sys_process INT;
    DECLARE v_state BIGINT;
    DECLARE v_owner VARCHAR(255);
    DECLARE v_owned_by BIGINT;
    DECLARE v_creator VARCHAR(255);
    DECLARE v_created_by BIGINT;
    DECLARE v_created_on TIMESTAMP;
    DECLARE v_modifier VARCHAR(255);
    DECLARE v_last_modified_by BIGINT;
    DECLARE v_last_modified_on TIMESTAMP;
    DECLARE v_commit_id VARCHAR(128);
    DECLARE v_finished INTEGER DEFAULT 0;
    DECLARE item_cursor CURSOR FOR
        SELECT im.path as item_path, ist.state as item_state, ist.system_processing as item_sys_process,
               im.owner as item_owner, im.creator as item_creator, im.modifier as item_modifier,
               im.modified as item_modified, im.commit_id as item_commit_id
        FROM item_state ist LEFT OUTER JOIN item_metadata im ON ist.site = im.site AND ist.path = im.path
        WHERE ist.site = siteId
        UNION
        SELECT im.path as item_path, ist.state as item_state, ist.system_processing as item_sys_process,
               im.owner as item_owner, im.creator as item_creator, im.modifier as item_modifier,
               im.modified as item_modified, im.commit_id as item_commit_id
        FROM item_state ist RIGHT OUTER JOIN item_metadata im ON ist.site = im.site AND ist.path = im.path
        WHERE ist.site = siteId;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET v_finished = 1;
    SELECT id INTO v_site_id FROM site WHERE site_id = siteId AND deleted = 0;
    DELETE FROM item WHERE site_id = v_site_id;
    OPEN item_cursor;
    insert_item: LOOP
        FETCH item_cursor INTO v_path, v_state_str, v_sys_process, v_owner, v_creator, v_modifier, v_last_modified_on, v_commit_id;
        IF v_finished = 1 THEN LEAVE insert_item;
        END IF;
        CASE
            WHEN v_state_str = 'NEW_UNPUBLISHED_LOCKED' THEN SELECT 11 + 16 * v_sys_process INTO v_state;
            WHEN v_state_str = 'NEW_UNPUBLISHED_UNLOCKED' THEN SELECT 3 + 16 * v_sys_process INTO v_state;
            WHEN v_state_str = 'NEW_SUBMITTED_WITH_WF_SCHEDULED' THEN SELECT 99 + 16 * v_sys_process INTO v_state;
            WHEN v_state_str = 'NEW_SUBMITTED_WITH_WF_SCHEDULED_LOCKED' THEN SELECT 107 + 16 * v_sys_process INTO v_state;
            WHEN v_state_str = 'NEW_SUBMITTED_WITH_WF_UNSCHEDULED' THEN SELECT 35 + 16 * v_sys_process INTO v_state;
            WHEN v_state_str = 'NEW_SUBMITTED_WITH_WF_UNSCHEDULED_LOCKED' THEN SELECT 43 + 16 * v_sys_process INTO v_state;
            WHEN v_state_str = 'NEW_SUBMITTED_NO_WF_SCHEDULED' THEN SELECT 67 + 16 * v_sys_process INTO v_state;
            WHEN v_state_str = 'NEW_SUBMITTED_NO_WF_SCHEDULED_LOCKED' THEN SELECT 75 + 16 * v_sys_process INTO v_state;
            WHEN v_state_str = 'NEW_SUBMITTED_NO_WF_UNSCHEDULED' THEN SELECT 3 + 16 * v_sys_process INTO v_state;
            WHEN v_state_str = 'NEW_PUBLISHING_FAILED' THEN SELECT 131 + 16 * v_sys_process INTO v_state;
            WHEN v_state_str = 'NEW_DELETED' THEN SELECT 5 + 16 * v_sys_process INTO v_state;
            WHEN v_state_str = 'EXISTING_UNEDITED_LOCKED' THEN SELECT 520 + 16 * v_sys_process INTO v_state;
            WHEN v_state_str = 'EXISTING_UNEDITED_UNLOCKED' THEN SELECT 512 + 16 * v_sys_process INTO v_state;
            WHEN v_state_str = 'EXISTING_EDITED_LOCKED' THEN SELECT 10 + 16 * v_sys_process INTO v_state;
            WHEN v_state_str = 'EXISTING_EDITED_UNLOCKED' THEN SELECT 2 + 16 * v_sys_process INTO v_state;
            WHEN v_state_str = 'EXISTING_SUBMITTED_WITH_WF_SCHEDULED' THEN SELECT 98 + 16 * v_sys_process INTO v_state;
            WHEN v_state_str = 'EXISTING_SUBMITTED_WITH_WF_SCHEDULED_LOCKED' THEN SELECT 106 + 16 * v_sys_process INTO v_state;
            WHEN v_state_str = 'EXISTING_SUBMITTED_WITH_WF_UNSCHEDULED' THEN SELECT 34 + 16 * v_sys_process INTO v_state;
            WHEN v_state_str = 'EXISTING_SUBMITTED_WITH_WF_UNSCHEDULED_LOCKED' THEN SELECT 42 + 16 * v_sys_process INTO v_state;
            WHEN v_state_str = 'EXISTING_SUBMITTED_NO_WF_SCHEDULED' THEN SELECT 66 + 16 * v_sys_process INTO v_state;
            WHEN v_state_str = 'EXISTING_SUBMITTED_NO_WF_SCHEDULED_LOCKED' THEN SELECT 74 + 16 * v_sys_process INTO v_state;
            WHEN v_state_str = 'EXISTING_SUBMITTED_NO_WF_UNSCHEDULED' THEN SELECT 2 + 16 * v_sys_process INTO v_state;
            WHEN v_state_str = 'EXISTING_PUBLISHING_FAILED' THEN SELECT 130 + 16 * v_sys_process INTO v_state;
            WHEN v_state_str = 'EXISTING_DELETED' THEN SELECT 4 + 16 * v_sys_process INTO v_state;
            ELSE SELECT 0 INTO v_state;
            END CASE;
        SELECT a.id INTO v_owned_by FROM (SELECT id FROM user WHERE username = v_owner UNION SELECT id from user WHERE username = v_owner LIMIT 1) as a;
        SELECT a.id INTO v_created_by FROM (SELECT id FROM user WHERE username = v_creator UNION SELECT id from user WHERE username = v_creator LIMIT 1) as a;
        SELECT a.id INTO v_last_modified_by FROM (SELECT id FROM user WHERE username = v_modifier UNION SELECT id from user WHERE username = v_modifier LIMIT 1) as a;
        INSERT INTO item (site_id, path, preview_url, state, owned_by, created_by, created_on, last_modified_by, last_modified_on, commit_id)
        VALUES (v_site_id, v_path, v_path, v_state, v_owned_by, v_created_by, v_created_on, v_last_modified_by, v_last_modified_on, v_commit_id);
    end loop insert_item;
    SELECT COUNT(1) FROM item WHERE site_id = v_site_id;
END ;

UPDATE _meta SET version = '3.2.0.11' ;