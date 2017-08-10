/*
 * Crafter Studio Web-content authoring solution
 * Copyright (C) 2007-2017 Crafter Software Corporation.
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

package org.craftercms.studio.impl.v1.service.dependency;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.craftercms.studio.api.v1.constant.StudioConstants;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.service.dependency.DependencyResolver;
import org.craftercms.studio.api.v1.to.DependencyResolverConfigTO;
import org.craftercms.studio.api.v1.util.StudioConfiguration;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.craftercms.studio.api.v1.util.StudioConfiguration.CONFIGURATION_SITE_DEPENDENCY_RESOLVER_CONFIG_BASE_PATH;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.CONFIGURATION_SITE_DEPENDENCY_RESOLVER_CONFIG_FILE_NAME;

public class RegexDependencyResolver implements DependencyResolver {

    private final static Logger logger = LoggerFactory.getLogger(RegexDependencyResolver.class);

    protected ContentService contentService;
    protected StudioConfiguration studioConfiguration;

    @Override
    public Set<String> resolve(String site, String path, String dependencyType, String mimetype, InputStream content) throws IOException {
        String contentString = IOUtils.toString(content);
        return resolve(site, path, dependencyType, mimetype, contentString);
    }

    @Override
    public Set<String> resolve(String site, String path, String dependencyType, String mimetype, String content) {
        DependencyResolverConfigTO config = getConfiguraion(site);
        Set<String> toRet = new HashSet<String>();
        if (config != null) {
            DependencyResolverConfigTO.DependencyType depType = config.getDependencyTypes().get(dependencyType);
            if (depType != null) {
                DependencyResolverConfigTO.MimeType mimeType = depType.getMimetypes().get(mimetype);
                if (mimeType != null) {
                    List<String> regexPatterns = mimeType.getPatterns();
                    if (CollectionUtils.isNotEmpty(regexPatterns)) {
                        for (String regexPattern : regexPatterns) {
                            Pattern pattern = Pattern.compile(regexPattern);
                            Matcher matcher = pattern.matcher(content);
                            while (matcher.find()) {
                                String matchedPath = matcher.group();
                                if (contentService.contentExists(site, matchedPath)) {
                                    toRet.add(matchedPath);
                                } else {
                                    logger.info("Found reference to " + matchedPath + " in content at " + path + " but content does not exist in referenced path for site " + site);
                                }
                            }
                        }
                    }
                }
            }
        } else {
            logger.error("Could not find Dependency Resolver configuration.");
        }
        return toRet;
    }

    private DependencyResolverConfigTO getConfiguraion(String site) {
        DependencyResolverConfigTO config = null;
        String configLocation = getConfigPath().replaceFirst(StudioConstants.PATTERN_SITE, site);
        configLocation = configLocation + "/" + getConfigFileName();
        Document document = null;
        try {
            document = contentService.getContentAsDocument(site, configLocation);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        if (document != null) {
            Element root = document.getRootElement();
            config = new DependencyResolverConfigTO();

            Element types = root.element("types");
            if (types != null) {
                Map<String, DependencyResolverConfigTO.DependencyType> dependencyTypes = new HashMap<String, DependencyResolverConfigTO.DependencyType>();
                Iterator<Element> iterTypes = types.elementIterator("dependency-type");
                while (iterTypes.hasNext()) {
                    DependencyResolverConfigTO.DependencyType dependencyType = new DependencyResolverConfigTO.DependencyType();
                    Map<String, DependencyResolverConfigTO.MimeType> mimeTypes = new HashMap<String, DependencyResolverConfigTO.MimeType>();
                    Element type = iterTypes.next();
                    String typeName = type.valueOf("name");
                    Element includes = type.element("includes");
                    Iterator<Element> iterPathPatterns = includes.elementIterator("path-pattern");
                    List<String> pathPatterns = new ArrayList<String>();
                    while (iterPathPatterns.hasNext()) {
                        Element pathPattern = iterPathPatterns.next();
                        String pathPatternValue = pathPattern.getStringValue();
                        pathPatterns.add(pathPatternValue);
                    }
                    dependencyType.setIncludePaths(pathPatterns);
                    Element mimetypes = type.element("mimetypes");
                    Iterator<Element> iterMimetype = mimetypes.elementIterator("mimetype");
                    while (iterMimetype.hasNext()) {
                        Element mimetype = iterMimetype.next();
                        DependencyResolverConfigTO.MimeType mimeType = new DependencyResolverConfigTO.MimeType();
                        List<String> patterns = new ArrayList<String>();
                        String mimeTypeName = mimetype.valueOf("name");
                        Element elPatterns = mimetype.element("patterns");
                        Iterator<Element> iterPatterns = elPatterns.elementIterator("pattern");
                        while (iterPatterns.hasNext()) {
                            Element pattern = iterPatterns.next();
                            patterns.add(pattern.getStringValue());
                        }
                        mimeType.setPatterns(patterns);
                        mimeTypes.put(mimeTypeName, mimeType);
                    }
                    dependencyType.setMimetypes(mimeTypes);
                    dependencyTypes.put(typeName, dependencyType);
                }
                config.setDependencyTypes(dependencyTypes);
            }
        }

        return config;
    }

    public String getConfigPath() {
        return studioConfiguration.getProperty(CONFIGURATION_SITE_DEPENDENCY_RESOLVER_CONFIG_BASE_PATH);
    }

    public String getConfigFileName() {
        return studioConfiguration.getProperty(CONFIGURATION_SITE_DEPENDENCY_RESOLVER_CONFIG_FILE_NAME);
    }

    public ContentService getContentService() { return contentService; }
    public void setContentService(ContentService contentService) { this.contentService = contentService; }

    public StudioConfiguration getStudioConfiguration() { return studioConfiguration; }
    public void setStudioConfiguration(StudioConfiguration studioConfiguration) { this.studioConfiguration = studioConfiguration; }
}
