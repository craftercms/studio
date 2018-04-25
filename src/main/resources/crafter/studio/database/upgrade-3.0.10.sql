ALTER TABLE `remote_repository` ADD COLUMN `remote_branch` VARCHAR(50) NOT NULL ;

UPDATE _meta SET version = '3.0.11' ;