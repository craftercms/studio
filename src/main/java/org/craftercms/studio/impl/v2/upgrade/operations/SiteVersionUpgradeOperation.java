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

package org.craftercms.studio.impl.v2.upgrade.operations;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.configuration2.Configuration;
import org.craftercms.studio.api.v2.exception.UpgradeException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * Implementation of {@link org.craftercms.studio.api.v2.upgrade.UpgradeOperation} that updates or adds the version
 * file for the given site.
 * @author joseross
 */
public class SiteVersionUpgradeOperation extends XsltFileUpgradeOperation {

    /**
     * Path of the default file.
     */
    protected Resource defaultFile;

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(final String version, final Configuration config) {
        super.init(version, config);
        defaultFile = new ClassPathResource(config.getString("defaultFile"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(final String site) throws UpgradeException {
        if(contentRepository.contentExists(site, path)) {
            super.execute(site);
        } else {
            try(InputStream is = defaultFile.getInputStream()) {
                writeToRepo(site, path, is, "Added version file for future upgrades");
            } catch (IOException e) {
                throw new UpgradeException("Error adding version file to site " + site, e);
            }
        }
    }

}
