ALTER TABLE `site` ADD COLUMN `state` VARCHAR(50) NOT NULL DEFAULT 'CREATING' ;

UPDATE `site` SET `state` = 'CREATED' WHERE `deleted` = 0 ;

UPDATE `site` SET `state` = 'DELETED' WHERE `deleted` = 1 ;

ALTER TABLE `cluster_site_sync_repo` ADD COLUMN `state` VARCHAR(50) NOT NULL DEFAULT 'CREATING' ;

ALTER TABLE `cluster_site_sync_repo` ADD COLUMN `published_repo_created` INT NOT NULL DEFAULT 0 ;

UPDATE `cluster_site_sync_repo` SET `state` = 'CREATED' WHERE `site_id` IN (SELECT `id` FROM `site` WHERE `deleted` = 0 );

UPDATE `cluster_site_sync_repo` SET `state` = 'DELETED' WHERE `site_id` IN (SELECT `id` FROM `site` WHERE `deleted` = 1 ) ;

UPDATE `cluster_site_sync_repo` cssr INNER JOIN `site` s ON cssr.`site_id` = s.`id` SET cssr.`published_repo_created` = s.`published_repo_created` ;

UPDATE _meta SET version = '3.1.11.3' ;