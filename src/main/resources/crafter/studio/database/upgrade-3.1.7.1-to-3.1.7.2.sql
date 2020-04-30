ALTER TABLE `audit` MODIFY `primary_target_id` VARCHAR(1024) NOT NULL ;

ALTER TABLE `audit` MODIFY `primary_target_value` VARCHAR(1024) NOT NULL ;

ALTER TABLE `audit_parameters` MODIFY `target_id` VARCHAR(1024) NOT NULL ;

ALTER TABLE `audit_parameters` MODIFY `target_value` VARCHAR(1024) NOT NULL ;

UPDATE _meta SET version = '3.1.7.2' ;