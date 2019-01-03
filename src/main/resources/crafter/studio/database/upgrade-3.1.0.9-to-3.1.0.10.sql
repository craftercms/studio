ALTER TABLE `site` ADD COLUMN `search_engine` VARCHAR(20) NOT NULL DEFAULT 'ElasticSearch' ;

UPDATE _meta SET version = '3.1.0.10' ;