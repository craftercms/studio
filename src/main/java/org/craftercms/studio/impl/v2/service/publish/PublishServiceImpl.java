package org.craftercms.studio.impl.v2.service.publish;

import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.security.permissions.DefaultPermission;
import org.craftercms.commons.security.permissions.annotations.HasPermission;
import org.craftercms.commons.security.permissions.annotations.ProtectedResourceId;
import org.craftercms.studio.api.v1.dal.ItemMetadata;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.service.content.ObjectMetadataManager;
import org.craftercms.studio.api.v1.service.objectstate.ObjectStateService;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v2.dal.PublishingPackage;
import org.craftercms.studio.api.v2.dal.PublishingPackageDetails;
import org.craftercms.studio.api.v2.service.publish.PublishService;
import org.craftercms.studio.api.v2.service.publish.internal.PublishServiceInternal;

import java.util.ArrayList;
import java.util.List;

import static org.craftercms.studio.api.v1.service.objectstate.State.NEW_UNPUBLISHED_UNLOCKED;
import static org.craftercms.studio.api.v1.service.objectstate.TransitionEvent.REJECT;
import static org.craftercms.studio.permissions.PermissionResolverImpl.SITE_ID_RESOURCE_ID;
import static org.craftercms.studio.permissions.StudioPermissions.ACTION_CANCEL_PUBLISH;
import static org.craftercms.studio.permissions.StudioPermissions.ACTION_GET_PUBLISHING_QUEUE;

public class PublishServiceImpl implements PublishService {

    private PublishServiceInternal publishServiceInternal;
    private SiteService siteService;
    private ObjectStateService objectStateService;
    private ObjectMetadataManager objectMetadataManager;

    @Override
    @HasPermission(type = DefaultPermission.class, action = ACTION_GET_PUBLISHING_QUEUE)
    public int getPublishingPackagesTotal(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId, String environment,
                                          String path, String state)
            throws SiteNotFoundException {
        if (!siteService.exists(siteId)) {
            throw new SiteNotFoundException(siteId);
        }
        return publishServiceInternal.getPublishingPackagesTotal(siteId, environment, path, state);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = ACTION_GET_PUBLISHING_QUEUE)
    public List<PublishingPackage> getPublishingPackages(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId,
                                                         String environment, String path, String state, int offset,
                                                         int limit) throws SiteNotFoundException {
        if (!siteService.exists(siteId)) {
            throw new SiteNotFoundException(siteId);
        }
        return publishServiceInternal.getPublishingPackages(siteId, environment, path, state, offset, limit);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = ACTION_GET_PUBLISHING_QUEUE)
    public PublishingPackageDetails getPublishingPackageDetails(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId,
                                                                String packageId) throws SiteNotFoundException {
        if (!siteService.exists(siteId)) {
            throw new SiteNotFoundException(siteId);
        }
        return publishServiceInternal.getPublishingPackageDetails(siteId, packageId);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = ACTION_CANCEL_PUBLISH)
    public void cancelPublishingPackages(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId,
                                         List<String> packageIds) throws SiteNotFoundException {
        if (!siteService.exists(siteId)) {
            throw new SiteNotFoundException(siteId);
        }
        publishServiceInternal.cancelPublishingPackages(siteId, packageIds);
        for (String pId : packageIds) {
            PublishingPackageDetails packageDetails = publishServiceInternal.getPublishingPackageDetails(siteId, pId);
            List<String> paths = new ArrayList<String>();
            for (PublishingPackageDetails.PublishingPackageItem item : packageDetails.getItems()) {
                paths.add(item.getPath());
                ItemMetadata itemMetadata = objectMetadataManager.getProperties(siteId, item.getPath());
                if (itemMetadata != null) {
                    itemMetadata.setSubmittedBy(StringUtils.EMPTY);
                    itemMetadata.setSendEmail(0);
                    itemMetadata.setSubmittedForDeletion(0);
                    itemMetadata.setSubmissionComment(StringUtils.EMPTY);
                    itemMetadata.setLaunchDate(null);
                    itemMetadata.setSubmittedToEnvironment(StringUtils.EMPTY);
                    objectMetadataManager.updateObjectMetadata(itemMetadata);
                }
            }
            objectStateService.transitionBulk(siteId, paths, REJECT, NEW_UNPUBLISHED_UNLOCKED);
        }
    }

    public PublishServiceInternal getPublishServiceInternal() {
        return publishServiceInternal;
    }

    public void setPublishServiceInternal(PublishServiceInternal publishServiceInternal) {
        this.publishServiceInternal = publishServiceInternal;
    }

    public SiteService getSiteService() {
        return siteService;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public ObjectStateService getObjectStateService() {
        return objectStateService;
    }

    public void setObjectStateService(ObjectStateService objectStateService) {
        this.objectStateService = objectStateService;
    }

    public ObjectMetadataManager getObjectMetadataManager() {
        return objectMetadataManager;
    }

    public void setObjectMetadataManager(ObjectMetadataManager objectMetadataManager) {
        this.objectMetadataManager = objectMetadataManager;
    }
}
