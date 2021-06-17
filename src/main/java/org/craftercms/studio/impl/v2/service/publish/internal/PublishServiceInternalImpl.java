/*
 * Copyright (C) 2007-2021 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.craftercms.studio.impl.v2.service.publish.internal;

import org.apache.commons.collections.CollectionUtils;
import org.craftercms.studio.api.v1.util.filter.DmFilterWrapper;
import org.craftercms.studio.api.v2.annotation.RetryingOperation;
import org.craftercms.studio.api.v2.dal.DeploymentHistoryItem;
import org.craftercms.studio.api.v2.dal.PublishRequest;
import org.craftercms.studio.api.v2.dal.PublishRequestDAO;
import org.craftercms.studio.api.v2.dal.PublishingHistoryItem;
import org.craftercms.studio.api.v2.dal.PublishingPackage;
import org.craftercms.studio.api.v2.dal.PublishingPackageDetails;
import org.craftercms.studio.api.v2.repository.ContentRepository;
import org.craftercms.studio.api.v2.service.publish.internal.PublishServiceInternal;
;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.craftercms.studio.api.v1.constant.StudioConstants.CONTENT_TYPE_ASSET;
import static org.craftercms.studio.api.v1.constant.StudioConstants.CONTENT_TYPE_COMPONENT;
import static org.craftercms.studio.api.v1.constant.StudioConstants.CONTENT_TYPE_PAGE;
import static org.craftercms.studio.api.v2.dal.PublishRequest.State.CANCELLED;
import static org.craftercms.studio.api.v2.dal.PublishRequest.State.COMPLETED;

public class PublishServiceInternalImpl implements PublishServiceInternal {

    private PublishRequestDAO publishRequestDao;
    private ContentRepository contentRepository;
    private DmFilterWrapper dmFilterWrapper;

    @Override
    public int getPublishingPackagesTotal(String siteId, String environment, String path, List<String> states) {
        return publishRequestDao.getPublishingPackagesTotal(siteId, environment, path, states);
    }

    @Override
    public List<PublishingPackage> getPublishingPackages(String siteId, String environment, String path,
                                                         List<String> states, int offset, int limit) {
        return publishRequestDao.getPublishingPackages(siteId, environment, path, states, offset, limit);
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

    @RetryingOperation
    @Override
    public void cancelPublishingPackages(String siteId, List<String> packageIds) {
        publishRequestDao.cancelPackages(siteId, packageIds, CANCELLED);
    }

    @Override
    public int getPublishingHistoryTotal(String siteId, String environment, String path, String publisher,
                                         ZonedDateTime dateFrom, ZonedDateTime dateTo, String contentType, long state) {
        return 0;
    }

    @Override
    public List<PublishingHistoryItem> getPublishingHistory(String siteId, String environment, String path,
                                                            String publisher, ZonedDateTime dateFrom,
                                                            ZonedDateTime dateTo, String contentType, long state,
                                                            String sortBy, String order, int offset, int limit) {
        return contentRepository.getPublishingHistory(siteId, environment, path, publisher, dateFrom, dateTo, limit);
    }

    @Override
    public List<DeploymentHistoryItem> getDeploymentHistory(String siteId, List<String> environments,
                                                            ZonedDateTime fromDate, ZonedDateTime toDate,
                                                            String filterType, int numberOfItems) {
        int offset = 0;
        int counter = 0;
        List<DeploymentHistoryItem> toRet = new ArrayList<DeploymentHistoryItem>();
        String contentTypeClass = null;
        switch (filterType) {
            case CONTENT_TYPE_PAGE:
            case CONTENT_TYPE_COMPONENT:
            case CONTENT_TYPE_ASSET:
                contentTypeClass = filterType;
                break;
            default:
                contentTypeClass = null;
        }
        List<PublishRequest> deploymentHistory = publishRequestDao.getDeploymentHistory(siteId, environments,
                COMPLETED, contentTypeClass, fromDate, toDate, offset, numberOfItems);
        if (CollectionUtils.isNotEmpty(deploymentHistory)) {
            for (PublishRequest publishRequest : deploymentHistory) {
                    DeploymentHistoryItem dhi = new DeploymentHistoryItem();
                    dhi.setSite(siteId);
                    dhi.setPath(publishRequest.getPath());
                    dhi.setDeploymentDate(publishRequest.getCompletedDate());
                    dhi.setUser(publishRequest.getUser());
                    dhi.setEnvironment(publishRequest.getEnvironment());
                    toRet.add(dhi);
                    if (!(++counter < numberOfItems)) {
                        break;
                    }
            }

        }
        toRet.sort((o1, o2) -> o2.getDeploymentDate().compareTo(o1.getDeploymentDate()));
        return toRet;
    }

    public PublishRequestDAO getPublishRequestDao() {
        return publishRequestDao;
    }

    public void setPublishRequestDao(PublishRequestDAO publishRequestDao) {
        this.publishRequestDao = publishRequestDao;
    }

    public ContentRepository getContentRepository() {
        return contentRepository;
    }

    public void setContentRepository(ContentRepository contentRepository) {
        this.contentRepository = contentRepository;
    }

    public DmFilterWrapper getDmFilterWrapper() {
        return dmFilterWrapper;
    }

    public void setDmFilterWrapper(DmFilterWrapper dmFilterWrapper) {
        this.dmFilterWrapper = dmFilterWrapper;
    }
}
