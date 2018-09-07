UPDATE `group`
SET group_name = LCASE(group_name) ;

UPDATE _meta SET version = '3.1.0.2' ;