ALTER TABLE `user` MODIFY `first_name` VARCHAR(32)  NOT NULL ;

ALTER TABLE `user` MODIFY `last_name` VARCHAR(32)  NOT NULL ;

UPDATE _meta SET version = '3.1.4.7' ;