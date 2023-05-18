/*
 * Copyright (C) 2007-2023 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.api.v2.service.site;

import org.craftercms.commons.plugin.model.PluginDescriptor;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteAlreadyExistsException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v2.dal.PublishStatus;
import org.craftercms.studio.api.v2.exception.InvalidParametersException;

import java.util.List;

import static java.lang.String.format;

/**
 * Site-related operations
 */
public interface SitesService {

    /**
     * Checks if a site exists. If it does not, it throws a {@link SiteNotFoundException}
     *
     * @param siteId site ID
     * @throws SiteNotFoundException if no site is found for the given site ID
     */
    default void checkSiteExists(String siteId) throws SiteNotFoundException {
        if (!exists(siteId)) {
            throw new SiteNotFoundException(format("Site '%s' not found.", siteId));
        }
    }

    /**
     * Check if site already exists
     *
     * @param siteId site ID
     * @return true if site exists, false otherwise
     */
    boolean exists(String siteId);


    /**
     * Get list of available blueprints
     *
     * @return list of blueprints
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
     * Updates the name and description for the given site
     *
     * @param siteId the id of the site
     * @param name the name of the site
     * @param description the description of the site
     *
     * @throws SiteNotFoundException if the site doesn't exist
     */
    void updateSite(String siteId, String name, String description)
            throws SiteNotFoundException, SiteAlreadyExistsException, InvalidParametersException;

    /**
     * Enables/disables publishing for the given site
     * @param siteId the site id
     * @param enabled true to enable publishing, false to disable
     */
    void enablePublishing(String siteId, boolean enabled);

    /**
     * Get publishing status for site
     * @param siteId site identifier
     * @return publishing status
     */
    PublishStatus getPublishingStatus(String siteId) throws SiteNotFoundException;

    /**
     * Clear publishing lock for site
     * @param siteId site identifier
     */
    void clearPublishingLock(String siteId) throws SiteNotFoundException;

    /**
     * Delete a site from the system
     * @param siteId the site id
     * @throws SiteNotFoundException if the site doesn't exist
     */
    void deleteSite(String siteId) throws ServiceLayerException;
}
