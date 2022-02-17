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
package org.craftercms.studio.permissions;

import org.craftercms.commons.security.permissions.SubjectResolver;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.springframework.beans.factory.annotation.Required;

/**
 * Implementation of Crafter Commons' {@link SubjectResolver} that returns Crafter Studio's current username
 * as the Subject.
 *
 * @author avasquez
 */
public class SubjectResolverImpl implements SubjectResolver<String> {

    private SecurityService securityService;

    @Required
    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

    @Override
    public String getCurrentSubject() {
        return securityService.getCurrentUser();
    }

}
