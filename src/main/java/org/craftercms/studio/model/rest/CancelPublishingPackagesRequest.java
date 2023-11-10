package org.craftercms.studio.model.rest;

import org.craftercms.commons.validation.annotations.param.ValidSiteId;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.springframework.util.CollectionUtils.isEmpty;

public class CancelPublishingPackagesRequest {
    @NotEmpty
    @Size(max = 50)
    @ValidSiteId
    private String siteId;
    private List<UUID> packageIds;

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public List<String> getPackageIds() {
        if (isEmpty(packageIds)) {
            return emptyList();
        }
        return packageIds.stream().map(UUID::toString)
                .collect(toList());
    }

    public void setPackageIds(List<UUID> packageIds) {
        this.packageIds = packageIds;
    }
}
