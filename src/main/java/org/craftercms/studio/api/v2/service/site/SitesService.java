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

package org.craftercms.studio.api.v2.service.site;

import org.craftercms.commons.plugin.model.PluginDescriptor;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteAlreadyExistsException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v2.dal.PublishStatus;
import org.craftercms.studio.api.v2.dal.Site;
import org.craftercms.studio.api.v2.exception.InvalidParametersException;
import org.craftercms.studio.api.v2.exception.InvalidSiteStateException;

import java.util.List;

public interface SitesService {

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
     * Check if current site state is matches the given state
     *
     * @param siteId site id
     * @param state  desired state
     * @throws InvalidSiteStateException if the site state doesn't match the given state
     * @throws SiteNotFoundException if the site doesn't exist
     */
    void checkSiteState(String siteId, String state) throws InvalidSiteStateException, SiteNotFoundException;

    /**
     * Duplicate a site
     *
     * @param sourceSiteId       the id of the site to duplicate
     * @param siteId             the id of the new site
     * @param siteName           the name of the new site
     * @param description        the description of the new site
     * @param sandboxBranch      the sandbox branch to use
     * @param readOnlyBlobStores whether the blob stores should be read only. Notice that this value is
     *                           overridden (forced to false) if serverless delivery is enabled
     * @throws ServiceLayerException if there is an error duplicating the site
     */
    void duplicate(String sourceSiteId, String siteId, String siteName, String description, String sandboxBranch, boolean readOnlyBlobStores)
            throws ServiceLayerException;

    /**
     * Get the sites matching a given state
     *
     * @param state the state to match. See {@link org.craftercms.studio.api.v2.dal.Site.State}
     * @return the list of sites matching the given state
     */
    List<Site> getSitesByState(String state);
}
