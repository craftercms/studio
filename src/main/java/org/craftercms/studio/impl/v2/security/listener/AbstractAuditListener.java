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
package org.craftercms.studio.impl.v2.security.listener;

import org.craftercms.commons.http.RequestContext;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v2.dal.AuditLog;
import org.craftercms.studio.api.v2.service.audit.internal.AuditServiceInternal;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.springframework.security.authentication.event.AbstractAuthenticationEvent;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.TARGET_TYPE_USER;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_GLOBAL_SYSTEM_SITE;

/**
 * Base class for all audit authentication listeners
 *
 * @author joseross
 * @since 4.0
 */
public abstract class AbstractAuditListener {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected final StudioConfiguration studioConfiguration;
    protected final SiteService siteService;
    protected final AuditServiceInternal auditServiceInternal;

    public AbstractAuditListener(StudioConfiguration studioConfiguration, SiteService siteService,
                                 AuditServiceInternal auditServiceInternal) {
        this.studioConfiguration = studioConfiguration;
        this.siteService = siteService;
        this.auditServiceInternal = auditServiceInternal;
    }

    protected void recordAuthenticationEvent(String operation, AbstractAuthenticationEvent event, String message) {
        try {
            var username = event.getAuthentication().getName();
            var site = siteService.getSite(studioConfiguration.getProperty(CONFIGURATION_GLOBAL_SYSTEM_SITE));
            AuditLog auditLog = auditServiceInternal.createAuditLogEntry();
            auditLog.setOperation(operation);
            auditLog.setActorId(username);
            auditLog.setSiteId(site.getId());
            auditLog.setPrimaryTargetId(username);
            auditLog.setPrimaryTargetType(TARGET_TYPE_USER);
            auditLog.setPrimaryTargetValue(username);
            auditServiceInternal.insertAuditLog(auditLog);

            if (isNotEmpty(message)) {
                logger.info(message, event.getAuthentication().getName(),
                        RequestContext.getCurrent().getRequest().getRemoteAddr());
            }
        } catch (SiteNotFoundException e) {
            // This never happens
        }
    }
}
