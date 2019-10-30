ALTER TABLE `audit` MODIFY `actor_id` VARCHAR(255) NOT NULL ;

ALTER TABLE `audit` MODIFY `actor_details` VARCHAR(255) NULL ;

UPDATE _meta SET version = '3.1.4.13' ;