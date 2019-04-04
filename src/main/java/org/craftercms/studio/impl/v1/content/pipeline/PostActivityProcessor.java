/*
 * Copyright (C) 2007-2019 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
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
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v1.to.ContentAssetInfoTO;
import org.craftercms.studio.api.v1.to.ResultTO;
import org.craftercms.studio.api.v2.dal.AuditLog;
import org.craftercms.studio.api.v2.service.audit.internal.AuditServiceInternal;
import org.craftercms.studio.impl.v1.util.ContentFormatUtils;

import static org.craftercms.studio.api.v1.constant.StudioConstants.FILE_SEPARATOR;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.OPERATION_CREATE;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.OPERATION_UPDATE;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.TARGET_TYPE_CONTENT_ITEM;

public class PostActivityProcessor extends BaseContentProcessor {

    public static final String NAME = "PostActivityProcessor";


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

    public void process(PipelineContent content, ResultTO result) throws SiteNotFoundException {
        if (result.getCommitId() != null) {
            String type = content.getProperty(DmConstants.KEY_ACTIVITY_TYPE);
            String user = content.getProperty(DmConstants.KEY_USER);
            String activityType = OPERATION_CREATE.equals(type) ? OPERATION_CREATE : OPERATION_UPDATE;
            String site = (String) content.getProperty(DmConstants.KEY_SITE);
            String folderPath = (String) content.getProperty(DmConstants.KEY_FOLDER_PATH);
            String fileName = (String) content.getProperty(DmConstants.KEY_FILE_NAME);
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
            auditServiceInternal.insertAuditLog(auditLog);

        }
    }

    protected AuditServiceInternal auditServiceInternal;
    protected SiteService siteService;

    public AuditServiceInternal getAuditServiceInternal() {
        return auditServiceInternal;
    }

    public void setAuditServiceInternal(AuditServiceInternal auditServiceInternal) {
        this.auditServiceInternal = auditServiceInternal;
    }

    public SiteService getSiteService() {
        return siteService;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }
}
