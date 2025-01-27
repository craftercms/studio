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

ALTER TABLE `item` DROP FOREIGN KEY IF EXISTS `item_ix_owned_by` ;

ALTER TABLE `item` CHANGE COLUMN IF EXISTS `owned_by` `locked_by` BIGINT NULL ;

ALTER TABLE `item` ADD FOREIGN KEY IF NOT EXISTS item_ix_locked_by(`locked_by`) REFERENCES `user` (`id`) ;

UPDATE _meta SET version = '4.0.0.34' ;
