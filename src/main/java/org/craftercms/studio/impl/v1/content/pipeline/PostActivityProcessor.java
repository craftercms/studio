/*
 * Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
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
package org.craftercms.studio.impl.v1.content.pipeline;

import org.craftercms.studio.api.v1.constant.DmConstants;
import org.craftercms.studio.api.v1.content.pipeline.PipelineContent;
import org.craftercms.studio.api.v1.dal.SiteFeed;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v1.to.ContentAssetInfoTO;
import org.craftercms.studio.api.v1.to.ResultTO;
import org.craftercms.studio.api.v2.dal.AuditLog;
import org.craftercms.studio.api.v2.dal.Item;
import org.craftercms.studio.api.v2.dal.User;
import org.craftercms.studio.api.v2.repository.ContentRepository;
import org.craftercms.studio.api.v2.service.audit.internal.ActivityStreamServiceInternal;
import org.craftercms.studio.api.v2.service.audit.internal.AuditServiceInternal;
import org.craftercms.studio.api.v2.service.item.internal.ItemServiceInternal;
import org.craftercms.studio.api.v2.service.security.internal.UserServiceInternal;
import org.craftercms.studio.impl.v1.util.ContentFormatUtils;
import org.craftercms.studio.impl.v2.utils.DateUtils;

import static org.craftercms.studio.api.v1.constant.StudioConstants.FILE_SEPARATOR;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.OPERATION_CREATE;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.OPERATION_UPDATE;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.TARGET_TYPE_CONTENT_ITEM;

public class PostActivityProcessor extends BaseContentProcessor {

    public static final String NAME = "PostActivityProcessor";

    protected AuditServiceInternal auditServiceInternal;
    protected SiteService siteService;
    protected ContentService contentService;
    protected ContentRepository contentRepository;
    protected ActivityStreamServiceInternal activityStreamServiceInternal;
    protected UserServiceInternal userServiceInternal;
    protected ItemServiceInternal itemServiceInternal;

    /**
     * default constructor
     */
    public PostActivityProcessor() {
        super(NAME);
    }

    /**
     * constructor that sets the process name
     *
     * @param name
     */
    public PostActivityProcessor(String name) {
        super(name);
    }

    public void process(PipelineContent content, ResultTO result) throws ServiceLayerException, UserNotFoundException {
        if (result.getCommitId() != null) {
            String site = content.getProperty(DmConstants.KEY_SITE);
            boolean skipAuditLogInsert =
                    ContentFormatUtils.getBooleanValue(content.getProperty(DmConstants.KEY_SKIP_AUDIT_LOG_INSERT));
            if (!skipAuditLogInsert) {
                String type = content.getProperty(DmConstants.KEY_ACTIVITY_TYPE);
                String user = content.getProperty(DmConstants.KEY_USER);
                String activityType = OPERATION_CREATE.equals(type) ? OPERATION_CREATE : OPERATION_UPDATE;
                String folderPath = content.getProperty(DmConstants.KEY_FOLDER_PATH);
                String fileName = content.getProperty(DmConstants.KEY_FILE_NAME);
                boolean isSystemAsset =
                        ContentFormatUtils.getBooleanValue(content.getProperty(DmConstants.KEY_SYSTEM_ASSET));
                if (isSystemAsset) {
                    ContentAssetInfoTO assetInfoTO = (ContentAssetInfoTO) result.getItem();
                    fileName = assetInfoTO.getFileName();
                }
                String uri = (folderPath.endsWith(FILE_SEPARATOR)) ? folderPath + fileName : folderPath + FILE_SEPARATOR
                        + fileName;
                SiteFeed siteFeed = siteService.getSite(site);
                AuditLog auditLog = auditServiceInternal.createAuditLogEntry();
                auditLog.setOperation(activityType);
                auditLog.setActorId(user);
                auditLog.setSiteId(siteFeed.getId());
                auditLog.setPrimaryTargetId(site + ":" + uri);
                auditLog.setPrimaryTargetType(TARGET_TYPE_CONTENT_ITEM);
                auditLog.setPrimaryTargetValue(uri);
                auditLog.setPrimaryTargetSubtype(contentService.getContentTypeClass(site, uri));
                auditServiceInternal.insertAuditLog(auditLog);

                User u = userServiceInternal.getUserByIdOrUsername(-1, user);
                Item item = itemServiceInternal.getItem(site, uri);
                activityStreamServiceInternal.insertActivity(siteFeed.getId(), u.getId(), activityType,
                        DateUtils.getCurrentTime(), item, null);
            }
            contentRepository.markGitLogAudited(site, result.getCommitId());
        }
    }

    public void setAuditServiceInternal(AuditServiceInternal auditServiceInternal) {
        this.auditServiceInternal = auditServiceInternal;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    public void setContentRepository(ContentRepository contentRepository) {
        this.contentRepository = contentRepository;
    }

    public void setActivityStreamServiceInternal(ActivityStreamServiceInternal activityStreamServiceInternal) {
        this.activityStreamServiceInternal = activityStreamServiceInternal;
    }

    public void setUserServiceInternal(UserServiceInternal userServiceInternal) {
        this.userServiceInternal = userServiceInternal;
    }

    public void setItemServiceInternal(ItemServiceInternal itemServiceInternal) {
        this.itemServiceInternal = itemServiceInternal;
    }
}
