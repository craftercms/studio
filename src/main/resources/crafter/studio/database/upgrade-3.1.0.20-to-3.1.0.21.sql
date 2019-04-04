ALTER TABLE `audit` MODIFY COLUMN `primary_target_type` VARCHAR(32) NOT NULL ;

ALTER TABLE `audit_parameters` MODIFY COLUMN `target_type` VARCHAR(32) NOT NULL ;

UPDATE _meta SET version = '3.1.0.21' ;