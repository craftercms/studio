ALTER TABLE `item_metadata` CHANGE COLUMN `lockowner` `lock_owner` VARCHAR(255) ;

ALTER TABLE `item_metadata` CHANGE COLUMN `modified` `last_modified_date` DATETIME ;

addColumnIfNotExists(@crafter_schema_name, 'item_metadata', 'label', 'VARCHAR(255) NULL') ;

addColumnIfNotExists(@crafter_schema_name, 'item_metadata', 'content_type_id', 'VARCHAR(255) NULL') ;

addColumnIfNotExists(@crafter_schema_name, 'item_metadata', 'preview_url', 'text NULL') ;

addColumnIfNotExists(@crafter_schema_name, 'item_metadata', 'system_type', 'VARCHAR(50) NULL') ;

addColumnIfNotExists(@crafter_schema_name, 'item_metadata', 'mime_type', 'VARCHAR(50) NULL') ;

addColumnIfNotExists(@crafter_schema_name, 'item_metadata', 'state', 'INT NOT NULL DEFAULT 0') ;

addColumnIfNotExists(@crafter_schema_name, 'item_metadata', 'disabled', 'INT NOT NULL DEFAULT 0') ;

addColumnIfNotExists(@crafter_schema_name, 'item_metadata', 'locale_code', 'VARCHAR(20) NULL') ;

addColumnIfNotExists(@crafter_schema_name, 'item_metadata', 'translation_source_id', 'BIGINT(20) NULL') ;

addColumnIfNotExists(@crafter_schema_name, 'item_metadata', 'created_date', 'DATETIME NULL') ;

addColumnIfNotExists(@crafter_schema_name, 'item_metadata', 'size_in_bytes', 'INT NULL') ;

addColumnIfNotExists(@crafter_schema_name, 'item_metadata', 'item_id', 'VARCHAR(255) NULL') ;

CREATE TABLE IF NOT EXISTS `item_translation` (
  `object_id` VARCHAR(255) NOT NULL,
  `source_id` BIGINT(20) NOT NULL,
  `translation_id` BIGINT(20) NOT NULL,
  `locale_code` VARCHAR(20) NOT NULL,
  `date_translated` DATETIME NOT NULL,
  PRIMARY KEY (`object_id`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8
  ROW_FORMAT = DYNAMIC ;

UPDATE _meta SET version = '3.1.7.1' ;