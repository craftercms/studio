/*
 * Copyright (C) 2007-2026 Crafter Software Corporation. All Rights Reserved.
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
/*
* This update rewrites the populateItemParentId procedure to prevent
* mariadb issues with subqueries (resolving column not found when referencing an alias from an outer query)
*/
DROP PROCEDURE IF EXISTS populateItemParentId ;

CREATE PROCEDURE populateItemParentId(IN siteId BIGINT)
BEGIN
    UPDATE item,
        (SELECT id, max(potential_parent_path) as calculated_parent_path,
						(SELECT p.id FROM item p WHERE (p.path = max(potential_parent_path)) and p.site_id = siteId) AS calculated_parent_id
        FROM
            (SELECT candidates.id, candidates.path, candidates.parent_id, i.id as potential_parent_id,
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
				) AS candidates INNER JOIN item i ON candidates.parent_path = i.path AND i.site_id = siteId
				WHERE i.site_id = siteId
			) AS mapped
        GROUP BY id
        ) AS updates
    SET item.parent_id = updates.calculated_parent_id
    WHERE item.id = updates.id;
END ;
