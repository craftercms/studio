CREATE PROCEDURE update_parent_id(IN siteId VARCHAR(50))
BEGIN
    DECLARE v_parent_id VARCHAR(255);
    DECLARE v_parent_path VARCHAR(2000);
    DECLARE v_parent_item_path VARCHAR(2000);
    DECLARE v_finished INTEGER DEFAULT 0;
    DECLARE parent_cursor CURSOR FOR SELECT ist.object_id as parent_id, REPLACE(ist.path, '/index.xml', '') AS parent_path, ist.path AS parent_item_path FROM item_state ist WHERE ist.site = siteId;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET v_finished = 1;
    OPEN parent_cursor;
    update_parent: LOOP
        FETCH parent_cursor INTO v_parent_id, v_parent_path;
        IF v_finished = 1 THEN LEAVE update_parent;
        END IF;
        UPDATE item_metadata SET item_id = v_parent_id WHERE site = siteId and path = v_parent_item_path;
        UPDATE item_metadata SET parent_id = v_parent_id WHERE site = siteId AND path RLIKE (concat(v_parent_path, '/[^/]+/index\.xml|', v_parent_path,'/(?!index\.xml)[^/]+$'));
    END LOOP update_parent;
END ;

ALTER TABLE `item_metadata` CHANGE COLUMN `lockowner` `lock_owner` VARCHAR(255) ;

ALTER TABLE `item_metadata` CHANGE COLUMN `modified` `last_modified_date` DATETIME ;

call addColumnIfNotExists('@crafter_schema_name', 'item_metadata', 'label', 'VARCHAR(255) NULL') ;

call addColumnIfNotExists('@crafter_schema_name', 'item_metadata', 'content_type_id', 'VARCHAR(255) NULL') ;

call addColumnIfNotExists('@crafter_schema_name', 'item_metadata', 'preview_url', 'text NULL') ;

call addColumnIfNotExists('@crafter_schema_name', 'item_metadata', 'system_type', 'VARCHAR(50) NULL') ;

call addColumnIfNotExists('@crafter_schema_name', 'item_metadata', 'mime_type', 'VARCHAR(50) NULL') ;

call addColumnIfNotExists('@crafter_schema_name', 'item_metadata', 'state', 'INT NOT NULL DEFAULT 0') ;

call addColumnIfNotExists('@crafter_schema_name', 'item_metadata', 'disabled', 'INT NOT NULL DEFAULT 0') ;

call addColumnIfNotExists('@crafter_schema_name', 'item_metadata', 'locale_code', 'VARCHAR(20) NULL') ;

call addColumnIfNotExists('@crafter_schema_name', 'item_metadata', 'translation_source_id', 'VARCHAR(255) NULL') ;

call addColumnIfNotExists('@crafter_schema_name', 'item_metadata', 'created_date', 'DATETIME NULL') ;

call addColumnIfNotExists('@crafter_schema_name', 'item_metadata', 'size_in_bytes', 'INT NULL') ;

call addColumnIfNotExists('@crafter_schema_name', 'item_metadata', 'item_id', 'VARCHAR(255) NULL') ;

call addColumnIfNotExists('@crafter_schema_name', 'item_metadata', 'parent_id', 'VARCHAR(255) NULL') ;

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