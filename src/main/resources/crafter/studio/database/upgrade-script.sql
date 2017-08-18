-- 2017-06-19 Publishing process changes (commit_id added to workflow)
alter table cstudio_copytoenvironment add column `commit_id` VARCHAR(50) NULL;

-- 2017-06-22 Publishing APIs (start, stop, status)
alter table cstudio_site add column `publishing_enabled` INT NOT NULL DEFAULT 1;
alter table cstudio_site add column `publishing_status_message` VARCHAR(2000) NULL;

-- 2017-06-28 CRAFTERCMS-977 Unify site column width across DB
alter table cstudio_activity modify column `site_network` VARCHAR(50) NOT NULL;
alter table cstudio_dependency modify column `site` VARCHAR(50) NOT NULL;
alter table cstudio_site modify column `site_id` VARCHAR(50) NOT NULL;

-- 2017-06-30 CRAFTERCMS-981 Upsert groups from LDAP
alter table cstudio_group add column `externally_managed` INT NOT NULL DEFAULT 0;

-- 2017-08-15 CRAFTERCMS-1199 New dependency resolver
alter table dependency modify column `type` VARCHAR(50) NOT NULL;