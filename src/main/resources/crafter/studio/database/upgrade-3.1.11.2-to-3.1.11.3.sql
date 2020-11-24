DROP PROCEDURE tryLockSyncRepoForSite ;

call dropColumnIfExists('crafter', 'site', 'sync_repo_lock_owner') ;

call dropColumnIfExists('crafter', 'site', 'sync_repo_lock_heartbeat') ;

UPDATE _meta SET version = '3.1.11.3' ;