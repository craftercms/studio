ALTER TABLE `publish_request` ADD COLUMN `package_id` VARCHAR(50) NULL ;

UPDATE _meta SET version = '3.0.11.2' ;