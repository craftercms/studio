-- 2017-06-19 Publishing process changes (commit_id added to workflow)
alter table cstudio_copytoenvironment add column `commit_id` VARCHAR(50) NULL;

-- 2017-06-22 Publishing APIs (start, stop, status)
alter table cstudio_site add column `publishing_enabled` INT NOT NULL DEFAULT 1;
alter table cstudio_site add column `publishing_status_message` VARCHAR(2000) NULL;