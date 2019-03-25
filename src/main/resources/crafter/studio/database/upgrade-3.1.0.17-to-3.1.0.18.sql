ALTER TABLE `cluster` MODIFY COLUMN `local_address` VARCHAR(255) NOT NULL ;
ALTER TABLE `cluster` MODIFY COLUMN `git_url` VARCHAR(1024) NOT NULL ;
ALTER TABLE `cluster` MODIFY COLUMN `git_remote_name` VARCHAR(255) NOT NULL ;

UPDATE _meta SET version = '3.1.0.18' ;