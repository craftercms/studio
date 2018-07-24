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

package org.craftercms.studio.impl.v1.service.dependency;

import org.apache.commons.collections4.CollectionUtils;
import org.craftercms.studio.api.v1.constant.StudioConstants;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.service.dependency.DependencyResolver;
import org.craftercms.studio.api.v1.to.DependencyResolverConfigTO;
import org.craftercms.studio.api.v1.util.StudioConfiguration;
import org.craftercms.studio.impl.v1.util.ContentUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.craftercms.studio.api.v1.constant.StudioConstants.FILE_SEPARATOR;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.
        CONFIGURATION_SITE_DEPENDENCY_RESOLVER_CONFIG_BASE_PATH;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.
        CONFIGURATION_SITE_DEPENDENCY_RESOLVER_CONFIG_FILE_NAME;

public class RegexDependencyResolver implements DependencyResolver {

    private static final Logger logger = LoggerFactory.getLogger(RegexDependencyResolver.class);

    private static final String PARENT_DEPENDENCY_TYPE = "parent";

    protected ContentService contentService;
    protected StudioConfiguration studioConfiguration;

    @Override
    public Map<String, Set<String>> resolve(String site, String path) {
        Map<String, Set<String>> toRet = new HashMap<String, Set<String>>();
        try {
            DependencyResolverConfigTO config = getConfiguraion(site);
            if (config != null) {
                String content = contentService.getContentAsString(site, path);
                if (content != null) {
                    Map<String, DependencyResolverConfigTO.ItemType> itemTypes = config.getItemTypes();
                    if (itemTypes != null) {
                        for (Map.Entry<String, DependencyResolverConfigTO.ItemType> entry : itemTypes.entrySet()) {
                            DependencyResolverConfigTO.ItemType itemType = entry.getValue();
                            List<String> includes = itemType.getIncludes();
                            if (ContentUtils.matchesPatterns(path, includes)) {
                                Map<String, DependencyResolverConfigTO.DependencyType> dependencyTypes =
                                        itemType.getDependencyTypes();
                                for (Map.Entry<String, DependencyResolverConfigTO.DependencyType> dependencyTypeEntry :
                                        dependencyTypes.entrySet()) {
                                    Set<String> extractedPaths = new HashSet<String>();
                                    DependencyResolverConfigTO.DependencyType dependencyType = dependencyTypeEntry.getValue();
                                    List<DependencyResolverConfigTO.DependencyExtractionPattern> extractionPatterns =
                                            dependencyType.getIncludes();
                                    for (DependencyResolverConfigTO.DependencyExtractionPattern extractionPattern :
                                            extractionPatterns) {
                                        Pattern pattern = Pattern.compile(extractionPattern.getFindRegex());
                                        Matcher matcher = pattern.matcher(content);
                                        while (matcher.find()) {
                                            String matchedPath = matcher.group();
                                            if (CollectionUtils.isNotEmpty(extractionPattern.getTransforms())) {
                                                for (DependencyResolverConfigTO.DependencyExtractionTransform transform :
                                                        extractionPattern.getTransforms()) {
                                                    Pattern find = Pattern.compile(transform.getMatch());
                                                    Matcher replaceMatcher = find.matcher(matchedPath);
                                                    matchedPath = replaceMatcher.replaceAll(transform.getReplace());
                                                }
                                            }
                                            if (contentService.contentExists(site, matchedPath)) {
                                                extractedPaths.add(matchedPath);
                                            } else {
                                                String message = "Found reference to " + matchedPath + " in content at " +
                                                        path + " but content does not exist in referenced path for site " +
                                                        site + ".\n"
                                                        + "Regular expression for extracting dependencies matched " +
                                                        "string, and after applying transformation rules to get value " +
                                                        "for dependency path, that dependency path was not found in" +
                                                        " site repository as a content.";
                                                logger.debug(message);
                                            }
                                        }
                                    }
                                    toRet.put(dependencyType.getName(), extractedPaths);
                                }
                            }
                        }
                    }
                } else {
                    logger.error("Failed to extract dependencies. Content not found for site: " + site + ", path: "
                            + path);
                }
            } else {
                String configLocation = getConfigLocation(site);
                logger.error("Failed to load Dependency Resolver configuration. Verify that configuration exists" +
                        " and it is valid XML file: " + configLocation);
            }
        } catch (Exception exc) {
            logger.error("Unexcpected error resolving dependencies for site: " + site + " path: " + path);
        }
        return toRet;
    }

