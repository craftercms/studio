ALTER TABLE _meta ADD COLUMN `studio_id` VARCHAR(40) NOT NULL ;

UPDATE _meta SET studio_id = UUID() ;

UPDATE _meta SET version = '3.1.0.3' ;