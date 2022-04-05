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

package org.craftercms.studio.impl.v1.service.dependency;

import org.apache.commons.collections4.CollectionUtils;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.service.dependency.DependencyResolver;
import org.craftercms.studio.api.v1.to.DependencyResolverConfigTO;
import org.craftercms.studio.api.v2.service.config.ConfigurationService;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.impl.v1.util.ContentUtils;
import org.dom4j.Document;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.craftercms.studio.api.v1.constant.StudioConstants.FILE_SEPARATOR;
import static org.craftercms.studio.api.v1.constant.StudioConstants.MODULE_STUDIO;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_DEFAULT_DEPENDENCY_RESOLVER_CONFIG_BASE_PATH;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_DEFAULT_DEPENDENCY_RESOLVER_CONFIG_FILE_NAME;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_ENVIRONMENT_ACTIVE;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_SITE_DEPENDENCY_RESOLVER_CONFIG_FILE_NAME;

public class RegexDependencyResolver implements DependencyResolver {

    private static final Logger logger = LoggerFactory.getLogger(RegexDependencyResolver.class);

    protected ContentService contentService;
    protected StudioConfiguration studioConfiguration;
    protected ConfigurationService configurationService;

    @Override
    public Map<String, Set<String>> resolve(String site, String path) {
        Map<String, Set<String>> toRet = new HashMap<String, Set<String>>();
        try {
            logger.debug("Get dependency resolver configuration for site " + site);
            DependencyResolverConfigTO config = getConfiguration(site);
            if (config != null) {
                logger.debug("Determine item type of content for site: " + site + ", path: " + path);
                DependencyResolverConfigTO.ItemType itemType = getItemTypeResolverConfig(site, path, config);
                if (itemType != null) {
                    String content = contentService.getContentAsString(site, path);
                    if (content != null) {
                        Map<String, DependencyResolverConfigTO.DependencyType> dependencyTypes =
                                itemType.getDependencyTypes();
                        logger.debug("Get dependencies of content for site: " + site + ", path: " + path);
                        toRet = getDependencies(site, path, content, dependencyTypes);
                    } else {
                        logger.error("Failed to extract dependencies. " +
                                "No content or empty content found for site: " + site + ", path: " + path);
                    }
                } else {
                    logger.debug("No dependency extraction required for site: " + site + ", path: " + path);
                }
            } else {
                String configLocation = getConfigFileName();
                logger.error("Failed to load Dependency Resolver configuration. Verify that configuration exists" +
                        " and it is valid XML file: " + configLocation);
            }
        } catch (Exception exc) {
            logger.error("Unexpected error resolving dependencies for site: " + site + " path: " + path, exc);
        }
        return toRet;
    }

