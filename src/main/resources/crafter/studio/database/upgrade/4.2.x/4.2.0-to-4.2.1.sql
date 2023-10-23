UPDATE `item` SET `system_type` = 'configuration' WHERE `path` LIKE '/config/%.xml' AND `path` NOT LIKE '/config/studio/content-types/%' AND `system_type` = 'file' ;

UPDATE `_meta` SET `version` = '4.2.1' ;