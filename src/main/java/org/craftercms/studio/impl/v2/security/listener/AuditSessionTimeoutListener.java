/*
 * Copyright (C) 2007-2026 Crafter Software Corporation. All Rights Reserved.
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

import org.craftercms.studio.api.v2.service.audit.AuditService;
import org.craftercms.studio.api.v2.service.site.SitesService;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.springframework.context.event.EventListener;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.session.HttpSessionDestroyedEvent;

import java.beans.ConstructorProperties;
import java.util.List;

import static org.craftercms.studio.api.v2.dal.AuditLogConstants.OPERATION_SESSION_TIMEOUT;

/**
 * Listener for Spring session destroyed event to record session timeout
 */
public class AuditSessionTimeoutListener extends AbstractAuditListener {

	@ConstructorProperties({"studioConfiguration", "siteService", "auditService"})
	public AuditSessionTimeoutListener(StudioConfiguration studioConfiguration, SitesService siteService, AuditService auditService) {
		super(studioConfiguration, siteService, auditService);
	}

	@EventListener
	public void recordSessionTimeout(HttpSessionDestroyedEvent event) {
		List<SecurityContext> contexts = event.getSecurityContexts();
		for (SecurityContext context : contexts) {
			logger.debug("Session destroyed for the security context '{}'", context);
			recordSessionTimeoutEvent(OPERATION_SESSION_TIMEOUT, context, "Session timeout for user '{}'");
		}
	}
}
