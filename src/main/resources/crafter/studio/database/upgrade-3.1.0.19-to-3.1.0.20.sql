ALTER TABLE `site` MODIFY COLUMN `search_engine` VARCHAR(20) NOT NULL DEFAULT 'Elasticsearch' ;

UPDATE `site` SET `search_engine` = 'Elasticsearch' WHERE `search_engine` = 'ElasticSearch' ;

UPDATE _meta SET version = '3.1.0.20' ;