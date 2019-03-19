ALTER TABLE `user` ADD COLUMN `deleted` INT NOT NULL DEFAULT 0 ;

UPDATE _meta SET version = '3.1.0.16' ;