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

package org.craftercms.studio.impl.v2.service.security;

import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v2.security.AvailableActionsResolver;
import org.craftercms.studio.api.v2.service.security.SecurityService;

public class SecurityServiceImpl implements SecurityService {

    private static final Logger logger = LoggerFactory.getLogger(SecurityServiceImpl.class);

    private AvailableActionsResolver availableActionsResolver;

    @Override
    public long getAvailableActions(String username, String site, String path)
            throws ServiceLayerException, UserNotFoundException {
        return availableActionsResolver.getAvailableActions(username, site, path);
    }

    @Override
    public void invalidateAvailableActions(String site) {
        availableActionsResolver.invalidateAvailableActions(site);
    }

    @Override
    public void invalidateAvailableActions() {
        availableActionsResolver.invalidateAvailableActions();
    }

    public AvailableActionsResolver getAvailableActionsResolver() {
        return availableActionsResolver;
    }

    public void setAvailableActionsResolver(AvailableActionsResolver availableActionsResolver) {
        this.availableActionsResolver = availableActionsResolver;
    }
}