    @SuppressWarnings("unchecked")
    private DependencyResolverConfigTO getConfiguraion(String site) {
        DependencyResolverConfigTO config = null;
        String configLocation = getConfigLocation(site);
        Document document = null;
        try {
            document = contentService.getContentAsDocument(site, configLocation);
        } catch (DocumentException e) {
            logger.error("Failed to load dependency resolver configuration from location: " + configLocation, e);
        }
        if (document != null) {
            Element root = document.getRootElement();
            config = new DependencyResolverConfigTO();

            Element itemTypesEl = root.element("item-types");
            if (itemTypesEl != null) {
                Map<String, DependencyResolverConfigTO.ItemType> itemTypes = new HashMap<String, DependencyResolverConfigTO.ItemType>();
                Iterator<Element> iterItemTypes = itemTypesEl.elementIterator("item-type");
                while (iterItemTypes.hasNext()) {
                    DependencyResolverConfigTO.ItemType itemType = new DependencyResolverConfigTO.ItemType();
                    List<String> itemTypeIncludes = new ArrayList<String>();
                    Map<String, DependencyResolverConfigTO.DependencyType> dependencyTypes = new HashMap<String, DependencyResolverConfigTO.DependencyType>();
                    Element itemTypeEl = iterItemTypes.next();
                    String typeName = itemTypeEl.valueOf("name");
                    Element includesIT = itemTypeEl.element("includes");
                    Iterator<Element> iterPathPatterns = includesIT.elementIterator("path-pattern");
                    while (iterPathPatterns.hasNext()) {
                        Element pathPattern = iterPathPatterns.next();
                        String pathPatternValue = pathPattern.getStringValue();
                        itemTypeIncludes.add(pathPatternValue);
                    }
                    itemType.setIncludes(itemTypeIncludes);
                    Element dependencyTypesEl = itemTypeEl.element("dependency-types");
                    Iterator<Element> iterDependencyTypes = dependencyTypesEl.elementIterator("dependency-type");
                    while (iterDependencyTypes.hasNext()) {
                        Element dependencyTypeEl = iterDependencyTypes.next();
                        DependencyResolverConfigTO.DependencyType dependencyType =
                                new DependencyResolverConfigTO.DependencyType();
                        List<DependencyResolverConfigTO.DependencyExtractionPattern> patterns = new ArrayList<DependencyResolverConfigTO.DependencyExtractionPattern>();
                        String dependencyTypeName = dependencyTypeEl.valueOf("name");
                        dependencyType.setName(dependencyTypeName);
                        Element dependencyTypeIncludesEl = dependencyTypeEl.element("includes");
                        Iterator<Element> iterDependencyTypeIncludes =
                                dependencyTypeIncludesEl.elementIterator("pattern");
                        while (iterDependencyTypeIncludes.hasNext()) {
                            DependencyResolverConfigTO.DependencyExtractionPattern pattern =
                                    new DependencyResolverConfigTO.DependencyExtractionPattern();
                            List<DependencyResolverConfigTO.DependencyExtractionTransform> transforms =
                                    new ArrayList<DependencyResolverConfigTO.DependencyExtractionTransform>();
                            Element patternEl = iterDependencyTypeIncludes.next();
                            Element findRegexEl = patternEl.element("find-regex");
                            pattern.setFindRegex(findRegexEl.getStringValue());
                            Element transformsEl = patternEl.element("transforms");
                            if (transformsEl != null) {
                                Iterator<Element> iterTransformEl = transformsEl.elementIterator("transform");
                                while (iterTransformEl.hasNext()) {
                                    Element transformEl = iterTransformEl.next();
                                    DependencyResolverConfigTO.DependencyExtractionTransform transform =
                                            new DependencyResolverConfigTO.DependencyExtractionTransform();
                                    Element matchEl = transformEl.element("match");
                                    Element replaceEl = transformEl.element("replace");
                                    transform.setMatch(matchEl.getStringValue());
                                    transform.setReplace(replaceEl.getStringValue());
                                    transforms.add(transform);
                                }
                            }
                            pattern.setTransforms(transforms);
                            patterns.add(pattern);
                        }
                        dependencyType.setIncludes(patterns);
                        dependencyTypes.put(dependencyTypeName, dependencyType);
                    }
                    itemType.setDependencyTypes(dependencyTypes);
                    itemTypes.put(typeName, itemType);
                }
                config.setItemTypes(itemTypes);
            }
        }

        return config;
    }

    private String getConfigLocation(String site) {
        String configLocation = getConfigPath().replaceFirst(StudioConstants.PATTERN_SITE, site);
        configLocation = configLocation + FILE_SEPARATOR + getConfigFileName();
        return configLocation;
    }

    public String getConfigPath() {
        return studioConfiguration.getProperty(CONFIGURATION_SITE_DEPENDENCY_RESOLVER_CONFIG_BASE_PATH);
    }

    public String getConfigFileName() {
        return studioConfiguration.getProperty(CONFIGURATION_SITE_DEPENDENCY_RESOLVER_CONFIG_FILE_NAME);
    }

    public ContentService getContentService() {
        return contentService;
    }

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    public StudioConfiguration getStudioConfiguration() {
        return studioConfiguration;
    }

    public void setStudioConfiguration(StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }
}
