UPDATE IGNORE group_user
SET group_id = (SELECT id FROM `group` WHERE group_name = 'system_admin')
WHERE group_id = (SELECT id FROM `group` WHERE group_name = 'crafter-create-sites') ;
DELETE FROM `group` WHERE group_name = 'crafter-create-sites' ;

UPDATE _meta SET version = '3.1.0.4' ;