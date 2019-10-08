package org.craftercms.studio.impl.v2.service.publish.internal;

import org.apache.commons.collections.CollectionUtils;
import org.craftercms.studio.api.v2.dal.PublishRequest;
import org.craftercms.studio.api.v2.dal.PublishRequestDAO;
import org.craftercms.studio.api.v2.dal.PublishingPackage;
import org.craftercms.studio.api.v2.dal.PublishingPackageDetails;
import org.craftercms.studio.api.v2.service.publish.internal.PublishServiceInternal;

import java.util.ArrayList;
import java.util.List;

import static org.craftercms.studio.api.v2.dal.PublishRequest.State.CANCELLED;

public class PublishServiceInternalImpl implements PublishServiceInternal {

    private PublishRequestDAO publishRequestDao;

    @Override
    public int getPublishingPackagesTotal(String siteId, String environment, String path, String state) {
        return publishRequestDao.getPublishingPackagesTotal(siteId, environment, path, state);
    }

    @Override
    public List<PublishingPackage> getPublishingPackages(String siteId, String environment, String path, String state, int offset, int limit) {
        return publishRequestDao.getPublishingPackages(siteId, environment, path, state, offset, limit);
    }

    @Override
    public PublishingPackageDetails getPublishingPackageDetails(String siteId, String packageId) {
        List<PublishRequest> publishingRequests = publishRequestDao.getPublishingPackageDetails(siteId, packageId);
        PublishingPackageDetails publishingPackageDetails = new PublishingPackageDetails();
        List<PublishingPackageDetails.PublishingPackageItem> packageItems =
                new ArrayList<PublishingPackageDetails.PublishingPackageItem>();
        if (CollectionUtils.isNotEmpty(publishingRequests)) {
            PublishRequest pr = publishingRequests.get(0);
            publishingPackageDetails.setSiteId(pr.getSite());
            publishingPackageDetails.setPackageId(pr.getPackageId());
            publishingPackageDetails.setEnvironment(pr.getEnvironment());
            publishingPackageDetails.setState(pr.getState());
            publishingPackageDetails.setScheduledDate(pr.getScheduledDate());
            publishingPackageDetails.setUser(pr.getUser());
            publishingPackageDetails.setComment(pr.getSubmissionComment());
        }
        for (PublishRequest publishRequest : publishingRequests) {
            PublishingPackageDetails.PublishingPackageItem item = new PublishingPackageDetails.PublishingPackageItem();
            item.setPath(publishRequest.getPath());
            item.setContentTypeClass(publishRequest.getContentTypeClass());
            packageItems.add(item);
        }
        publishingPackageDetails.setItems(packageItems);
        return publishingPackageDetails;
    }

    @Override
    public void cancelPublishingPackages(String siteId, List<String> packageIds) {
        publishRequestDao.cancelPackages(siteId, packageIds, CANCELLED);
    }

    public PublishRequestDAO getPublishRequestDao() {
        return publishRequestDao;
    }

    public void setPublishRequestDao(PublishRequestDAO publishRequestDao) {
        this.publishRequestDao = publishRequestDao;
    }
}
