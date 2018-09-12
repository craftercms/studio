/*
 * Copyright (C) 2007-2018 Crafter Software Corporation. All rights reserved.
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
 *
 */

package org.craftercms.studio.impl.v2.upgrade;

import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.api.v1.util.StudioConfiguration;
import org.craftercms.studio.api.v2.upgrade.RepositoryUpgrade;
import org.springframework.beans.factory.annotation.Required;

/**
 * Base implementation of {@link RepositoryUpgrade} to provide access to configuration & repository.
 * @author joseross
 */
public abstract class AbstractRepositoryUpgrade implements RepositoryUpgrade {

    /**
     * The configuration instance.
     */
    protected StudioConfiguration studioConfiguration;

    /**
     * The content repository instance.
     */
    protected ContentRepository contentRepository;

    @Required
    public void setStudioConfiguration(final StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }

    @Required
    public void setContentRepository(final ContentRepository contentRepository) {
        this.contentRepository = contentRepository;
    }

}
