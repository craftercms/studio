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
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import java.beans.ConstructorProperties;

import static org.craftercms.studio.api.v2.dal.AuditLogConstants.OPERATION_LOGIN;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.OPERATION_LOGIN_FAILED;

/**
 * Listener for Spring's login that records events using Studio's audit service
 *
 * @author joseross
 * @since 4.0
 */

public class AuditLoginListener extends AbstractAuditListener {

    @ConstructorProperties({"studioConfiguration", "siteService", "auditServiceInternal"})
    public AuditLoginListener(StudioConfiguration studioConfiguration, SiteService siteService,
                              AuditServiceInternal auditServiceInternal) {
        super(studioConfiguration, siteService, auditServiceInternal);
    }

    @EventListener
    public void recordAuthenticationSuccess(AuthenticationSuccessEvent event) {
        if (event.getAuthentication() instanceof PreAuthenticatedAuthenticationToken) {
//            Disabled because every request to the API triggers this event
//            recordAuthenticationEvent(OPERATION_PRE_AUTH, event, null);
        } else {
            recordAuthenticationEvent(OPERATION_LOGIN, event, "User {0} logged in from IP: {1}");
        }
    }

    @EventListener
    public void recordAuthenticationFailure(AbstractAuthenticationFailureEvent event) {
        recordAuthenticationEvent(OPERATION_LOGIN_FAILED, event,
                "Failed to authenticate user {0} logging in from IP: {1}. Reason: " +
                        event.getException().getLocalizedMessage());
        logger.debug("Authentication error for user {0}", event.getException(),
                event.getAuthentication().getName());
    }

}
