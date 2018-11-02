ALTER TABLE `cluster` ADD COLUMN IF NOT EXISTS `git_remote_name` VARCHAR(50) NOT NULL ;

ALTER TABLE `cluster` ADD UNIQUE IF NOT EXISTS `uq_cl_git_remote_name` (`git_remote_name`) ;

UPDATE _meta SET version = '3.1.0.7' ;