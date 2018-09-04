ALTER TABLE `item_metadata` ADD COLUMN IF NOT EXISTS `submittedtoenvironment` VARCHAR(255) NULL ;

UPDATE _meta SET version = '3.0.15' ;