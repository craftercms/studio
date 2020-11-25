/*
 * Copyright (C) 2007-2020 Crafter Software Corporation. All Rights Reserved.
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
import org.craftercms.studio.api.v2.annotation.IsActionAllowed;
import org.craftercms.studio.api.v2.annotation.RetryingOperation;
import org.craftercms.studio.api.v2.dal.PublishRequest;
import org.craftercms.studio.api.v2.dal.PublishRequestDAO;
import org.craftercms.studio.api.v2.dal.PublishingHistoryItem;
import org.craftercms.studio.api.v2.dal.PublishingPackage;
import org.craftercms.studio.api.v2.dal.PublishingPackageDetails;
import org.craftercms.studio.api.v2.repository.ContentRepository;
import org.craftercms.studio.api.v2.service.publish.internal.PublishServiceInternal;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.craftercms.studio.api.v2.dal.PublishRequest.State.CANCELLED;
import static org.craftercms.studio.api.v2.security.AvailableActions.CANCEL_PUBLISH_CONST_LONG;
import static org.craftercms.studio.api.v2.security.AvailableActions.EVERYTHING_ALLOWED;
import static org.craftercms.studio.api.v2.security.AvailableActions.READ_PUBLISHING_QUEUE_CONST_LONG;

public class PublishServiceInternalImpl implements PublishServiceInternal {

    private PublishRequestDAO publishRequestDao;
    private ContentRepository contentRepository;

    @Override
    @IsActionAllowed(allowedActionsMask = READ_PUBLISHING_QUEUE_CONST_LONG)
    public int getPublishingPackagesTotal(String siteId, String environment, String path, List<String> states) {
        return publishRequestDao.getPublishingPackagesTotal(siteId, environment, path, states);
    }

    @Override
    @IsActionAllowed(allowedActionsMask = READ_PUBLISHING_QUEUE_CONST_LONG)
    public List<PublishingPackage> getPublishingPackages(String siteId, String environment, String path,
                                                         List<String> states, int offset, int limit) {
        return publishRequestDao.getPublishingPackages(siteId, environment, path, states, offset, limit);
    }

    @Override
    @IsActionAllowed(allowedActionsMask = READ_PUBLISHING_QUEUE_CONST_LONG)
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
    @IsActionAllowed(allowedActionsMask = CANCEL_PUBLISH_CONST_LONG)
    public void cancelPublishingPackages(String siteId, List<String> packageIds) {
        publishRequestDao.cancelPackages(siteId, packageIds, CANCELLED);
    }

    @Override
    @IsActionAllowed(allowedActionsMask = EVERYTHING_ALLOWED)
    public int getPublishingHistoryTotal(String siteId, String environment, String path, String publisher,
                                         ZonedDateTime dateFrom, ZonedDateTime dateTo, String contentType, long state) {
        return 0;
    }

    @Override
    @IsActionAllowed(allowedActionsMask = EVERYTHING_ALLOWED)
    public List<PublishingHistoryItem> getPublishingHistory(String siteId, String environment, String path,
                                                            String publisher, ZonedDateTime dateFrom,
                                                            ZonedDateTime dateTo, String contentType, long state,
                                                            String sortBy, String order, int offset, int limit) {
        return contentRepository.getPublishingHistory(siteId, environment, path, publisher, dateFrom, dateTo, limit);
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
}
