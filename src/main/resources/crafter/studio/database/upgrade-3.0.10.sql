UPDATE `audit` SET `source` = 'API' WHERE `source` = 'UI' ;

UPDATE _meta SET version = '3.0.11' ;