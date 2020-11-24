DROP PROCEDURE tryLockSyncRepoForSite ;

CREATE PROCEDURE tryLockSyncRepoForSite(
    IN siteId VARCHAR(50),
    IN lockOwnerId VARCHAR(255),
    IN ttl INT,
    OUT locked INT)
BEGIN
    DECLARE v_lock_owner_id VARCHAR(255);
    DECLARE v_lock_heartbeat DATETIME;
    SELECT sync_repo_lock_owner, sync_repo_lock_heartbeat INTO  v_lock_owner_id, v_lock_heartbeat FROM site
    WHERE site_id = siteId AND deleted = 0;
    SET locked = 0;
    IF (v_lock_owner_id IS NULL OR v_lock_owner_id = '' OR v_lock_owner_id = lockOwnerId OR DATE_ADD(v_lock_heartbeat, INTERVAL ttl MINUTE) < CURRENT_TIMESTAMP)
    THEN
        UPDATE site SET sync_repo_lock_owner = lockOwnerId, sync_repo_lock_heartbeat = CURRENT_TIMESTAMP WHERE site_id = siteId and deleted = 0;
        SET locked = 1;
    END IF;
    SELECT locked;
END ;

UPDATE _meta SET version = '3.1.11.2' ;