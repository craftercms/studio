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

call addColumnIfNotExists('crafter', 'workflow', 'publishing_package_id', 'VARCHAR(50)     NULL') ;

DROP PROCEDURE IF EXISTS migrateWorkflow ;

CREATE PROCEDURE migrateWorkflow(IN siteId VARCHAR(50))
BEGIN
    DECLARE v_site_id BIGINT;
    DECLARE v_item_id BIGINT;
    DECLARE v_path VARCHAR(2048);
    DECLARE v_renamed INT;
    DECLARE v_previous_path VARCHAR(2048);
    DECLARE v_state VARCHAR(255);
    DECLARE v_state_str VARCHAR(255);
    DECLARE v_submitter VARCHAR(255);
    DECLARE v_submitter_id BIGINT;
    DECLARE v_submitter_comment TEXT;
    DECLARE v_reviewer VARCHAR(255);
    DECLARE v_reviewer_id BIGINT;
    DECLARE v_reviewer_comment TEXT;
    DECLARE v_schedule TIMESTAMP;
    DECLARE v_notify_submitter INT;
    DECLARE v_last_modified_by BIGINT;
    DECLARE v_last_modified_on TIMESTAMP;
    DECLARE v_target_environment VARCHAR(255);
    DECLARE v_publishing_package_id VARCHAR(50);

    DECLARE v_finished INTEGER DEFAULT 0;
    DECLARE item_cursor CURSOR FOR
        SELECT ist.path as item_path, ist.state as item_state, im.renamed as item_renamed,
               im.oldurl as item_previous_path, im.submittedtoenvironment as item_target_environment,
               im.submittedby as item_submitter, im.submissioncomment as item_submitter_comment,
               im.launchdate as item_scheduled, im.sendemail as item_notify_submitter
        FROM item_state ist LEFT OUTER JOIN item_metadata im ON ist.site = im.site AND ist.path = im.path
        WHERE ist.site = siteId
        UNION
        SELECT ist.path as item_path, ist.state as item_state, im.renamed as item_renamed,
               im.oldurl as item_previous_path, im.submittedtoenvironment as item_target_environment,
               im.submittedby as item_submitter, im.submissioncomment as item_submitter_comment,
               im.launchdate as item_scheduled, im.sendemail as item_notify_submitter
        FROM item_state ist RIGHT OUTER JOIN item_metadata im ON ist.site = im.site AND ist.path = im.path
        WHERE ist.site = siteId;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET v_finished = 1;

    OPEN item_cursor;
    update_item: LOOP
        FETCH item_cursor INTO v_path, v_state, v_renamed, v_previous_path, v_target_environment, v_submitter,
            v_submitter_comment, v_schedule, v_notify_submitter;
        IF v_finished = 1 THEN LEAVE update_item;
        END IF;
        SELECT id INTO v_site_id FROM site WHERE site_id = siteId AND deleted = 0;
        SELECT id INTO v_item_id FROM item WHERE site_id = v_site_id AND path = v_path;
        CASE
            WHEN v_state = 'NEW_SUBMITTED_WITH_WF_SCHEDULED' THEN SELECT 'OPENED' INTO v_state_str;
            WHEN v_state = 'NEW_SUBMITTED_WITH_WF_SCHEDULED_LOCKED' THEN SELECT 'OPENED' INTO v_state_str;
            WHEN v_state = 'NEW_SUBMITTED_WITH_WF_UNSCHEDULED' THEN SELECT 'OPENED' INTO v_state_str;
            WHEN v_state = 'NEW_SUBMITTED_WITH_WF_UNSCHEDULED_LOCKED' THEN SELECT 'OPENED' INTO v_state_str;
            WHEN v_state = 'NEW_SUBMITTED_NO_WF_SCHEDULED' THEN SELECT 'APPROVED' INTO v_state_str;
            WHEN v_state = 'NEW_SUBMITTED_NO_WF_SCHEDULED_LOCKED' THEN SELECT 'APPROVED' INTO v_state_str;
            WHEN v_state = 'NEW_SUBMITTED_NO_WF_UNSCHEDULED' THEN SELECT 'APPROVED' INTO v_state_str;
            WHEN v_state = 'EXISTING_SUBMITTED_WITH_WF_SCHEDULED' THEN SELECT 'OPENED' INTO v_state_str;
            WHEN v_state = 'EXISTING_SUBMITTED_WITH_WF_SCHEDULED_LOCKED' THEN SELECT 'OPENED' INTO v_state_str;
            WHEN v_state = 'EXISTING_SUBMITTED_WITH_WF_UNSCHEDULED' THEN SELECT 'OPENED' INTO v_state_str;
            WHEN v_state = 'EXISTING_SUBMITTED_WITH_WF_UNSCHEDULED_LOCKED' THEN SELECT 'OPENED' INTO v_state_str;
            WHEN v_state = 'EXISTING_SUBMITTED_NO_WF_SCHEDULED' THEN SELECT 'APPROVED' INTO v_state_str;
            WHEN v_state = 'EXISTING_SUBMITTED_NO_WF_SCHEDULED_LOCKED' THEN SELECT 'APPROVED' INTO v_state_str;
            WHEN v_state = 'EXISTING_SUBMITTED_NO_WF_UNSCHEDULED' THEN SELECT 'APPROVED' INTO v_state_str;
            ELSE SEt v_state_str = NULL;
            END CASE;
        SELECT a.id INTO v_submitter_id FROM (SELECT id FROM user WHERE username = v_submitter UNION SELECT id FROM user WHERE username = v_submitter LIMIT 1) as a;
        SELECT a.id INTO v_reviewer_id FROM (SELECT id FROM user WHERE username = v_reviewer UNION SELECT id FROM user WHERE username = v_reviewer LIMIT 1) as a;
        SELECT submissioncomment, package_id INTO v_reviewer_comment, v_publishing_package_id
        FROM publish_request WHERE state = 'READY_FOR_LIVE' AND site = siteId and path = v_path;
        IF v_renamed > 0 THEN UPDATE item SET previous_path = v_previous_path WHERE id = v_item_id;
        END IF;
        IF v_state_str IS NOT NULL THEN
            INSERT INTO workflow (item_id, target_environment, state, submitter_id, submitter_comment, notify_submitter,
                                  schedule, reviewer_id, reviewer_comment, publishing_package_id)
            VALUES (v_item_id, v_target_environment, v_state_str, v_submitter_id, v_submitter_comment,
                    v_notify_submitter, v_schedule, v_reviewer_id, v_reviewer_comment, v_publishing_package_id);
        END IF;
        set v_finished = 0;
    end loop update_item;
    SELECT COUNT(1) FROM item WHERE site_id = v_site_id;
END ;


UPDATE _meta SET version = '4.0.0.8' ;
