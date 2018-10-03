ALTER TABLE `site` ADD COLUMN IF NOT EXISTS `sandbox_branch` VARCHAR(255) NOT NULL DEFAULT 'master' ;

UPDATE _meta SET version = '3.0.15.1' ;