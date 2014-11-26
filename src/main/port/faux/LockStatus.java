package org.alfresco.service.cmr.lock;

public enum LockStatus {

    NO_LOCK,
    LOCKED,
    LOCK_OWNER,
    LOCK_EXPIRED;

    private LockStatus() {
    }
}