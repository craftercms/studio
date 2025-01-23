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

call addColumnIfNotExists('crafter', 'site', 'state', 'VARCHAR(50) NOT NULL DEFAULT ''CREATING''') ;

UPDATE site SET state = 'CREATED' WHERE deleted = 0 ;

UPDATE site SET state = 'DELETED' WHERE deleted = 1 ;

call addColumnIfNotExists('crafter', 'cluster_site_sync_repo', 'site_state', 'VARCHAR(50) NOT NULL DEFAULT ''CREATING''') ;

call addColumnIfNotExists('crafter', 'cluster_site_sync_repo', 'site_published_repo_created', 'INT NOT NULL DEFAULT 0') ;

UPDATE cluster_site_sync_repo cssr INNER JOIN site s ON cssr.site_id = s.id SET cssr.site_state = 'CREATED' WHERE s.deleted = 0 ;

UPDATE cluster_site_sync_repo cssr INNER JOIN site s ON cssr.site_id = s.id SET cssr.site_state = 'DELETED' WHERE s.deleted = 1 ;

UPDATE cluster_site_sync_repo cssr INNER JOIN site s ON cssr.site_id = s.id SET cssr.site_published_repo_created = s.published_repo_created ;

UPDATE _meta SET version = '3.2.0.16' ;
