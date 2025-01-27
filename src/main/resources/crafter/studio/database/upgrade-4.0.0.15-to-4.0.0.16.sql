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

call addColumnIfNotExists('crafter', 'site', 'publishing_status', 'VARCHAR(20) NULL') ;

UPDATE site SET publishing_status = TRIM(SUBSTRING_INDEX(publishing_status_message, '|', 1)) ;

UPDATE site SET publishing_status_message = TRIM(SUBSTRING_INDEX(publishing_status_message, '|', -1)) ;

UPDATE site SET publishing_status = 'ready' WHERE publishing_status = 'started' ;

UPDATE site SET publishing_status = 'publishing' WHERE publishing_status = 'busy' ;

UPDATE site SET publishing_status = 'error' WHERE publishing_status_message LIKE 'Stopped%' ;

UPDATE _meta SET version = '4.0.0.16' ;