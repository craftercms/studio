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
package org.craftercms.studio.impl.v2.upgrade.providers;

import org.apache.commons.io.IOUtils;
import org.craftercms.commons.upgrade.impl.UpgradeContext;
import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.impl.v2.upgrade.StudioUpgradeContext;
import org.springframework.core.io.Resource;

import java.beans.ConstructorProperties;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.List;

/**
 * Extension of {@link XmlFileVersionProvider} that also creates the version file if missing.
 *
 * @author joseross
 * @since 4.0.0
 */
public class SiteVersionProvider extends XmlFileVersionProvider {

    /**
     * Path of the default file.
     */
    protected Resource defaultFile;

    @ConstructorProperties({"path", "xpath", "defaultVersion", "contentRepository", "defaultFile"})
    public SiteVersionProvider(String path, String xpath, String defaultVersion, ContentRepository contentRepository,
                               Resource defaultFile) {
        super(path, xpath, defaultVersion, contentRepository);
        this.defaultFile = defaultFile;
    }

    @Override
    protected void doSetVersion(UpgradeContext<String> context, String newVersion) throws Exception {
        var studioContext = (StudioUpgradeContext) context;
        var file = studioContext.getFile(path);

        if (!Files.exists(file)) {
            logger.info("Creating new version file in site '{}'", context);
            try (InputStream in = defaultFile.getInputStream();
                 OutputStream out = Files.newOutputStream(file)) {
                IOUtils.copy(in, out);
                studioContext.commitChanges("[Upgrade Manager] Add version file", List.of(path), null);

            }
        } else {
            logger.debug("Version file already exists in site '{}'", context);
        }

        super.doSetVersion(context, newVersion);
    }

}
