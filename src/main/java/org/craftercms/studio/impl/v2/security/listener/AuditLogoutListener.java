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

import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v2.service.audit.internal.AuditServiceInternal;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.LogoutSuccessEvent;

import java.beans.ConstructorProperties;

import static org.craftercms.studio.api.v2.dal.AuditLogConstants.OPERATION_LOGOUT;

/**
 * Listener for Spring's logout that records events using Studio's audit service
 *
 * @author joseross
 * @since 4.0
 */
public class AuditLogoutListener extends AbstractAuditListener {

    @ConstructorProperties({"studioConfiguration", "siteService", "auditServiceInternal"})
    public AuditLogoutListener(StudioConfiguration studioConfiguration, SiteService siteService,
                               AuditServiceInternal auditServiceInternal) {
        super(studioConfiguration, siteService, auditServiceInternal);
    }

    @EventListener
    public void recordLogout(LogoutSuccessEvent event) {
        recordAuthenticationEvent(OPERATION_LOGOUT, event, "User {0} logged out from IP: {1}");
    }

}
