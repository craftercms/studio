ALTER TABLE `cluster` CHANGE COLUMN `local_ip` `local_address` VARCHAR(40) NOT NULL ;

UPDATE _meta SET version = '3.1.0.9' ;