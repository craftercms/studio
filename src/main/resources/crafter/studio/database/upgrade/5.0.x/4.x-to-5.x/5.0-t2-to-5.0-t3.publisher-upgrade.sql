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

/*
	'SUBMITTED' (pending approval) packages are migrated from the 'OPENED' workflow records, which don't have a package id yet
	To create a 5.x publish_package, we group the items submitted by the same submitter_id on the submitted_on date as packages
 */
INSERT INTO publish_package
			(site_id, target, title, schedule, approval_state, package_state, live_error, staging_error, submitter_id,
			 submitter_comment, submitted_on, reviewer_id, reviewer_comment, reviewed_on, published_on, package_type, commit_id,
			 published_staging_commit_id, published_live_commit_id, old_package_id)
		SELECT i.site_id, MIN(w.target_environment), 'Migrated package', MIN(w.schedule), 'SUBMITTED',
				-- READY = 2⁰ = 1
				1 AS package_state, 0 AS live_error, 0 as staging_error, submitter_id, MIN(submitter_comment), w.submitted_on,
				NULL, NULL, NULL, NULL, 'ITEM_LIST', MIN(s.last_commit_id), NULL, NULL, CONCAT(w.submitted_on, '_', w.submitter_id)
		FROM workflow w
		INNER JOIN item i ON i.id = w.item_id
		INNER JOIN site s ON s.id = i.site_id
		WHERE w.publishing_package_id IS NULL
		AND w.state = 'OPENED'
		-- Group the items submitted by the same user on the same date as packages
		GROUP BY i.site_id, w.submitted_on, w.submitter_id ;

/************************* POPULATE  publish_item *************************/
INSERT INTO publish_item
			(package_id, path, live_previous_path, staging_previous_path,
			`action`, user_requested, publish_state, live_error, staging_error)
			SELECT pp.id AS package_id,
				pr.path, pr.oldpath AS live_previous_path, pr.oldpath AS staging_previous_path,
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
				END AS publish_state, 0 AS live_error, 0 AS staging_error
			FROM publish_request pr
			INNER JOIN site s ON s.site_id = pr.site
			INNER JOIN publish_package pp ON pp.old_package_id = pr.package_id AND pp.site_id = s.id
			WHERE s.deleted = 0 AND s.system = 0 ;

INSERT INTO publish_item
			(package_id, path, live_previous_path, staging_previous_path,
			`action`, user_requested, publish_state, live_error, staging_error)
			SELECT pp.id AS package_id, i.path, i.previous_path AS live_previous_path, i.previous_path AS staging_previous_path,
						-- state = 1 means NEW
						IF((i.state & 1 > 0), 'ADD', 'UPDATE') AS action, true AS user_requested,
						1 AS publish_state, -- PENDING = 2⁰ = 1
						0 AS live_error, 0 AS staging_error
			FROM workflow w
			INNER JOIN item i ON i.id = w.item_id
			INNER JOIN publish_package pp ON pp.old_package_id = CONCAT(w.submitted_on, '_', w.submitter_id)
										AND pp.site_id = i.site_id
			WHERE w.publishing_package_id IS NULL
			AND w.state = 'OPENED' ;

/************************* POPULATE  item_publish_item *************************/
INSERT INTO item_publish_item
			(publish_item_id, item_id)
			SELECT pi.id, i.id
			FROM publish_item pi
				INNER JOIN publish_package pp ON pp.id = pi.package_id
				INNER JOIN site s ON pp.site_id = s.id
				INNER JOIN item i ON i.path = pi.path AND i.site_id = s.id
			WHERE pi.publish_state = 1 AND pi.action <> 'DELETE' ; -- We only need the item_publish_item for the PENDING items that are not deleted
