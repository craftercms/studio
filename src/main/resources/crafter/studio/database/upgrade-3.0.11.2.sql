ALTER TABLE `remote_repository` ADD UNIQUE IF NOT EXISTS `uq_rr_site_remote_name` (`site_id`, `remote_name`) ;

UPDATE _meta SET version = '3.0.11.3' ;