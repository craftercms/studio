ALTER TABLE `item_metadata` ADD COLUMN `submittedtoenvironment` VARCHAR(255) NULL ;

UPDATE _meta SET version = '3.0.15' ;