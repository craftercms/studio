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

//************************* DELETE TABLES START *************************//
DROP TABLE IF EXISTS `publish_request` ;
DROP TABLE IF EXISTS `workflow` ;
//************************** DELETE TABLES END **************************//

//************************* DELETE COLUMNS START *************************//
ALTER TABLE `item` DROP COLUMN `last_published_on` ;
ALTER TABLE `item` DROP COLUMN `previous_path` ;
ALTER TABLE `publish_package` DROP COLUMN `old_package_id` ;
//************************** DELETE COLUMNS END **************************//

//************************* DROP PROCEDURES START *************************//
DROP PROCEDURE IF EXISTS populateItemTarget
//************************** DROP PROCEDURES END **************************//
