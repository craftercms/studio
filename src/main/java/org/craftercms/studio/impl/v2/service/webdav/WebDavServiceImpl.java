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

package org.craftercms.studio.impl.v2.service.webdav;

import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.config.ConfigurationException;
import org.craftercms.commons.config.profiles.webdav.WebDavProfile;
import org.craftercms.commons.lang.UrlUtils;
import org.craftercms.commons.security.permissions.DefaultPermission;
import org.craftercms.commons.security.permissions.annotations.HasPermission;
import org.craftercms.commons.security.permissions.annotations.ProtectedResourceId;
import org.craftercms.commons.validation.annotations.param.ValidateParams;
import org.craftercms.commons.validation.annotations.param.ValidateStringParam;
import org.craftercms.studio.api.v1.exception.WebDavException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.webdav.WebDavItem;
import org.craftercms.studio.api.v2.service.webdav.WebDavService;
import org.craftercms.studio.impl.v1.util.config.profiles.SiteAwareConfigProfileLoader;
import org.springframework.util.MimeType;
import org.springframework.web.util.UriUtils;
import com.github.sardine.DavResource;
import com.github.sardine.Sardine;

import static org.craftercms.commons.file.stores.WebDavUtils.createClient;
import static org.springframework.util.MimeTypeUtils.ALL_VALUE;

/**
 * Default implementation of {@link WebDavService}.
 * @author joseross
 * @since 3.1.4
 */
public class WebDavServiceImpl implements WebDavService {

    private static final Logger logger = LoggerFactory.getLogger(WebDavServiceImpl.class);

    public static final String FILTER_ALL_ITEMS = "item";

    protected String urlPattern;

    /**
     * Instance of {@link SiteAwareConfigProfileLoader} used to load the configuration file.
     */
    protected SiteAwareConfigProfileLoader<WebDavProfile> profileLoader;

    /**
     * Charset used to encode paths in URLs.
     */
    protected Charset charset = Charset.defaultCharset();

    protected WebDavProfile getProfile(String site, String profileId) throws WebDavException  {
        try {
            return profileLoader.loadProfile(site, profileId);
        } catch (ConfigurationException e) {
            throw new WebDavException("Unable to load WebDav profile", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ValidateParams
    @HasPermission(type = DefaultPermission.class, action = "webdav_read")
    public List<WebDavItem> list(@ValidateStringParam(name = "siteId")
                                 @ProtectedResourceId("siteId") final String siteId,
                                 @ValidateStringParam(name = "profileId") final String profileId,
                                 @ValidateStringParam(name = "path") final String path,
                                 @ValidateStringParam(name = "type") final String type) throws WebDavException {
        WebDavProfile profile = getProfile(siteId, profileId);
        String listPath = StringUtils.appendIfMissing(profile.getBaseUrl(),"/");
        MimeType filterType;
        try {
            Sardine sardine = createClient(profile);
            if(StringUtils.isEmpty(type) || type.equals(FILTER_ALL_ITEMS)) {
                filterType = MimeType.valueOf(ALL_VALUE);
            } else {
                filterType = new MimeType(type);
            }

            if(StringUtils.isNotEmpty(path)) {
                String[] tokens = StringUtils.split(path, "/");
                for(String token : tokens) {
                    if(StringUtils.isNotEmpty(token)) {
                        listPath += StringUtils.appendIfMissing(UriUtils.encode(token, charset.name()), "/");
                    }
                }
            }

            if (!sardine.exists(listPath)) {
                logger.debug("Folder {0} doesn't exist", listPath);
                return Collections.emptyList();
            }
            logger.debug("Listing resources at {0}", listPath);
            List<DavResource> resources = sardine.list(listPath, 1, true);
            logger.debug("Found {0} resources at {0}", resources.size(), listPath);
            return resources.stream()
                .skip(1) // to avoid repeating the folder being listed
                .filter(r -> r.isDirectory() || filterType.includes(MimeType.valueOf(r.getContentType())))
                .map(r -> new WebDavItem(getName(r), getUrl(r, profileId, profile), r.isDirectory()))
                .collect(Collectors.toList());
        } catch (Exception e) {
            throw new WebDavException("Error listing resources", e);
        }
    }

    protected String getUrl(DavResource resource, String profileId, WebDavProfile profile) {
        String relativePath = RegExUtils.removeFirst(resource.getPath(), URI.create(profile.getBaseUrl()).getPath());
        if(resource.isDirectory()) {
            return relativePath;
        } else {
            return getRemoteAssetUrl(profileId, relativePath);
        }
    }

    protected String getRemoteAssetUrl(String profileId, String fullPath) {
        return String.format(urlPattern, profileId, StringUtils.removeStart(fullPath, "/"));
    }

    protected String getRemoteAssetUrl(String profileId, String path, String filename) {
        String fullPath = UrlUtils.concat(path, filename);
        return getRemoteAssetUrl(profileId, fullPath);
    }

    protected String getName(DavResource resource) {
        if(StringUtils.isNotEmpty(resource.getDisplayName())) {
            return resource.getDisplayName();
        } else {
            String path = resource.getPath();
            if(resource.isDirectory()) {
                path = StringUtils.removeEnd(path, "/");
            }
            return StringUtils.substringAfterLast(path, "/");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ValidateParams
    @HasPermission(type = DefaultPermission.class, action = "webdav_write")
    public WebDavItem upload(@ValidateStringParam(name = "siteId") @ProtectedResourceId("siteId") final String siteId,
                             @ValidateStringParam(name = "profileId") final String profileId,
                             @ValidateStringParam(name = "path") final String path,
                             @ValidateStringParam(name = "filename") final String filename,
                             final InputStream content)
        throws WebDavException {
        WebDavProfile profile = getProfile(siteId, profileId);
        String uploadUrl = StringUtils.appendIfMissing(profile.getBaseUrl(), "/");
        try {
            Sardine sardine = createClient(profile);

            if(StringUtils.isNotEmpty(path)) {
                String[] folders = StringUtils.split(path, "/");

                for(String folder : folders) {
                    uploadUrl += StringUtils.appendIfMissing(folder, "/");

                    logger.debug("Checking folder {0}", uploadUrl);
                    if(!sardine.exists(uploadUrl)) {
                        logger.debug("Creating folder {0}", uploadUrl);
                        sardine.createDirectory(uploadUrl);
                        logger.debug("Folder {0} created", uploadUrl);
                    } else {
                        logger.debug("Folder {0} already exists", uploadUrl);
                    }
                }
            }

            uploadUrl =  StringUtils.appendIfMissing(uploadUrl, "/");
            String fileUrl = uploadUrl + UriUtils.encode(filename, charset.name());

            logger.debug("Starting upload of file {0}", filename);
            logger.debug("Uploading file to {0}", fileUrl);

            sardine.put(fileUrl, content);
            logger.debug("Upload complete for file {0}", fileUrl);


            return new WebDavItem(filename, getRemoteAssetUrl(profileId, path, filename), false);
        } catch (Exception e ) {
            throw new WebDavException("Error uploading file", e);
        }
    }

    public String getUrlPattern() {
        return urlPattern;
    }

    public void setUrlPattern(String urlPattern) {
        this.urlPattern = urlPattern;
    }

    public SiteAwareConfigProfileLoader<WebDavProfile> getProfileLoader() {
        return profileLoader;
    }

    public void setProfileLoader(SiteAwareConfigProfileLoader<WebDavProfile> profileLoader) {
        this.profileLoader = profileLoader;
    }
}
