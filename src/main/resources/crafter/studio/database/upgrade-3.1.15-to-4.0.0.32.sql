ALTER TABLE `publish_request`
DROP COLUMN `completed_date` ;

ALTER TABLE `site`
CHANGE COLUMN `state` `state` VARCHAR(50) NOT NULL DEFAULT 'INITIALIZING' ;
