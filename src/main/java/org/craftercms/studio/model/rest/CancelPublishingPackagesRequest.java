package org.craftercms.studio.model.rest;

import java.util.List;

public class CancelPublishingPackagesRequest {

    private String siteId;
    private List<String> packageIds;

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public List<String> getPackageIds() {
        return packageIds;
    }

    public void setPackageIds(List<String> packageIds) {
        this.packageIds = packageIds;
    }
}
