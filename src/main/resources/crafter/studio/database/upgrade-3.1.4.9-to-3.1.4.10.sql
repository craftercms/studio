ALTER TABLE `user` CHANGE COLUMN `username` `username` VARCHAR(255) ;

UPDATE _meta SET version = '3.1.4.10' ;