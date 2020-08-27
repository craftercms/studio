ALTER TABLE `item` MODIFY `created_by` BIGINT NULL ;

ALTER TABLE `item` MODIFY `last_modified_by` BIGINT NULL ;

UPDATE _meta SET version = '3.2.0.5' ;