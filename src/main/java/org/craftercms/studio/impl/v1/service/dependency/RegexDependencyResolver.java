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
import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.service.dependency.DependencyResolver;
import org.craftercms.studio.api.v1.to.DependencyResolverConfigTO;
import org.craftercms.studio.impl.v1.util.ContentUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.craftercms.studio.api.v1.constant.DmConstants.INDEX_FILE;

public class RegexDependencyResolver implements DependencyResolver {

    private final static Logger logger = LoggerFactory.getLogger(RegexDependencyResolver.class);

    String PATTERN_SITE = "\\{site\\}";
    String FILE_SEPARATOR = "/";
    private static final String PARENT_DEPENDENCY_TYPE = "parent";

    String DEFAULT_RESOLVER_CONFIG_PATH = "crafter/studio/dependency/resolver-config.xml";

    protected ContentService contentService;
    protected String configPath;
    protected String configFileName;

    @Override
    public Map<String, Set<String>> resolve(String site, String path) {
        Map<String, Set<String>> toRet = new HashMap<String, Set<String>>();
        DependencyResolverConfigTO config = getConfiguration(site);
        if (config != null) {
            String content = contentService.getContentAsString(contentService.expandRelativeSitePath(site, path));
            if (content != null) {
                Map<String, DependencyResolverConfigTO.ItemType> itemTypes = config.getItemTypes();
                if (itemTypes != null) {
                    for (Map.Entry<String, DependencyResolverConfigTO.ItemType> entry : itemTypes.entrySet()) {
                        DependencyResolverConfigTO.ItemType itemType = entry.getValue();
                        List<String> includes = itemType.getIncludes();
                        if (ContentUtils.matchesPatterns(path, includes)) {
                            Map<String, DependencyResolverConfigTO.DependencyType> dependencyTypes = itemType.getDependencyTypes();
                            for (Map.Entry<String, DependencyResolverConfigTO.DependencyType> dependencyTypeEntry : dependencyTypes.entrySet()) {
                                Set<String> extractedPaths = new HashSet<String>();
                                DependencyResolverConfigTO.DependencyType dependencyType = dependencyTypeEntry.getValue();
                                List<DependencyResolverConfigTO.DependencyExtractionPattern> extractionPatterns = dependencyType.getIncludes();
                                for (DependencyResolverConfigTO.DependencyExtractionPattern extractionPattern : extractionPatterns) {
                                    Pattern pattern = Pattern.compile(extractionPattern.getFindRegex());
                                    Matcher matcher = pattern.matcher(content);
                                    while (matcher.find()) {
                                        String matchedPath = matcher.group();
                                        if (CollectionUtils.isNotEmpty(extractionPattern.getTransforms())) {
                                            for (DependencyResolverConfigTO.DependencyExtractionTransform transform : extractionPattern.getTransforms()) {
                                                Pattern find = Pattern.compile(transform.getMatch());
                                                Matcher replaceMatcher = find.matcher(matchedPath);
                                                matchedPath = replaceMatcher.replaceAll(transform.getReplace());
                                            }
                                        }
                                        if (contentService.contentExists(site, matchedPath)) {
                                            extractedPaths.add(matchedPath);
                                        } else {
                                            String message = "Found reference to " + matchedPath + " in content at " + path + " but content does not exist in referenced path for site " + site + ".\n"
                                                    + "Regular expression for extracting dependencies matched string, and after applying transformation rules to get value for dependency path, that dependency path was not found in site repository as a content.";
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
                logger.error("Failed to extract dependencies. Content not found for site: " + site + ", path: " + path);
            }
        } else {
            String configLocation = getConfigLocation(site);
            logger.error("Failed to load Dependency Resolver configuration. Verify that configuration exists and it is valid XML file: " + configLocation);
        }
        String parentDependencyPath = getParentDependency(site, path);
        if (StringUtils.isNotEmpty(parentDependencyPath)) {
            Set<String> parentDeps = new HashSet<String>();
            parentDeps.add(parentDependencyPath);
            toRet.put(PARENT_DEPENDENCY_TYPE, parentDeps);
        }
        return toRet;
    }

    @SuppressWarnings("unchecked")
    private DependencyResolverConfigTO getConfiguration(String site) {
        DependencyResolverConfigTO config = null;
        String configLocation = getConfigLocation(site);
        Document document = null;
        try {
            document = contentService.getContentAsDocument(configLocation);
        } catch (DocumentException e) {
            logger.error("Failed to load dependency resolver configuration from location: " + configLocation, e);
        }
        if (document == null) {
            logger.info("Loading default dependency resolver configuration");
            document = getConfigFromResources();
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
                        DependencyResolverConfigTO.DependencyType dependencyType = new DependencyResolverConfigTO.DependencyType();
                        List<DependencyResolverConfigTO.DependencyExtractionPattern> patterns = new ArrayList<DependencyResolverConfigTO.DependencyExtractionPattern>();
                        String dependencyTypeName = dependencyTypeEl.valueOf("name");
                        dependencyType.setName(dependencyTypeName);
                        Element dependencyTypeIncludesEl = dependencyTypeEl.element("includes");
                        Iterator<Element> iterDependencyTypeIncludes = dependencyTypeIncludesEl.elementIterator("pattern");
                        while (iterDependencyTypeIncludes.hasNext()) {
                            DependencyResolverConfigTO.DependencyExtractionPattern pattern = new DependencyResolverConfigTO.DependencyExtractionPattern();
                            List<DependencyResolverConfigTO.DependencyExtractionTransform> transforms = new ArrayList<DependencyResolverConfigTO.DependencyExtractionTransform>();
                            Element patternEl = iterDependencyTypeIncludes.next();
                            Element findRegexEl = patternEl.element("find-regex");
                            pattern.setFindRegex(findRegexEl.getStringValue());
                            Element transformsEl = patternEl.element("transforms");
                            if (transformsEl != null) {
                                Iterator<Element> iterTransformEl = transformsEl.elementIterator("transform");
                                while (iterTransformEl.hasNext()) {
                                    Element transformEl = iterTransformEl.next();
                                    DependencyResolverConfigTO.DependencyExtractionTransform transform = new DependencyResolverConfigTO.DependencyExtractionTransform();
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

    private String getParentDependency(String site, String path) {
        int idx = path.lastIndexOf(FILE_SEPARATOR + INDEX_FILE);
        String aPath = path;
        if (idx > 0) {
            aPath = path.substring(0, idx);
        }
        logger.debug("Calculate parent url for " + aPath);
        String parentPath = ContentUtils.getParentUrl(aPath);
        if (StringUtils.isNotEmpty(parentPath)) {
            String parentIndexPath = parentPath + FILE_SEPARATOR + INDEX_FILE;
            if (contentService.contentExists(site, parentIndexPath)) {
                return parentIndexPath;
            } else {
                return parentPath;
            }
        }
        return StringUtils.EMPTY;
    }

    private String getConfigLocation(String site) {
        String configLocation = getConfigPath().replaceFirst(PATTERN_SITE, site);
        configLocation = configLocation + FILE_SEPARATOR + getConfigFileName();
        return configLocation;
    }

    private Document getConfigFromResources() {
        Document retDocument = null;
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(DEFAULT_RESOLVER_CONFIG_PATH);
        if(is != null) {
            try {
                SAXReader saxReader = new SAXReader();
                try {
                    saxReader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
                    saxReader.setFeature("http://xml.org/sax/features/external-general-entities", false);
                    saxReader.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
                }catch (SAXException ex){
                    logger.error("Unable to turn off external entity loading, This could be a security risk.", ex);
                }
                try {
                    retDocument = saxReader.read(is);
                } catch (DocumentException e) {
                    logger.error("Failed to read default dependency resolver configuration from classpath: " + DEFAULT_RESOLVER_CONFIG_PATH);
                }
            }
            finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                }
                catch (IOException err) {
                    logger.error("Faild to close config resource {0}", err, DEFAULT_RESOLVER_CONFIG_PATH);
                }
            }
        }
        return retDocument;
    }

    public String getConfigPath() { return configPath; }
    public void setConfigPath(String configPath) { this.configPath = configPath; }

    public String getConfigFileName() { return configFileName; }
    public void setConfigFileName(String configFileName) { this.configFileName = configFileName; }

    public ContentService getContentService() { return contentService; }
    public void setContentService(ContentService contentService) { this.contentService = contentService; }
}
