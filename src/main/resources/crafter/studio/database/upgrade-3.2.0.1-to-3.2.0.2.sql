ALTER TABLE `site` ADD COLUMN IF NOT EXISTS `published_repo_created` INT NOT NULL DEFAULT 0 ;

UPDATE site SET published_repo_created = 1 WHERE deleted = 0 ;
