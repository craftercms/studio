package org.craftercms.studio.api.v2.dal;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class PublishStatus {

    public static final String READY = "ready";
    public static final String QUEUED = "queued";
    public static final String PUBLISHING = "publishing";
    public static final String STOPPED = "stopped";
    public static final String ERROR = "error";

    @JsonIgnore
    private long id;
    private boolean enabled;
    @JsonIgnore
    private int enabledAsInt;
    private String status;
    private String message;
    private String lockOwner;
    private String lockTTL;

    @JsonIgnore
    public long getId() {
        return id;
    }

    @JsonIgnore
    public void setId(long id) {
        this.id = id;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @JsonIgnore
    public int getEnabledAsInt() {
        return enabledAsInt;
    }

    @JsonIgnore
    public void setEnabledAsInt(int enabledAsInt) {
        this.enabledAsInt = enabledAsInt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getLockOwner() {
        return lockOwner;
    }

    public void setLockOwner(String lockOwner) {
        this.lockOwner = lockOwner;
    }

    public String getLockTTL() {
        return lockTTL;
    }

    public void setLockTTL(String lockTTL) {
        this.lockTTL = lockTTL;
    }
}
