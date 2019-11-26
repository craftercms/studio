package org.craftercms.studio.api.v2.dal;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.ZonedDateTime;

public class PublishingPackage {

    private String packageId;
    private String siteId;
    private String environment;
    private String state;
    private ZonedDateTime scheduledDate;
    private String user;
    private String comment;

    @JsonProperty("id")
    public String getPackageId() {
        return packageId;
    }

    @JsonProperty("id")
    public void setPackageId(String packageId) {
        this.packageId = packageId;
    }

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @JsonProperty("schedule")
    public ZonedDateTime getScheduledDate() {
        return scheduledDate;
    }

    @JsonProperty("schedule")
    public void setScheduledDate(ZonedDateTime scheduledDate) {
        this.scheduledDate = scheduledDate;
    }

    @JsonProperty("approver")
    public String getUser() {
        return user;
    }

    @JsonProperty("approver")
    public void setUser(String user) {
        this.user = user;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
