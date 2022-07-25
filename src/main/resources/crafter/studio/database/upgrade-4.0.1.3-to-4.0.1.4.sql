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

-- Delete orphan audit_parameters records from deleted sites
DELETE FROM audit_parameters
WHERE audit_id NOT IN (SELECT id FROM audit) ;

-- Add missing FK audit_parameters -> audit
ALTER TABLE `audit_parameters`
ADD CONSTRAINT `audit_parameters_ix_audit_id`
	FOREIGN KEY IF NOT EXISTS (`audit_id`) REFERENCES `audit`(`id`)
ON DELETE CASCADE ;

UPDATE `_meta` SET `version` = '4.0.1.4' ;
