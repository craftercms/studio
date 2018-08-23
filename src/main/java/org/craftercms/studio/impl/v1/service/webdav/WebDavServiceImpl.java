/*
 * Copyright (C) 2007-2018 Crafter Software Corporation.
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
 */

package org.craftercms.studio.impl.v1.service.webdav;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

import org.craftercms.commons.validation.annotations.param.ValidateStringParam;
import org.craftercms.studio.api.v1.exception.WebDavException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.webdav.WebDavService;
import org.craftercms.studio.api.v1.webdav.WebDavItem;
import org.craftercms.studio.api.v1.webdav.WebDavProfile;
import org.craftercms.studio.api.v1.webdav.WebDavProfileReader;
import org.springframework.beans.factory.annotation.Required;
import com.github.sardine.DavResource;
import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;

/**
 * Default implementation of {@link WebDavService}.
 * @author joseross
 */
public class WebDavServiceImpl implements WebDavService {

    private static final Logger logger = LoggerFactory.getLogger(WebDavServiceImpl.class);

    /**
     * Instance of {@link WebDavProfileReader} used to parse the configuration file.
     */
    protected WebDavProfileReader profileReader;

    @Required
    public void setProfileReader(final WebDavProfileReader profileReader) {
        this.profileReader = profileReader;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<WebDavItem> list(@ValidateStringParam(name = "site_id") final String site,
                                 @ValidateStringParam(name = "profile") final String profileId,
                                 @ValidateStringParam(name = "path") final String path)
        throws
        WebDavException {
        WebDavProfile profile = profileReader.getProfile(site, profileId);
        String finalPath = profile.getBaseUrl() + "/" + path;
        Sardine sardine = SardineFactory.begin(profile.getUsername(), profile.getPassword());
        try {
            logger.info("Listing resources at {0}", finalPath);
            List<DavResource> resources = sardine.list(finalPath);
            logger.info("Found {0} resources", resources.size());
            return resources.stream()
                .filter(r -> !r.isDirectory())
                .map(r -> new WebDavItem(r.getDisplayName(), profile.getBaseUrl() + r.getPath(), r.isDirectory()))
                .collect(Collectors.toList());
        } catch (Exception e) {
            throw new WebDavException("Error listing resources", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String upload(@ValidateStringParam(name = "site_id") final String site,
                         @ValidateStringParam(name = "profile") final String profileId,
                         @ValidateStringParam(name = "path") final String path,
                         @ValidateStringParam(name = "filename") final String filename,
                         final InputStream content)
        throws WebDavException {
        WebDavProfile profile = profileReader.getProfile(site, profileId);
        String uploadUrl = profile.getBaseUrl() + path;
        String fileUrl = uploadUrl + "/" + filename;

        try {
            logger.info("Starting upload of file {0}", filename);
            Sardine sardine = SardineFactory.begin(profile.getUsername(), profile.getPassword());
            try {
                logger.info("Creating upload folder {0}", uploadUrl);
                sardine.createDirectory(uploadUrl);
            } catch (Exception e) {
                logger.info("Upload folder already exists");
            }
            sardine.put(fileUrl, content);
            logger.info("Upload complete");
            return fileUrl;
        } catch (Exception e ) {
            throw new WebDavException("Error uploading file", e);
        }
    }

}