    private DependencyResolverConfigTO getConfiguration(String site) {
        DependencyResolverConfigTO config = null;
        logger.debug("Get configuration location for site " + site);
        String configLocation = getConfigFileName();
        String defaultConfigLocation = getDefaultConfigLocation();
        Document document = null;
        try {
            logger.debug("Load configuration as xml document from " + configLocation);
            document = configurationService.getConfigurationAsDocument(site, MODULE_STUDIO, configLocation,
                    studioConfiguration.getProperty(CONFIGURATION_ENVIRONMENT_ACTIVE));
            if (document == null) {
                logger.debug("Loading default dependency resolver configuration");
                document = configurationService.getGlobalConfigurationAsDocument(defaultConfigLocation);
            }
        } catch (ServiceLayerException e) {
            logger.error("Failed to load dependency resolver configuration from location: " + configLocation, e);
        }
        if (document != null) {
            Element root = document.getRootElement();
            config = new DependencyResolverConfigTO();

            Element itemTypesEl = root.element(XML_CONFIGURATION_ROOT_ELEMENT);
            if (itemTypesEl != null) {
                logger.debug("Load configuration according to XML structure");
                Map<String, DependencyResolverConfigTO.ItemType> itemTypes =
                        new HashMap<String, DependencyResolverConfigTO.ItemType>();
                Iterator<Element> iterItemTypes = itemTypesEl.elementIterator(XML_CONFIGURATION_ITEM_TYPE);
                logger.debug("Populate item types");
                while (iterItemTypes.hasNext()) {
                    DependencyResolverConfigTO.ItemType itemType = new DependencyResolverConfigTO.ItemType();
                    List<String> itemTypeIncludes = new ArrayList<String>();
                    Map<String, DependencyResolverConfigTO.DependencyType> dependencyTypes =
                            new HashMap<String, DependencyResolverConfigTO.DependencyType>();
                    Element itemTypeEl = iterItemTypes.next();
                    String typeName = itemTypeEl.valueOf(XML_CONFIGURATION_NAME);
                    Element includesIT = itemTypeEl.element(XML_CONFIGURATION_INCLUDES);
                    Iterator<Element> iterPathPatterns = includesIT.elementIterator(XML_CONFIGURATION_PATH_PATTERN);
                    while (iterPathPatterns.hasNext()) {
                        Element pathPattern = iterPathPatterns.next();
                        String pathPatternValue = pathPattern.getStringValue();
                        itemTypeIncludes.add(pathPatternValue);
                    }
                    itemType.setIncludes(itemTypeIncludes);


                    List<String> itemTypeExcludes = new ArrayList<String>();
                    Element excludesIT = itemTypeEl.element(XML_CONFIGURATION_EXCLUDES);
                    if (excludesIT != null) {
                        iterPathPatterns = excludesIT.elementIterator(XML_CONFIGURATION_PATH_PATTERN);
                        while (iterPathPatterns.hasNext()) {
                            Element pathPattern = iterPathPatterns.next();
                            String pathPatternValue = pathPattern.getStringValue();
                            itemTypeExcludes.add(pathPatternValue);
                        }
                        itemType.setExcludes(itemTypeExcludes);
                    }

                    Element dependencyTypesEl = itemTypeEl.element(XML_CONFIGURATION_DEPENDENCY_TYPES);
                    Iterator<Element> iterDependencyTypes = dependencyTypesEl.elementIterator(XML_CONFIGURATION_DEPENDENCY_TYPE);
                    logger.debug("Populate dependency types for " + typeName);
                    while (iterDependencyTypes.hasNext()) {
                        Element dependencyTypeEl = iterDependencyTypes.next();
                        DependencyResolverConfigTO.DependencyType dependencyType =
                                new DependencyResolverConfigTO.DependencyType();
                        List<DependencyResolverConfigTO.DependencyExtractionPattern> patterns =
                                new ArrayList<DependencyResolverConfigTO.DependencyExtractionPattern>();
                        String dependencyTypeName = dependencyTypeEl.valueOf(XML_CONFIGURATION_NAME);
                        dependencyType.setName(dependencyTypeName);
                        Element dependencyTypeIncludesEl = dependencyTypeEl.element(XML_CONFIGURATION_INCLUDES);
                        Iterator<Element> iterDependencyTypeIncludes =
                                dependencyTypeIncludesEl.elementIterator(XML_CONFIGURATION_PATTERN);
                        while (iterDependencyTypeIncludes.hasNext()) {
                            DependencyResolverConfigTO.DependencyExtractionPattern pattern =
                                    new DependencyResolverConfigTO.DependencyExtractionPattern();
                            List<DependencyResolverConfigTO.DependencyExtractionTransform> transforms =
                                    new ArrayList<DependencyResolverConfigTO.DependencyExtractionTransform>();
                            Element patternEl = iterDependencyTypeIncludes.next();
                            Element findRegexEl = patternEl.element(XML_CONFIGURATION_FIND_REGEX);
                            pattern.setFindRegex(findRegexEl.getStringValue());
                            Element transformsEl = patternEl.element(XML_CONFIGURATION_TRANSFORMS);
                            if (transformsEl != null) {
                                Iterator<Element> iterTransformEl = transformsEl.elementIterator(XML_CONFIGURATION_TRANSFORM);
                                while (iterTransformEl.hasNext()) {
                                    Element transformEl = iterTransformEl.next();
                                    DependencyResolverConfigTO.DependencyExtractionTransform transform =
                                            new DependencyResolverConfigTO.DependencyExtractionTransform();
                                    Element matchEl = transformEl.element(XML_CONFIGURATION_MATCH);
                                    Element replaceEl = transformEl.element(XML_CONFIGURATION_REPLACE);
                                    Element splitEl = transformEl.element(XML_CONFIGURATION_SPLIT);
                                    transform.setMatch(matchEl.getStringValue());
                                    transform.setReplace(replaceEl.getStringValue());
                                    if (splitEl != null) {
                                        transform.setSplit(Boolean.parseBoolean(splitEl.getStringValue()));

                                        transform.setDelimiter(splitEl.attributeValue("delimiter", ","));
                                    }
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
        } else {
            logger.warn("Dependency resolver XML configuration for site " + site + " does not exist at " + configLocation);
        }

        return config;
    }

    private DependencyResolverConfigTO.ItemType getItemTypeResolverConfig(String site, String path,
                                                                          DependencyResolverConfigTO config) {
        logger.debug("Loop through all item types to match path against include patterns");
        Map<String, DependencyResolverConfigTO.ItemType> itemTypes = config.getItemTypes();
        DependencyResolverConfigTO.ItemType itemType = null;
        if (itemTypes != null) {
            for (Map.Entry<String, DependencyResolverConfigTO.ItemType> entry : itemTypes.entrySet()) {
                DependencyResolverConfigTO.ItemType it = entry.getValue();
                List<String> includes = it.getIncludes();
                List<String> excludes = it.getExcludes();
                if (ContentUtils.matchesPatterns(path, includes) && !ContentUtils.matchesPatterns(path, excludes)) {
                    itemType = it;
                    break;
                }
            }
        }
        return itemType;
    }

    private Map<String, Set<String>> getDependencies(String site, String path, String content, Map<String,
            DependencyResolverConfigTO.DependencyType> dependencyTypes) {
        Map<String, Set<String>> toRet = new HashMap<String, Set<String>>();
        logger.debug("Loop through all dependency types");
        for (Map.Entry<String, DependencyResolverConfigTO.DependencyType> dependencyTypeEntry :
                dependencyTypes.entrySet()) {
            Set<String> extractedPaths = new HashSet<String>();
            DependencyResolverConfigTO.DependencyType dependencyType = dependencyTypeEntry.getValue();
            List<DependencyResolverConfigTO.DependencyExtractionPattern> extractionPatterns =
                    dependencyType.getIncludes();
            logger.debug("Loop through all extraction patterns for " + dependencyTypeEntry.getKey());
            for (DependencyResolverConfigTO.DependencyExtractionPattern extractionPattern :
                    extractionPatterns) {
                Pattern pattern = Pattern.compile(extractionPattern.getFindRegex());
                Matcher matcher = pattern.matcher(content);
                logger.debug("Matching content against regular expression " + extractionPattern.getFindRegex());
                while (matcher.find()) {
                    String matchedValue = matcher.group();
                    List<String> matchedPaths = new LinkedList<>();
                    logger.debug("Matched path: " + matchedValue + ". Apply transformations");
                    if (CollectionUtils.isNotEmpty(extractionPattern.getTransforms())) {
                        for (DependencyResolverConfigTO.DependencyExtractionTransform transform :
                                extractionPattern.getTransforms()) {
                            Pattern find = Pattern.compile(transform.getMatch());
                            Matcher replaceMatcher = find.matcher(matchedValue);

                            if (transform.isSplit()) {
                                if (replaceMatcher.matches()) {
                                    matchedValue = replaceMatcher.group(1);
                                    String[] splitValues = matchedValue.split(transform.getDelimiter());

                                    List<String> transformedValues = Stream.of(splitValues)
                                            // simulate a regex to be able to apply the replace from the config
                                            .map(v -> {
                                                Pattern p = Pattern.compile("(" + v + ")");
                                                Matcher m = p.matcher(v);
                                                return m.replaceAll(transform.getReplace());
                                            })
                                            .collect(Collectors.toList());

                                    matchedPaths.addAll(transformedValues);
                                }
                            } else {
                                matchedValue = replaceMatcher.replaceAll(transform.getReplace());
                                matchedPaths.add(matchedValue);
                            }
                        }
                    } else {
                        matchedPaths.add(matchedValue);
                    }
                    for(String matchedPath : matchedPaths) {
                        if (contentService.shallowContentExists(site, matchedPath)) {
                            logger.debug("Content exists for matched path " + matchedPath + ". Adding to the result set");
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
            }
            toRet.put(dependencyType.getName(), extractedPaths);
        }
        return toRet;
    }

    private String getDefaultConfigLocation() {
        return getDefaultConfigPath() + FILE_SEPARATOR + getDefaultConfigFileName();
    }


    public String getConfigFileName() {
        return studioConfiguration.getProperty(CONFIGURATION_SITE_DEPENDENCY_RESOLVER_CONFIG_FILE_NAME);
    }

    public String getDefaultConfigPath() {
        return studioConfiguration.getProperty(CONFIGURATION_DEFAULT_DEPENDENCY_RESOLVER_CONFIG_BASE_PATH);
    }

    public String getDefaultConfigFileName() {
        return studioConfiguration.getProperty(CONFIGURATION_DEFAULT_DEPENDENCY_RESOLVER_CONFIG_FILE_NAME);
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

    public ConfigurationService getConfigurationService() {
        return configurationService;
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }
}
