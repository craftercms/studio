ALTER TABLE `audit` MODIFY COLUMN `operation` VARCHAR(32) NOT NULL ;

UPDATE _meta SET version = '3.1.0.22' ;