UPDATE `group`
SET group_name = LCASE(group_name) ;

INSERT IGNORE INTO `group` (record_last_updated, org_id, group_name, group_description)
VALUES (CURRENT_TIMESTAMP, 1, 'site_admin', 'Site Administrator group') ;

INSERT IGNORE INTO `group` (record_last_updated, org_id, group_name, group_description)
VALUES (CURRENT_TIMESTAMP, 1, 'site_author', 'Site Author group') ;

INSERT IGNORE INTO `group` (record_last_updated, org_id, group_name, group_description)
VALUES (CURRENT_TIMESTAMP, 1, 'site_publisher', 'Site Publisher group') ;

INSERT IGNORE INTO `group` (record_last_updated, org_id, group_name, group_description)
VALUES (CURRENT_TIMESTAMP, 1, 'site_developer', 'Site Developer group') ;

INSERT IGNORE INTO `group` (record_last_updated, org_id, group_name, group_description)
VALUES (CURRENT_TIMESTAMP, 1, 'site_reviewer', 'Site Reviewer group') ;

UPDATE _meta SET version = '3.1.0.2' ;