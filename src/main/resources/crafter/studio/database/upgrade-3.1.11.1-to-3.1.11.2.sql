DROP PROCEDURE tryLockSyncRepoForSite ;

ALTER TABLE `site` DROP COLUMN `sync_repo_lock_owner` ;

ALTER TABLE `site` DROP COLUMN `sync_repo_lock_heartbeat` ;

UPDATE _meta SET version = '3.1.11.2' ;