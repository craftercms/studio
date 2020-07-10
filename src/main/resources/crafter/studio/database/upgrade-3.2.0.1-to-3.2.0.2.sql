ALTER TABLE `site` ADD COLUMN `published_repo_created` INT NOT NULL DEFAULT 0 ;

UPDATE site SET published_repo_created = 1 WHERE deleted = 0 ;

UPDATE _meta SET version = '3.2.0.2' ;