ALTER TABLE `_meta` ADD COLUMN `integrity` BIGINT(10) NULL ;

UPDATE _meta SET version = '3.0.17' ;