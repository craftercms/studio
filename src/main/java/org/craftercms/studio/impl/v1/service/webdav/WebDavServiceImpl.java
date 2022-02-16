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

package org.craftercms.studio.impl.v1.service.webdav;

import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.namespace.QName;

import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.config.ConfigurationException;
import org.craftercms.commons.config.profiles.webdav.WebDavProfile;
import org.craftercms.commons.validation.annotations.param.ValidateStringParam;
import org.craftercms.studio.api.v1.exception.WebDavException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.webdav.WebDavService;
import org.craftercms.studio.api.v1.webdav.WebDavItem;
import org.craftercms.studio.impl.v1.util.config.profiles.SiteAwareConfigProfileLoader;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.MimeType;
import org.springframework.web.util.UriUtils;
import com.github.sardine.DavResource;
import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;

import static com.github.sardine.util.SardineUtil.DEFAULT_NAMESPACE_PREFIX;
import static com.github.sardine.util.SardineUtil.DEFAULT_NAMESPACE_URI;
import static org.springframework.util.MimeTypeUtils.ALL_VALUE;

/**
 * Default implementation of {@link WebDavService}.
 * @author joseross
 * @deprecated This service has been replaced by {@link org.craftercms.studio.impl.v2.service.webdav.WebDavServiceImpl}
 */
@Deprecated
public class WebDavServiceImpl implements WebDavService {

    private static final Logger logger = LoggerFactory.getLogger(WebDavServiceImpl.class);

    public static final String PROPERTY_DISPLAY_NAME = "displayname";
    public static final String PROPERTY_CONTENT_TYPE = "getcontenttype";
    public static final String PROPERTY_RESOURCE_TYPE = "resourcetype";

    public static final String FILTER_ALL_ITEMS = "item";

    /**
     * Instance of {@link SiteAwareConfigProfileLoader} used to load the configuration file.
     */
    protected SiteAwareConfigProfileLoader<WebDavProfile> profileLoader;

    /**
     * Charset used to encode paths in URLs.
     */
    protected Charset charset;

    /**
     * Properties to request to the server when listing resources.
     */
    protected Set<QName> properties;

    public WebDavServiceImpl() {
        charset = Charset.defaultCharset();
        properties = new HashSet<>();
        properties.add(new QName(DEFAULT_NAMESPACE_URI, PROPERTY_DISPLAY_NAME, DEFAULT_NAMESPACE_PREFIX));
        properties.add(new QName(DEFAULT_NAMESPACE_URI, PROPERTY_CONTENT_TYPE, DEFAULT_NAMESPACE_PREFIX));
        properties.add(new QName(DEFAULT_NAMESPACE_URI, PROPERTY_RESOURCE_TYPE, DEFAULT_NAMESPACE_PREFIX));
    }

    @Required
    public void setProfileLoader(SiteAwareConfigProfileLoader<WebDavProfile> profileLoader) {
        this.profileLoader = profileLoader;
    }

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
    public List<WebDavItem> list(@ValidateStringParam(name = "site_id") final String site,
                                 @ValidateStringParam(name = "profile") final String profileId,
                                 @ValidateStringParam(name = "path") final String path,
                                 @ValidateStringParam(name = "type") final String type) throws WebDavException {
        WebDavProfile profile = getProfile(site, profileId);
        String listPath = StringUtils.appendIfMissing(profile.getBaseUrl(),"/");
        MimeType filterType;
        Sardine sardine = SardineFactory.begin(profile.getUsername(), profile.getPassword());
        try {
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
            String basePath = new URL(profile.getBaseUrl()).getPath();
            String baseDomain = profile.getBaseUrl();
            String deliveryUrl = profile.getDeliveryBaseUrl();
            logger.debug("Listing resources at {0}", listPath);
            List<DavResource> resources = sardine.propfind(listPath, 1, properties);
            logger.debug("Found {0} resources at {0}", resources.size(), listPath);
            return resources.stream()
                .skip(1) // to avoid repeating the folder being listed
                .filter(r -> r.isDirectory() || filterType.includes(MimeType.valueOf(r.getContentType())))
                .map(r ->
                    new WebDavItem(getName(r), getUrl(r, baseDomain, deliveryUrl, basePath), r.isDirectory()))
                .collect(Collectors.toList());
        } catch (Exception e) {
            throw new WebDavException("Error listing resources", e);
        }
    }

    
    protected String getUrl(DavResource resource, String baseUrl, String deliveryUrl, String basePath) {
        String relativePath = StringUtils.removeFirst(resource.getPath(), basePath);
        if(resource.isDirectory()) {
            return baseUrl + relativePath;
        } else {
            return (StringUtils.isNotEmpty(deliveryUrl)? deliveryUrl : baseUrl) + relativePath;
        }
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
    public String upload(@ValidateStringParam(name = "site_id") final String site,
                         @ValidateStringParam(name = "profile") final String profileId,
                         @ValidateStringParam(name = "path") final String path,
                         @ValidateStringParam(name = "filename") final String filename,
                         final InputStream content)
        throws WebDavException {
        WebDavProfile profile = getProfile(site, profileId);
        String uploadUrl = StringUtils.appendIfMissing(profile.getBaseUrl(), "/");
        try {
            Sardine sardine = SardineFactory.begin(profile.getUsername(), profile.getPassword());

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
            if(StringUtils.isNotEmpty(profile.getDeliveryBaseUrl())) {
                fileUrl = StringUtils.replaceFirst(fileUrl, profile.getBaseUrl(), profile.getDeliveryBaseUrl());
            }
            return fileUrl;
        } catch (Exception e ) {
            throw new WebDavException("Error uploading file", e);
        }
    }

}
