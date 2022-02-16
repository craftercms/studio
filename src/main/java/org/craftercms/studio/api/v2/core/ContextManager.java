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
package org.craftercms.studio.api.v2.core;

import org.craftercms.core.service.Context;

/**
 * Manages all {@link Context} objects for sites
 *
 * @author joseross
 * @since 4.0.0
 */
public interface ContextManager {

    /**
     * Returns the context for the given site, creating it if needed
     * @param siteId the id of the site
     * @return the context
     */
    Context getContext(String siteId);

    /**
     * Destroys the context for the given site
     * @param siteId the id of the site
     */
    void destroyContext(String siteId);

}
