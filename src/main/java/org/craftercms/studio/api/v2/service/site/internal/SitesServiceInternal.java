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

package org.craftercms.studio.api.v2.service.site.internal;

import org.craftercms.commons.plugin.model.PluginDescriptor;
import org.craftercms.studio.api.v1.exception.SiteAlreadyExistsException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v2.dal.PublishStatus;
import org.craftercms.studio.api.v2.exception.MissingPluginParameterException;

import java.util.List;
import java.util.Map;

public interface SitesServiceInternal {

    /**
     * Get list of available blueprints
     *
     * @return lost of blueprints
     */
    List<PluginDescriptor> getAvailableBlueprints();

    /**
     * Get the blueprint descriptor from the global repo
     * @param id the id of the blueprint
     * @return the descriptor object or null if not found
     */
    PluginDescriptor getBlueprintDescriptor(String id);

    /**
     * Get blueprint location
     *
     * @param blueprintId blueprint id
     * @return blueprint location
     */
    String getBlueprintLocation(String blueprintId);

    /**
     * Get the blueprint descriptor from a site repo
     * @param id the id of the site
     * @return the blueprint object or null if not found
     */
    PluginDescriptor getSiteBlueprintDescriptor(String id);

    /**
     * Validates that all required parameters are provided and have a value
     * @param descriptor the plugin descriptor to validate
     * @param params the parameters to validate
     * @throws MissingPluginParameterException if any of the required parameters is not valid
     */
    void validateBlueprintParameters(PluginDescriptor descriptor, Map<String, String> params)
        throws MissingPluginParameterException;

    /**
     * Updates the name and description for the given site
     *
     * @param siteId the id of the site
     * @param name the name of the site
     * @param description the description of the site
     *
     * @throws SiteNotFoundException if the site doesn't exist
     */
    void updateSite(String siteId, String name, String description)
            throws SiteNotFoundException, SiteAlreadyExistsException;

    /**
     * Get publishing status for site
     * @param siteId site identifier
     * @return publishing status
     */
    PublishStatus getPublishingStatus(String siteId);

    /**
     * Clear publishing lock for site
     * @param siteId site identifier
     */
    void clearPublishingLock(String siteId);
}
