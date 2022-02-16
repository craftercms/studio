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

DROP PROCEDURE IF EXISTS update_parent_id ;

CREATE PROCEDURE update_parent_id(IN siteId BIGINT, IN rootPath VARCHAR(2000))
BEGIN
    DECLARE v_parent_id BIGINT;
    DECLARE v_parent_path VARCHAR(2000);
    DECLARE v_parent_item_path VARCHAR(2000);
    DECLARE v_finished INTEGER DEFAULT 0;
    DECLARE parent_cursor CURSOR FOR SELECT i.id as parent_id, REPLACE(i.path, '/index.xml','') AS parent_path, i.path
                                                 AS parent_item_path FROM item i WHERE i.site_id = siteId AND (path = rootPath OR path LIKE concat(rootPath, '/%'));
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET v_finished = 1;
    OPEN parent_cursor;
    update_parent: LOOP
        FETCH parent_cursor INTO v_parent_id, v_parent_path, v_parent_item_path;
        IF v_finished = 1 THEN LEAVE update_parent;
        END IF;
        UPDATE item SET parent_id = v_parent_id WHERE site_id = siteId
                                                  AND path RLIKE (concat(v_parent_path, '/[^/]+/index\.xml|', v_parent_path,'/(?!index\.xml)[^/]+$'));
    END LOOP update_parent;
END ;

UPDATE _meta SET version = '4.0.0.19' ;