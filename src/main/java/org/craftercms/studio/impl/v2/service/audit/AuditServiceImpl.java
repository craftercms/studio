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

package org.craftercms.studio.impl.v2.service.audit;

import org.craftercms.commons.security.permissions.DefaultPermission;
import org.craftercms.commons.security.permissions.annotations.HasPermission;
import org.craftercms.commons.security.permissions.annotations.ProtectedResourceId;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v2.dal.AuditLog;
import org.craftercms.studio.api.v2.service.audit.AuditService;
import org.craftercms.studio.api.v2.service.audit.internal.AuditServiceInternal;

import java.util.List;

import static org.craftercms.studio.permissions.PermissionResolverImpl.SITE_ID_RESOURCE_ID;

public class AuditServiceImpl implements AuditService {

    private AuditServiceInternal auditServiceInternal;
    private SiteService siteService;

    @Override
    @HasPermission(type = DefaultPermission.class, action = "audit_log")
    public List<AuditLog> getAuditLogForSite(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String site, int offset, int limit,
                                             String user, List<String> actions) throws SiteNotFoundException {
        if (!siteService.exists(site)) {
            throw new SiteNotFoundException();
        }
        return auditServiceInternal.getAuditLogForSite(site, offset, limit, user, actions);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = "audit_log")
    public int getAuditLogForSiteTotal(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String site, String user, List<String> actions)
            throws SiteNotFoundException {
        if (!siteService.exists(site)) {
            throw new SiteNotFoundException();
        }
        return auditServiceInternal.getAuditLogForSiteTotal(site, user, actions);
    }

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
