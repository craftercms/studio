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

package org.craftercms.studio.impl.v2.upgrade.operations.site;

import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.upgrade.exception.UpgradeException;
import org.craftercms.studio.api.v1.constant.StudioConstants;
import org.craftercms.studio.api.v1.dal.SiteFeed;
import org.craftercms.studio.api.v1.dal.SiteFeedMapper;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.impl.v2.upgrade.StudioUpgradeContext;
import org.craftercms.studio.impl.v2.upgrade.operations.AbstractUpgradeOperation;

import java.beans.ConstructorProperties;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.craftercms.studio.api.v2.dal.QueryParameterNames.SITE_ID;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.REPO_BASE_PATH;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.SITES_REPOS_PATH;

public class AddSiteUuidOperation extends AbstractUpgradeOperation {

    private static final Logger logger = LoggerFactory.getLogger(AddSiteUuidOperation.class);

    private SiteFeedMapper siteFeedMapper;

    @ConstructorProperties({"studioConfiguration", "siteFeedMapper"})
    public AddSiteUuidOperation(StudioConfiguration studioConfiguration, SiteFeedMapper siteFeedMapper) {
        super(studioConfiguration);
        this.siteFeedMapper = siteFeedMapper;
    }

    @Override
    public void doExecute(final StudioUpgradeContext context) throws UpgradeException {
        var site = context.getTarget();
        logger.debug("Get site data from database for site " + site);
        Map<String, String> params = new HashMap<String, String>();
        params.put(SITE_ID, site);
        SiteFeed siteFeed = siteFeedMapper.getSite(params);
        if (siteFeed != null) {
            try {
                logger.debug("Add UUID file for site " + site);
                addSiteUuidFile(site, siteFeed.getSiteUuid());
            } catch (IOException e) {
                throw new UpgradeException("Error when adding UUID file for site " + site, e);
            }
        }
    }

    private void addSiteUuidFile(String site, String siteUuid) throws IOException {
        if (StringUtils.isNotEmpty(siteUuid)) {
            Path path = Paths.get(studioConfiguration.getProperty(REPO_BASE_PATH),
                    studioConfiguration.getProperty(SITES_REPOS_PATH), site,
                    StudioConstants.SITE_UUID_FILENAME);
            String toWrite = StudioConstants.SITE_UUID_FILE_COMMENT + "\n" + siteUuid;
            logger.debug("Write UUID " + siteUuid + " to the file " + path.toString() + " for site " + site);
            Files.write(path, toWrite.getBytes());
        }
    }

}
