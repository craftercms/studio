call addColumnIfNotExists('crafter', 'site', 'state', 'VARCHAR(50) NOT NULL DEFAULT ''CREATING''') ;

UPDATE `site` SET `state` = 'CREATED' WHERE `deleted` = 0 ;

UPDATE `site` SET `state` = 'DELETED' WHERE `deleted` = 1 ;

call addColumnIfNotExists('crafter', 'cluster_site_sync_repo', 'site_state', 'VARCHAR(50) NOT NULL DEFAULT ''CREATING''') ;

call addColumnIfNotExists('crafter', 'cluster_site_sync_repo', 'site_published_repo_created', 'INT NOT NULL DEFAULT 0') ;

UPDATE `cluster_site_sync_repo` cssr INNER JOIN `site` s ON cssr.`site_id` = s.`id` SET cssr.`site_state` = 'CREATED' WHERE s.`deleted` = 0 ;

UPDATE `cluster_site_sync_repo` cssr INNER JOIN `site` s ON cssr.`site_id` = s.`id` SET cssr.`site_state` = 'DELETED' WHERE s.`deleted` = 1 ;

UPDATE `cluster_site_sync_repo` cssr INNER JOIN `site` s ON cssr.`site_id` = s.`id` SET cssr.`site_published_repo_created` = s.`published_repo_created` ;

UPDATE _meta SET version = '3.1.11.4' ;