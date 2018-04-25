ALTER TABLE `remote_repository` DROP COLUMN `remote_branch` ;

UPDATE `audit` SET `source` = 'API' WHERE `source` = 'UI' ;

UPDATE _meta SET version = '3.0.11.1' ;