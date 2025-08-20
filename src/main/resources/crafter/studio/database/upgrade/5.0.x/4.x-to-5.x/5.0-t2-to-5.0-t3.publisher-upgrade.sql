/*
 * Copyright (C) 2007-2025 Crafter Software Corporation. All Rights Reserved.
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
/************************* POPULATE  publish_package *************************/
INSERT INTO publish_package
			(site_id, target, title, schedule, approval_state, package_state, live_error, staging_error, submitter_id,
			 submitter_comment, submitted_on, reviewer_id, reviewer_comment, reviewed_on, published_on, package_type, commit_id,
			 published_staging_commit_id, published_live_commit_id, old_package_id)

		SELECT s.id, pr.environment, 'Migrated package', pr.scheduleddate, IF(w.state = 'OPENED', 'SUBMITTED', IF(pr.state = 'CANCELLED', 'REJECTED', 'APPROVED')) AS approval_state,
				CASE pr.state
					WHEN 'READY_FOR_LIVE' THEN POWER(2, 0) -- READY
					WHEN 'PROCESSING' THEN POWER(2, 0) -- READY
					WHEN 'COMPLETED' THEN POWER(2, 8) + IF(pr.environment = 'live', POWER(2, 2) + POWER(2, 5), POWER(2, 5)) -- COMPLETED + LIVE_SUCCESS + STAGING_SUCCESS
					ELSE POWER(2, 9) -- Anything else (cancelled, blocked, processing) is CANCELLED
				END AS package_state,
				0 AS live_error, 0 as staging_error, IFNULL(w.submitter_id, (SELECT u.id FROM user u WHERE u.username = pr.username)) AS submitter_id,
				pr.submissioncomment, w.submitted_on, w.reviewer_id, w.reviewer_comment, NULL,
				pr.published_on, 'ITEM_LIST', NULL, NULL, NULL, pr.package_id
		FROM publish_request pr
			INNER JOIN site s ON s.site_id = pr.site
			LEFT JOIN workflow w ON w.publishing_package_id = pr.package_id
		GROUP BY pr.package_id ;

/************************* POPULATE  publish_item *************************/
INSERT INTO publish_item
			(package_id, path, live_previous_path, staging_previous_path,
			`action`, user_requested, publish_state, live_error, staging_error)
			SELECT
				(SELECT pp.id
					FROM publish_package pp
					WHERE pp.old_package_id = pr.package_id) AS package_id,
				pr.path, pr.oldpath, pr.oldpath,
				CASE pr.action
					WHEN 'NEW' THEN 'ADD'
					WHEN 'DELETE' THEN 'DELETE'
					ELSE 'UPDATE'
				END AS action, true AS user_requested,
				CASE pr.state
					WHEN 'COMPLETED' THEN IF(pr.environment = 'live', POWER(2, 2) + POWER(2, 4), POWER(2, 4)) -- if live, live_success+staging_success, otherwise staging_success
					ELSE POWER(2, 0) -- PENDING, default value
				END AS publish_state, 0, 0
			FROM publish_request pr ;

/************************* POPULATE  item_publish_item *************************/
INSERT INTO item_publish_item
			(publish_item_id, item_id)
			SELECT pi.id, i.id
			FROM publish_item pi
				INNER JOIN publish_package pp ON pp.id = pi.package_id
				INNER JOIN site s ON pp.site_id = s.id
				INNER JOIN item i ON i.path = pi.path AND i.site_id = s.id
			WHERE pi.publish_state = 1 AND pi.action <> 'DELETE' ;
