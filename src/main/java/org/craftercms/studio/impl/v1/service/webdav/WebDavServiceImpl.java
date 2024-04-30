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

package org.craftercms.studio.impl.v1.service.webdav;

import com.github.sardine.DavResource;
import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.config.ConfigurationException;
import org.craftercms.commons.config.profiles.ConfigurationProfileNotFoundException;
import org.craftercms.commons.config.profiles.webdav.WebDavProfile;
import org.craftercms.commons.validation.annotations.param.ValidateStringParam;
import org.craftercms.studio.api.v1.exception.WebDavException;
import org.craftercms.studio.api.v1.service.webdav.WebDavService;
import org.craftercms.studio.api.v1.webdav.WebDavItem;
import org.craftercms.studio.impl.v1.util.config.profiles.SiteAwareConfigProfileLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.MimeType;
import org.springframework.web.util.UriUtils;

import javax.xml.namespace.QName;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    public WebDavServiceImpl(SiteAwareConfigProfileLoader<WebDavProfile> profileLoader) {
        charset = Charset.defaultCharset();
        properties = new HashSet<>();
        properties.add(new QName(DEFAULT_NAMESPACE_URI, PROPERTY_DISPLAY_NAME, DEFAULT_NAMESPACE_PREFIX));
        properties.add(new QName(DEFAULT_NAMESPACE_URI, PROPERTY_CONTENT_TYPE, DEFAULT_NAMESPACE_PREFIX));
        properties.add(new QName(DEFAULT_NAMESPACE_URI, PROPERTY_RESOURCE_TYPE, DEFAULT_NAMESPACE_PREFIX));
        this.profileLoader = profileLoader;
    }

    protected WebDavProfile getProfile(String site, String profileId) throws WebDavException, ConfigurationProfileNotFoundException {
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
    public List<WebDavItem> list(@ValidateStringParam final String site,
                                 @ValidateStringParam final String profileId,
                                 @ValidateStringParam final String path,
                                 @ValidateStringParam final String type) throws WebDavException, ConfigurationProfileNotFoundException {
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
                logger.debug("Folder '{}' doesn't exist in site '{}'", listPath, site);
                return Collections.emptyList();
            }
            String basePath = new URL(profile.getBaseUrl()).getPath();
            String baseDomain = profile.getBaseUrl();
            String deliveryUrl = profile.getDeliveryBaseUrl();
            logger.debug("List resources at site '{}' path '{}'", site, listPath);
            List<DavResource> resources = sardine.propfind(listPath, 1, properties);
            logger.debug("Found '{}' resources at site '{}' path '{}'", resources.size(), site, listPath);
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
    public String upload(@ValidateStringParam final String site,
                         @ValidateStringParam final String profileId,
                         @ValidateStringParam final String path,
                         @ValidateStringParam final String filename,
                         final InputStream content)
            throws WebDavException, ConfigurationProfileNotFoundException {
        WebDavProfile profile = getProfile(site, profileId);
        String uploadUrl = StringUtils.appendIfMissing(profile.getBaseUrl(), "/");
        try {
            Sardine sardine = SardineFactory.begin(profile.getUsername(), profile.getPassword());

            if(StringUtils.isNotEmpty(path)) {
                String[] folders = StringUtils.split(path, "/");

                for(String folder : folders) {
                    uploadUrl += StringUtils.appendIfMissing(folder, "/");

                    logger.debug("Check folder at site '{}' path '{}'", site, uploadUrl);
                    if(!sardine.exists(uploadUrl)) {
                        logger.debug("Create folder in site '{}' path '{}'", site, uploadUrl);
                        sardine.createDirectory(uploadUrl);
                        logger.debug("Folder in site '{}' path '{}' successfully created", site, uploadUrl);
                    } else {
                        logger.debug("Folder in site '{}' path '{}' already exists", site, uploadUrl);
                    }
                }
            }

            uploadUrl =  StringUtils.appendIfMissing(uploadUrl, "/");
            String fileUrl = uploadUrl + UriUtils.encode(filename, charset.name());

            logger.debug("Start uploading file '{}' to site '{}' path '{}' file URL '{}'",
                    filename, site, uploadUrl, fileUrl);

            sardine.put(fileUrl, content);
            if(StringUtils.isNotEmpty(profile.getDeliveryBaseUrl())) {
                fileUrl = StringUtils.replaceFirst(fileUrl, profile.getBaseUrl(), profile.getDeliveryBaseUrl());
            }
            return fileUrl;
        } catch (Exception e) {
            logger.error("Failed to upload file '{}' to site '{}' path '{}'",
                    filename, site, uploadUrl, e);
            throw new WebDavException("Failed to upload file to WebDAV", e);
        }
    }

}
