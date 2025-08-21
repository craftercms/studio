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

		SELECT MIN(s.id), MIN(pr.environment), 'Migrated package', MIN(pr.scheduleddate), IF(MIN(w.state) = 'OPENED', 'SUBMITTED',
				IF(MIN(pr.state) = 'CANCELLED', 'REJECTED', 'APPROVED')) AS approval_state,
				CASE MIN(pr.state)
					WHEN 'READY_FOR_LIVE' THEN 1 -- READY = 2⁰
					WHEN 'PROCESSING' THEN 1 -- READY = 2⁰
					-- COMPLETED = 2⁸ = 256
					-- LIVE_SUCCESS + STAGING_SUCCESS = 2² + 2⁵ = 4 + 32 = 36
					-- STAGING_SUCCESS = 2⁵ = 32
					WHEN 'COMPLETED' THEN 256 + IF(MIN(pr.environment)= 'live', 36, 32)
					ELSE 512 -- Anything else (cancelled, blocked, processing) is CANCELLED = 2⁹ = 512
				END AS package_state,
				0 AS live_error, 0 as staging_error, IFNULL(MIN(w.submitter_id), MIN(u.id)) AS submitter_id,
				MIN(pr.submissioncomment), MIN(w.submitted_on), MIN(w.reviewer_id), MIN(w.reviewer_comment), NULL,
				MIN(pr.published_on), 'ITEM_LIST', NULL, NULL, NULL, MIN(pr.package_id)
		FROM publish_request pr
			INNER JOIN site s ON s.site_id = pr.site
			LEFT JOIN workflow w ON w.publishing_package_id = pr.package_id
			LEFT JOIN user u ON u.username = pr.username
		GROUP BY pr.package_id ;

/************************* POPULATE  publish_item *************************/
INSERT INTO publish_item
			(package_id, path, live_previous_path, staging_previous_path,
			`action`, user_requested, publish_state, live_error, staging_error)
			SELECT pp.id AS package_id,
				pr.path, pr.oldpath, pr.oldpath,
				CASE pr.action
					WHEN 'NEW' THEN 'ADD'
					WHEN 'DELETE' THEN 'DELETE'
					ELSE 'UPDATE'
				END AS action, true AS user_requested,
				CASE pr.state
					-- LIVE_SUCCESS = 2² = 4
					-- STAGING_SUCCESS = 2⁴ = 16
					WHEN 'COMPLETED' THEN IF(pr.environment = 'live', 20, 16) -- if live, LIVE_SUCCESS + STAGING_SUCCESS, otherwise STAGING_SUCCESS
					ELSE 1 -- PENDING = 2⁰ = 1, default value
				END AS publish_state, 0, 0
			FROM publish_request pr
			INNER JOIN publish_package pp ON pp.old_package_id = pr.package_id ;

/************************* POPULATE  item_publish_item *************************/
INSERT INTO item_publish_item
			(publish_item_id, item_id)
			SELECT pi.id, i.id
			FROM publish_item pi
				INNER JOIN publish_package pp ON pp.id = pi.package_id
				INNER JOIN site s ON pp.site_id = s.id
				INNER JOIN item i ON i.path = pi.path AND i.site_id = s.id
			WHERE pi.publish_state = 1 AND pi.action <> 'DELETE' ; -- We only need the item_publish_item for the PENDING items that are not deleted
