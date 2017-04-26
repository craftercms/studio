/*
 * Crafter Studio Web-content authoring solution
 * Copyright (C) 2007-2016 Crafter Software Corporation.
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

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import org.apache.commons.lang.StringUtils;
import org.craftercms.studio.api.v1.constant.StudioConstants;
import org.craftercms.studio.api.v1.constant.DmConstants;
import org.craftercms.studio.api.v1.constant.DmXmlConstants;
import org.craftercms.studio.api.v1.dal.DependencyEntity;
import org.craftercms.studio.api.v1.dal.DependencyMapper;
import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.exception.ServiceException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.AbstractRegistrableService;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.service.dependency.DmDependencyService;
import org.craftercms.studio.api.v1.to.ContentItemTO;
import org.craftercms.studio.api.v1.to.CopyDependencyConfigTO;
import org.craftercms.studio.api.v1.to.DeleteDependencyConfigTO;
import org.craftercms.studio.api.v1.to.DmDependencyTO;
import org.craftercms.studio.api.v1.util.DmContentItemComparator;
import org.craftercms.studio.api.v1.util.StudioConfiguration;
import org.craftercms.studio.impl.v1.util.ContentFormatUtils;
import org.craftercms.studio.impl.v1.util.ContentUtils;
import org.craftercms.studio.impl.v1.util.XmlUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.craftercms.studio.api.v1.util.StudioConfiguration.DEPENDENCIES_IGNORE_DEPENDENCIES_RULES;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.DEPENDENCIES_MANUAL_DEPENDENCY_APPROVING_ENABLED;

public class DmDependencyServiceImpl extends AbstractRegistrableService implements DmDependencyService {

    private static final Logger logger = LoggerFactory.getLogger(DmDependencyServiceImpl.class);

    protected static final String JSON_KEY_ITEMS = "items";
    protected static final String JSON_KEY_SUBMISSION_COMMENT = "submissionComment";
    protected static final String JSON_KEY_DEPENDENCIES = "dependencies";


    @Override
    public void register() {
        getServicesManager().registerService(DmDependencyService.class, this);
    }

    /**
     * get dependent items from the given list of files
     *
     * @param site
     * @param parentUri
     * @param dependencies
     * @param populateUpdatedDependecinesOnly
     * @return
     */
    protected List<ContentItemTO> getDependentItems(String site, String parentUri, List<DependencyEntity> dependencies, boolean populateUpdatedDependecinesOnly) {
        List<ContentItemTO> items = null;
        if (dependencies != null) {
            items = new ArrayList<>(dependencies.size());
            for (DependencyEntity dependency : dependencies) {
                String path = dependency.getTargetPath();
                ContentItemTO dependencyItem = null;
                dependencyItem = contentService.getContentItem(site, path);

                if (dependencyItem != null) {
                    dependencyItem.setReference(true);
                    dependencyItem.setMandatoryParent(parentUri);
                    items.add(dependencyItem);
                }
            }
        } else {
            items = new ArrayList<>(0);
        }
        return items;
    }

    public Set<ContentItemTO> getDependantItems(String site, String path){
        Set<ContentItemTO> dependents = new HashSet<>();
        List<String> rawDependentItems = getDependantPaths(site, path);
        for (String dependentItem : rawDependentItems) {
            dependents.add(contentService.getContentItem(dependentItem));
        }
        // Make sure its a read only list.
        return Collections.unmodifiableSet(dependents);
    }

    public Set<ContentItemTO> getDependenciesItems(String site, String path){
        Set<ContentItemTO> dependents = new HashSet<>();
        List<String> rawDependentItems = getDependencyPaths(site, path);
        for (String dependentItem : rawDependentItems) {
            dependents.add(contentService.getContentItem(dependentItem));
        }
        // Make sure its a read only list.
        return Collections.unmodifiableSet(dependents);
    }

    @Override
    public Map<String, Object> getDependencies(String site, String request, Boolean deleteDependencies) throws ServiceException {

        if(deleteDependencies == null)
            deleteDependencies=false;
        try {

            List<ContentItemTO> items = null;
            JSONArray jsonArray = (JSONArray)JSONSerializer.toJSON(request);
            if (jsonArray != null && jsonArray.size() > 0) {
                List<String> submittedItems = new ArrayList<>(jsonArray.size());
                ListIterator<JSONObject> iterator = jsonArray.listIterator();
                while (iterator.hasNext()) {
                    JSONObject jsonObject = iterator.next();
                    String uri = jsonObject.getString("uri");
                    submittedItems.add(uri);
                }
                DmContentItemComparator comparator = new DmContentItemComparator(DmContentItemComparator.SORT_BROWSER_URI, true, true, true);
                if (isManualDependencyApprovingEnabled()) {
                    items = getDependenciesManualApproving(site, submittedItems, comparator, false, deleteDependencies);
                } else {
                    items = getDependencies(site, submittedItems, comparator, false, deleteDependencies);
                }
            }
            StringBuilder sb = new StringBuilder();
            Set<String> submissionComments = new HashSet<String>();
            Set<String> dependenciesFiles = new HashSet<String>();
            for (ContentItemTO item : items) {
                String comment = item.getSubmissionComment();
                if (StringUtils.isNotEmpty(comment)) {
                    if (!submissionComments.contains(comment)) {
                        sb.append(comment).append("\n");
                        submissionComments.add(comment);
                    }
                }
                dependenciesFiles.addAll(submitToApproveDependencyRule.applyRule(site, item.getUri()));
            }
            Map<String, Object> result = new HashMap<>();
            result.put(JSON_KEY_ITEMS, items);
            result.put(JSON_KEY_SUBMISSION_COMMENT, sb.toString());
            result.put(JSON_KEY_DEPENDENCIES, dependenciesFiles);
            return result;
        } catch (RuntimeException e){
            logger.error("Error getting dependecies",e);
            throw e;
        } catch (ServiceException e) {
            logger.error("Error getting dependecies",e);
            throw e;
        }
    }

    @Override
    public DmDependencyTO getDependenciesNoCalc(String site, String path, boolean populateUpdatedDependenciesOnly, boolean recursive, Set<String> processedDependencies) {
        List<String> paths = new ArrayList<String>();
        paths.add(path);

        List<DmDependencyTO> items = getDependencyItemsNoCalc(site, paths, processedDependencies, populateUpdatedDependenciesOnly, recursive);
        if (items.size() > 0) {
            return items.get(0);
        } else {
            return null;
        }
    }

    protected List<DmDependencyTO> getDependencyItemsNoCalc(String site, List<String> paths, Set<String> processedDependencies, boolean populateUpdatedDependenciesOnly, boolean recursive) {
        List<DmDependencyTO> items = new ArrayList<DmDependencyTO>(paths.size());
        if (processedDependencies == null) {
            processedDependencies = new HashSet<String>();
        }
        for (String path : paths) {
            if (processedDependencies.contains(path)) {
                continue;
            }
            processedDependencies.add(path);
            DmDependencyTO item = new DmDependencyTO();
            item.setUri(path);
            if (recursive) {
                if (ContentUtils.matchesPatterns(path, servicesConfig.getPagePatterns(site))
                        || ContentUtils.matchesPatterns(path, servicesConfig.getComponentPatterns(site))) {
                    /*try {
                        Document document = this.getDocument(site, null, path);
                        if (document == null) {
                            return items;
                        }

                        // Check for skipDependencies flag
                        Element root = document.getRootElement();
                        boolean skipDependencies = false;
                        String isSkipDependenciesValue = root.valueOf("//" + DmXmlConstants.ELM_SKIP_DEPENDENCIES);
                        if (isSkipDependenciesValue != null && !"".equals(isSkipDependenciesValue)) {
                            skipDependencies = ContentFormatUtils.getBooleanValue(isSkipDependenciesValue);
                        }
                        if (skipDependencies) {
                            return items;
                        }
                    } catch (ContentNotFoundException e) {
                        logger.error("Error while getting dependent file names for " + path + " in " + site, e);
                    }*/
                }

                String cleanPath = getCleanPath(path);
                Map<String, Object> params = new HashMap<>();
                params.put("site", site);
                params.put("sourcePath", cleanPath);
                params.put("type", DEPENDENCY_NAME_ASSET);
                List<DependencyEntity> assetsList = dependencyMapper.getDependenciesByType(params);
                List<String> assetPaths = new ArrayList<String>();
                for (DependencyEntity assetEntity : assetsList) {
                    assetPaths.add(assetEntity.getTargetPath());
                }
                List<DmDependencyTO> assets = getDependencyItemsNoCalc(site, assetPaths, processedDependencies, populateUpdatedDependenciesOnly, recursive);
                item.setAssets(assets);

                params = new HashMap<>();
                params.put("site", site);
                params.put("sourcePath", cleanPath);
                params.put("type", DEPENDENCY_NAME_COMPONENT);
                List<DependencyEntity> componentsList = dependencyMapper.getDependenciesByType(params);
                List<String> componentsPaths = new ArrayList<String>();
                for (DependencyEntity componentEntity : componentsList) {
                    componentsPaths.add(componentEntity.getTargetPath());
                }
                List<DmDependencyTO> components = getDependencyItemsNoCalc(site, componentsPaths, processedDependencies, populateUpdatedDependenciesOnly, recursive);
                item.setComponents(components);

                params = new HashMap<>();
                params.put("site", site);
                params.put("sourcePath", cleanPath);
                params.put("type", DEPENDENCY_NAME_DOCUMENT);
                List<DependencyEntity> documentsList = dependencyMapper.getDependenciesByType(params);
                List<String> documentsPaths = new ArrayList<String>();
                for (DependencyEntity documentEntity : documentsList) {
                    documentsPaths.add(documentEntity.getTargetPath());
                }
                List<DmDependencyTO> documents = getDependencyItemsNoCalc(site, documentsPaths, processedDependencies, populateUpdatedDependenciesOnly, recursive);
                item.setDocuments(documents);

                params = new HashMap<>();
                params.put("site", site);
                params.put("sourcePath", cleanPath);
                params.put("type", DEPENDENCY_NAME_RENDERING_TEMPLATE);
                List<DependencyEntity> templatesList = dependencyMapper.getDependenciesByType(params);
                List<String> templatesPaths = new ArrayList<String>();
                for (DependencyEntity templateEntity : templatesList) {
                    templatesPaths.add(templateEntity.getTargetPath());
                }
                List<DmDependencyTO> templates = getDependencyItemsNoCalc(site, templatesPaths, processedDependencies, populateUpdatedDependenciesOnly, recursive);
                item.setRenderingTemplates(templates);

                /**
                 * get Page dependency as well
                 */
                params = new HashMap<>();
                params.put("site", site);
                params.put("sourcePath", cleanPath);
                params.put("type", DEPENDENCY_NAME_PAGE);
                List<DependencyEntity> pagesList = dependencyMapper.getDependenciesByType(params);
                List<String> pagesPaths = new ArrayList<String>();
                for (DependencyEntity pageEntity : pagesList) {
                    pagesPaths.add(pageEntity.getTargetPath());
                }
                List<DmDependencyTO> pages = getDependencyItemsNoCalc(site, pagesPaths, processedDependencies, populateUpdatedDependenciesOnly, recursive);
                item.setPages(pages);

            }
            items.add(item);
        }
        return items;
    }

    @Override
    public DmDependencyTO getDependencies(String site, String path, boolean populateUpdatedDependecinesOnly, boolean recursive) {
        List<String> paths = new ArrayList<>(1);
        paths.add(path);
        Set<String> processedDependencies = new HashSet<>();
        List<DmDependencyTO> items = getDependencyItems(site, paths, processedDependencies ,populateUpdatedDependecinesOnly, recursive, false);
        if (items.size() > 0) {
            return items.get(0);
        } else {
            return null;
        }
    }

    /**
     * get dependency items given multiple content paths
     *
     * @param site
     * @param paths
     * @param populateUpdatedDependecinesOnly
     * @param recursive
     * @return dependency items
     */
    protected List<DmDependencyTO> getDependencyItems(String site, List<String> paths, Set<String> processedDependencies, boolean populateUpdatedDependecinesOnly, boolean recursive, boolean isDraftContent) {
        List<DmDependencyTO> items = new ArrayList<>(paths.size());
        for (String path : paths) {
            if (processedDependencies.contains(path)) {
                continue;
            }
            processedDependencies.add(path);
            DmDependencyTO item = new DmDependencyTO();
            item.setUri(path);
            if (recursive
                    && (ContentUtils.matchesPatterns(path, servicesConfig.getPagePatterns(site))
                    || ContentUtils.matchesPatterns(path, servicesConfig.getComponentPatterns(site)))) {
                try {
                	Document document = this.loadDocument(site, path);
                    if (document == null) {
                        return items;
                    }

                    // Check for skipDependencies flag
                    Element root = document.getRootElement();
                    boolean skipDependencies = false;
                    String isSkipDependenciesValue = root.valueOf("//" + DmXmlConstants.ELM_SKIP_DEPENDENCIES);
                    if (isSkipDependenciesValue != null && !"".equals(isSkipDependenciesValue)) {
                        skipDependencies = ContentFormatUtils.getBooleanValue(isSkipDependenciesValue);
                    }
                    if (skipDependencies) {
                        return items;
                    }

                    StringBuffer buffer = new StringBuffer(XmlUtils.convertDocumentToString(document));
                    List<String> assets = getDependentFileNames(site, buffer, populateUpdatedDependecinesOnly, servicesConfig.getAssetPatterns(site));
                    List<DmDependencyTO> assetItems = getDependencyItems(site, assets, processedDependencies, populateUpdatedDependecinesOnly, false, false);
                    item.setAssets(assetItems);
                    List<String> components = getDependentFileNames(site, buffer, populateUpdatedDependecinesOnly, servicesConfig.getComponentPatterns(site));
                    List<DmDependencyTO> compItems = getDependencyItems(site, components, processedDependencies, populateUpdatedDependecinesOnly, recursive, true);
                    item.setComponents(compItems);
                    List<String> documents = getDependentFileNames(site, buffer, populateUpdatedDependecinesOnly, servicesConfig.getDocumentPatterns(site));
                    List<DmDependencyTO> docItems = getDependencyItems(site, documents, processedDependencies, populateUpdatedDependecinesOnly, recursive, false);
                    item.setDocuments(docItems);
                    List<String> templates = getDependentFileNames(site, buffer, populateUpdatedDependecinesOnly, servicesConfig.getRenderingTemplatePatterns(site));
                    List<DmDependencyTO> templateItems = getDependencyItems(site, templates, processedDependencies, populateUpdatedDependecinesOnly, recursive, false);
                    item.setRenderingTemplates(templateItems);
                    /*
                    List<String> levelDescriptors = getDependentLevelDescriptors(site, path, populateUpdatedDependecinesOnly, _servicesConfig.getLevelDescriptorName(site));
                    List<DmDependencyTO> levelDescriptorItems = getDependencyItems(site, sub,sandbox, levelDescriptors, populateUpdatedDependecinesOnly, recursive);
                    item.setLevelDescriptors(levelDescriptorItems);*/

                    /**
                     * get Page dependency as well
                     */
                    List<String> pages = getDependentFileNames(site, buffer, populateUpdatedDependecinesOnly, servicesConfig.getPagePatterns(site));
                    List<DmDependencyTO> pageItems = getDependencyItems(site, pages, processedDependencies, populateUpdatedDependecinesOnly, recursive, false);
                    item.setPages(pageItems);

                } catch (ContentNotFoundException e) {
                    logger.error("Error while getting dependent file names for " + path + " in " + site, e);
                } catch (IOException e) {
                    logger.error("Error while getting dependent file names for " + path + " in " + site, e);
                }
            } else if (false /*recursive*/) {
                boolean isCss = path.endsWith(DmConstants.CSS_PATTERN);
                boolean isJs = path.endsWith(DmConstants.JS_PATTERN);
                List<String> templatePatterns = servicesConfig.getRenderingTemplatePatterns(site);
                boolean isTemplate = false;
                for (String templatePattern : templatePatterns) {
                    Pattern pattern = Pattern.compile(templatePattern);
                    Matcher matcher = pattern.matcher(path);
                    if (matcher.matches()) {
                        isTemplate = true;
                        break;
                    }
                }
                if (isCss || isJs || isTemplate) {
                    try {
                        InputStream is = contentService.getContent(site, path);
                        //is.reset();
                        int size = is.available();
                        char[] theChars = new char[size];
                        byte[] bytes    = new byte[size];

                        is.read(bytes, 0, size);
                        for (int i = 0; i < size;) {
                            theChars[i] = (char)(bytes[i++]&0xff);
                        }

                        StringBuffer buffer = new StringBuffer(new String(theChars));
                        List<String> assets = getDependentFileNames(site, buffer, populateUpdatedDependecinesOnly, servicesConfig.getAssetPatterns(site));
                        List<DmDependencyTO> assetItems = getDependencyItems(site, assets, processedDependencies, populateUpdatedDependecinesOnly, false, false);
                        item.setAssets(assetItems);
                        List<String> components = getDependentFileNames(site, buffer, populateUpdatedDependecinesOnly, servicesConfig.getComponentPatterns(site));
                        List<DmDependencyTO> compItems = getDependencyItems(site, components, processedDependencies, populateUpdatedDependecinesOnly, recursive, true);
                        item.setComponents(compItems);
                        List<String> documents = getDependentFileNames(site, buffer, populateUpdatedDependecinesOnly, servicesConfig.getDocumentPatterns(site));
                        List<DmDependencyTO> docItems = getDependencyItems(site, documents, processedDependencies, populateUpdatedDependecinesOnly, recursive, false);
                        item.setDocuments(docItems);
                        List<String> templates = getDependentFileNames(site, buffer, populateUpdatedDependecinesOnly, servicesConfig.getRenderingTemplatePatterns(site));
                        List<DmDependencyTO> templateItems = getDependencyItems(site, templates, processedDependencies, populateUpdatedDependecinesOnly, recursive, false);
                        item.setRenderingTemplates(templateItems);
                        /*
                        List<String> levelDescriptors = getDependentLevelDescriptors(site, path, populateUpdatedDependecinesOnly, _servicesConfig.getLevelDescriptorName(site));
                        List<DmDependencyTO> levelDescriptorItems = getDependencyItems(site, sub,sandbox, levelDescriptors, populateUpdatedDependecinesOnly, recursive);
                        item.setLevelDescriptors(levelDescriptorItems);*/

                        /**
                         * get Page dependency as well
                         */
                        List<String> pages = getDependentFileNames(site, buffer, populateUpdatedDependecinesOnly, servicesConfig.getPagePatterns(site));
                        List<DmDependencyTO> pageItems = getDependencyItems(site, pages, processedDependencies, populateUpdatedDependecinesOnly, recursive, false);
                        item.setPages(pageItems);
                    } catch (IOException e) {
                        logger.error("Error while getting dependent file names for " + path + " in " + site, e);
                    } catch (ContentNotFoundException e) {
                        logger.error("Error while getting dependent file names for " + path + " in " + site, e);
                    }
                }
            }
            items.add(item);
        }
        return items;
    }

    /**
     * Loading document using a draft document to get it first from Draft (temp) folder
     * and if it is not into temp folder then it is getting from the path received in as argument
     * @param site The site name
     *
     * @param path Path where content is taken to load the content
     *
     * @return An document loaded from the path
     *
     * @throws ContentNotFoundException If content was not found
     */
    protected Document loadDocument(String site, String path) throws ContentNotFoundException{
    	Document document = null;
        document = getDocument(site,  path);
        /***************************************/
    	return document;
    }

    protected Document getDocument(String site, String path) throws ContentNotFoundException {
        Document document=null;
        InputStream content;
        try {
            content = contentService.getContent(site, path);
            document = ContentUtils.convertStreamToXml(content);
        } catch (DocumentException e) {
            logger.error("Failed to convert content to XML document.", e);
        }
        return document;
    }

    /**
     * get dependency file names from the given buffer
     *
     * @param site
     * @param buffer
     * @param populateUpdatedDependecinesOnly
     * @param patterns
     * @return
     */
    protected List<String> getDependentFileNames(String site, StringBuffer buffer, boolean populateUpdatedDependecinesOnly, List<String> patterns) {
        List<String> files = new ArrayList<>();
        if (patterns != null) {
            for (String patternStr : patterns) {
                Pattern pattern = Pattern.compile(patternStr);
                Matcher matcher = pattern.matcher(buffer);
                while (matcher.find()) {
                    String match = matcher.group();
                    //if (!populateUpdatedDependecinesOnly || contentService.isUpdatedOrNew(site, match)) {
                        if (!files.contains(match)) {
                            files.add(match);
                        }
                    //}
                }
            }
        }
        return files;
    }

    protected List<ContentItemTO> getDependencies(String site, List<String> submittedItems, DmContentItemComparator comparator,
                                                  boolean multiLevelChildren) throws ServiceException {

        return getDependencies(site, submittedItems, comparator,multiLevelChildren,false);

    }

    protected List<ContentItemTO> getDependenciesManualApproving(String site, List<String> submittedItems, DmContentItemComparator comparator, boolean multiLevelChildren, boolean delDep) throws ServiceException {
        if (submittedItems != null) {
            List<ContentItemTO> displayItems = new ArrayList<ContentItemTO>();
            Set<String> dependenciesPaths = new LinkedHashSet<String>();
            for (String submittedItem : submittedItems) {
                dependenciesPaths.add(submittedItem);
                dependenciesPaths.addAll(submitToApproveDependencyRule.applyRule(site, submittedItem));
            }
            for (String depPath : dependenciesPaths) {
                ContentItemTO itemTo = contentService.getContentItem(site, depPath);
                displayItems.add(itemTo);
            }

            return displayItems;
        } else {
            throw new ServiceException("No items provided.");
        }
    }

    protected List<ContentItemTO> getDependencies(String site, List<String> submittedItems, DmContentItemComparator comparator, boolean multiLevelChildren, boolean delDep) throws ServiceException {
        if (submittedItems != null) {
            // get all change set excluding deleted items
            List<ContentItemTO> items = new ArrayList<>(submittedItems.size());
            Set<String> includedItems = new HashSet<>();
            Set<String> includedDependencies = new HashSet<>();
            //Set<String> includedLvlDescs = new FastSet<String>();
            for (String submittedItem : submittedItems) {
                boolean deleteDependencies = delDep;
                if (!StringUtils.isEmpty(submittedItem) && !includedItems.contains(submittedItem)) {
                    try {
                        ContentItemTO item = null;
                        item = contentService.getContentItem(site, submittedItem);
                        if(item.isSubmittedForDeletion()) {
                            deleteDependencies = true;
                        }
                        if (deleteDependencies /* TODO: rename*/) {
                            if (item.isContainer()) {
                                String folderPath = submittedItem.replace(DmConstants.INDEX_FILE, "");
                                // The purpose is to set children for this node.
                                //item = dmContentService.getItems(item, site, null, folderPath, -1, false, "default", true, !delDep);
                            }
                        }

                        retrieveDependencyItems(item, includedDependencies, deleteDependencies, site);
                        addDependencyItem(site, items, includedItems, includedDependencies, item, comparator, deleteDependencies);
                        if (!delDep) {
                            List<String> levelDescs = getDependentLevelDescriptors(site, submittedItem, false, servicesConfig.getLevelDescriptorName(site));
                            for (String levelDesc : levelDescs) {
                                //String lvlDescFullPath = servicesConfig.getRepositoryRootPath(site) + levelDesc;
                                //ObjectState lvlDescState = objectStateService.getObjectState(site, levelDesc);
                                if (objectStateService.isNew(site, levelDesc)) {
                                    if (!submittedItems.contains(levelDesc) && !includedItems.contains(levelDesc)) {
                                        includedItems.add(levelDesc);
                                        ContentItemTO lvlItem = contentService.getContentItem(site, levelDesc);
                                        retrieveDependencyItems(lvlItem, includedDependencies, deleteDependencies, site);
                                        addDependencyItem(site, items, includedItems, includedDependencies, lvlItem, comparator, deleteDependencies);
                                    }
                                }
                        }
                        }
                    } catch (ContentNotFoundException e) {
                        logger.error("content not found ["+submittedItem+"]",e);
                    }
                }
            }
            List<ContentItemTO> displayItems = new ArrayList<>();
            // if we need to return child dependencies at single level
            // populate all children of each item to the second level
            // couldn't avoid doing this due to the mandatory parent setup
            List<String> allReferences = new ArrayList<String>();
            if (!multiLevelChildren) {
                for (ContentItemTO item : items) {
                    if (!includedDependencies.contains(item.getUri())) {
                        List<ContentItemTO> targetChildren = item.getChildren();
                        List<ContentItemTO> pages = item.getPages();
                        item.setChildren(new ArrayList<ContentItemTO>());
                        item.setNumOfChildren(0);
                        flattenDependencies(item, targetChildren, comparator,item,allReferences);
                        flattenDependencies(item, pages, comparator,null,allReferences);
                        displayItems.add(item);
                    }
                }
            }
            removeDuplicateReferences(allReferences,displayItems);
            return displayItems;
        } else {
            throw new ServiceException("No items provided.");
        }
    }

    /**
     * add all dependency files from the given content item
     *
     * @param parentItem
     * @param includedDependencies
     * @param site
     */
    protected void retrieveDependencyItems(ContentItemTO parentItem, Set<String> includedDependencies, boolean deleteDependency, String site) {
        String contentType=parentItem.getContentType();
        if (contentType == null) {
            return;
        }
        List<DeleteDependencyConfigTO> deleteDependencyPatterns = servicesConfig.getDeleteDependencyPatterns(site, contentType);
        List<String> deletePattern = new ArrayList<>();
        for(DeleteDependencyConfigTO dependencyConfigTO:deleteDependencyPatterns){
            deletePattern.add(dependencyConfigTO.getPattern());
        }
        List<ContentItemTO> componentItems = parentItem.getComponents();  //we need to do it recursively
        if (componentItems != null) {
            for (ContentItemTO component : componentItems) {
                if(!deleteDependency || ContentUtils.matchesPatterns(component.getUri(), deletePattern))
                    includedDependencies.add(component.getUri());
            }
        }

        List<ContentItemTO> documentItems = parentItem.getDocuments();
        if (documentItems != null) {
            for (ContentItemTO document : documentItems) {
                if(!deleteDependency || (deleteDependency && ContentUtils.matchesPatterns(document.getUri(), deletePattern)))
                    includedDependencies.add(document.getUri());
            }
        }

        List<ContentItemTO> levelDescriptorItems = parentItem.getLevelDescriptors();
        if (levelDescriptorItems != null) {
            for (ContentItemTO levelDescriptor : levelDescriptorItems) {
                if (!deleteDependency || (deleteDependency && ContentUtils.matchesPatterns(levelDescriptor.getUri(), deletePattern)))
                    includedDependencies.add(levelDescriptor.getUri());
            }
        }

        //$Review$ get deleted item dependencies
    }

    /**
     * add a dependency item to the list of items
     *
     * @param site
     * @param items
     * @param includedItems
     * @param item
     * @param comparator
     * @throws ServiceException
     */
    protected void addDependencyItem(String site, List<ContentItemTO> items, Set<String> includedItems, Set<String> includedDependencies,
                                     ContentItemTO item, DmContentItemComparator comparator,boolean deleteDependencies) throws ServiceException {
        // if this item is a new file, check if the parent is new
        if (!deleteDependencies && (item.isNewFile() /* TODO: check renamed */)) {
            String parentUri = "";
            if (item.getName().equals(DmConstants.INDEX_FILE)) {
                // if the current page is the index page, then the parent page is one level above
                // remove /index.xml
                String uri = item.getPath();
                String [] levels = uri.split("/");
                int last = levels.length - 1;
                for (int index = 0; index < last; index++) {
                    parentUri += levels[index] + "/";
                }
                parentUri += DmConstants.INDEX_FILE;
            } else {
                // if the current page is not an index page, then the parent page is the index page at the same level
                parentUri = item.getPath() + "/" + DmConstants.INDEX_FILE;
            }
            // add parent to the dependencies if the parent is new
            if (!includedItems.contains(parentUri) && parentUri.startsWith(DmConstants.ROOT_PATTERN_PAGES)) {
                try {
                    // add only if the parent item is new and not submitted to workflow
                    ContentItemTO parentItem = contentService.getContentItem(site, parentUri);
                    if (parentItem != null ) {
                        retrieveDependencyItems(parentItem, includedDependencies,deleteDependencies, site);
                        if (parentItem.isNewFile() /* TODO: check renamed */) {
                            // add the parent item first recursively
                            addDependencyItem(site, items, includedItems, includedDependencies, parentItem, comparator,deleteDependencies);
                        }
                    }
                } catch (ContentNotFoundException e) {
                    // if no parent found, there is no mandatory parent (e.g. download content)
                }
            }
        }/*
        boolean found = false;
        int position = -1;
        // add a new item as a child if the new item is a sub folder
        // or a file under one of the top level items
        for (int index = 0; index < items.size(); index++) {
            ContentItemTO topLevelItem = items.get(index);
            String categoryUri = topLevelItem.getUri();
            categoryUri = categoryUri.replaceFirst(DmConstants.INDEX_FILE, "");
            String topLevelItemFullPath = contentService.expandRelativeSitePath(site, topLevelItem.getUri());
            if (item.getUri().startsWith(categoryUri)) {
                populatePageDependencies(site, item, true);
                boolean topLevelItemRenamed = false; // TODO: check renamed
                topLevelItem.addChild(item, comparator, true,topLevelItemRenamed);
                position = index;
                found = true;
            } else {
                // if the top level item belongs to the current item being added
                // replace top level item with the current item and add the top level item to the current item
                String currentCategoryUri = item.getUri().replaceFirst(DmConstants.INDEX_FILE, "");
                if (topLevelItem.getUri().startsWith(currentCategoryUri)) {
                    items.remove(index);
                    populatePageDependencies(site, item, true);
                    item.addChild(topLevelItem, comparator, true);
                    if (!found) {
                        populatePageDependencies(site, item, true);
                        items.add(index, item);
                        found = true;
                        position = index;
                    }
                }
            }
        }*/
        // if not, add the new item to the top level item list
        //if (!found) {
            //EMO-11523 dont include page dependencies for delete flow
            if(!deleteDependencies) {
                populatePageDependencies(site, item, true);
            }
            items.add(item);
        //}
        includedItems.add(item.getUri());
    }

    protected void flattenDependencies(ContentItemTO topLevelItem,
                                       List<ContentItemTO> children, DmContentItemComparator comparator, ContentItemTO parent,List<String>referencePages) {
        if (children != null) {
            for (ContentItemTO child : children) {
                // add lower level dependencies to the same top level item recursively
                if(!isChildAlreadyExists(topLevelItem,child)) {
                    topLevelItem.addChild(child,false,false);
                }
                if(parent != null) {
                    child.setParentPath(parent.getBrowserUri());
                } else {
                    //for reference pages parent will be null
                    referencePages.add(child.getUri());
                }
                flattenDependencies(topLevelItem, child.getChildren(), comparator,child,referencePages);
                flattenDependencies(topLevelItem, child.getPages(), comparator,null,referencePages);
                // remove children and floating children since those are already added to the top level item
                child.setChildren(null);
                // add the current level item
//                    topLevelItem.addChild(child, comparator, false);
            }
        }
    }

    /**
     * populate all children of each item to the first level dependency
     *
     * @param topLevelItem
     * @param item
     */
    protected boolean isChildAlreadyExists(ContentItemTO topLevelItem, ContentItemTO item) {
        List<ContentItemTO> children = topLevelItem.getChildren();
        for(ContentItemTO child:children) {
            if(child.getUri().equals(item.getUri())) {
                return true;
            }
        }
        return false;
    }

    protected void removeDuplicateReferences(List<String>references, List<ContentItemTO>displayItems) {
        Iterator<ContentItemTO> itr = displayItems.iterator();
        while (itr.hasNext()) {
            ContentItemTO item = itr.next();
            String uri = item.getUri();
            if(references.contains(uri)) {
                itr.remove();
            }
        }
    }

    protected void populatePageDependencies(String site, ContentItemTO item, boolean populateUpdatedDependecinesOnly) {
        Map<String, Object> params = new HashMap<>();
        params.put("site", site);
        params.put("sourcePath", getCleanPath(item.getUri()));
        params.put("type", DEPENDENCY_NAME_PAGE);
        List<DependencyEntity> pages = dependencyMapper.getDependenciesByType(params);
        List<ContentItemTO> pageItems = getDependentItems(site, item.getUri(), pages, populateUpdatedDependecinesOnly);
        List<ContentItemTO> newPages = new ArrayList<ContentItemTO>();
        if (populateUpdatedDependecinesOnly) {
            for (ContentItemTO pageItem : pageItems) {
                if (pageItem.isNew()) {
                    newPages.add(pageItem);
                }
            }
            item.setPages(newPages);
        } else{
            item.setPages(pageItems);
        }
    }
/*
    @Override
    public List<DependencyEntity> getDirectDependencies(String site, String path) {
        try {
            return _dependencyDaoService.getDependencies(site, path);
        } catch (SQLException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Failed to get direct dependency", e);
            }
            return null;
        }
    }*/

    @Override
    public void extractDependencies(String site, String path, Document document, Map<String, Set<String>> globalDeps) throws ServiceException {
        if (globalDeps == null) {
            globalDeps = new HashMap<String, Set<String>>();
        }
        Map<String, List<String>> dependencies = extractDirectDependency(site, path, document, globalDeps);
        int size = 0;
        for (List<String> vals : dependencies.values()) {
            size += (vals == null) ? 0 : vals.size();
        }
        setDependencies(site, path, dependencies);
    }

    /**
     * get a map of prefixed QName and a list of dependency files for the given document
     * (e.g. cstudio-core:children={/site/website/...},cstudio-core:components={...},...)
     *
     * @param site
     * @param path
     * @param document
     * @return a map of direct dependency
     */
    protected Map<String, List<String>> extractDirectDependency(String site, String path, Document document, Map<String, Set<String>> globalDeps) {
        // here we only care about direct dependencies (assets, components, documents - no child pages)
        // need all items regardless they are updated or not
        // add the current path to all dependency items as a parent
        // update the corresponding components, documents, assets to have this path as a mandatory parent
        if (globalDeps == null) {
            globalDeps = new HashMap<>();
        }
        Set<String> globalPages = globalDeps.get(DEPENDENCY_NAME_PAGE);
        Set<String> globalComponents = globalDeps.get(DEPENDENCY_NAME_COMPONENT);
        if ((globalPages != null && globalPages.contains(path)) || (globalComponents != null && globalComponents.contains(path))) {
            return new HashMap<>();
        }
        try {

            // Check for skipDependencies flag
            Element root = document.getRootElement();
            boolean skipDependencies = false;
            String isSkipDependenciesValue = root.valueOf("//" + DmXmlConstants.ELM_SKIP_DEPENDENCIES);
            if (isSkipDependenciesValue != null && !"".equals(isSkipDependenciesValue)) {
                skipDependencies = ContentFormatUtils.getBooleanValue(isSkipDependenciesValue);
            }
            if (skipDependencies) {
                return new HashMap<>();
            }

            StringBuffer buffer = new StringBuffer(XmlUtils.convertDocumentToString(document));
            List<String> assets = getDependentFileNames(site, buffer, false, servicesConfig.getAssetPatterns(site));
            List<String> components = getDependentFileNames(site, buffer, false, servicesConfig.getComponentPatterns(site));
            List<String> documents = getDependentFileNames(site, buffer, false, servicesConfig.getDocumentPatterns(site));
            List<String> pages = getDependentFileNames(site, buffer, false, servicesConfig.getPagePatterns(site));
            List<String> templates = getDependentFileNames(site, buffer, false, servicesConfig.getRenderingTemplatePatterns(site));
            //List<String> levelDescriptors = getDependentLevelDescriptors(site, path, false, servicesConfig.getLevelDescriptorName(site));
            Map<String, List<String>> dependency = new HashMap<>();
            dependency.put(DEPENDENCY_NAME_ASSET, assets);
            dependency.put(DEPENDENCY_NAME_COMPONENT, components);
            dependency.put(DEPENDENCY_NAME_DOCUMENT, documents);
            dependency.put(DEPENDENCY_NAME_PAGE, pages);
            dependency.put(DEPENDENCY_NAME_RENDERING_TEMPLATE, templates);
            //dependency.put(DEPENDENCY_NAME_LEVEL_DESCRIPTOR, levelDescriptors);

            for (String patternStr : servicesConfig.getPagePatterns(site)) {
                Pattern pattern = Pattern.compile(patternStr);
                Matcher matcher = pattern.matcher(path);
                if (matcher.matches()) {
                    if (globalPages == null) {
                        globalPages = new HashSet<>();
                    }
                    globalPages.add(path);
                    globalDeps.put(DEPENDENCY_NAME_PAGE, globalPages);
                    break;
                }
            }

            for (String patternStr : servicesConfig.getComponentPatterns(site)) {
                Pattern pattern = Pattern.compile(patternStr);
                Matcher matcher = pattern.matcher(path);
                if (matcher.matches()) {
                    if (globalComponents == null) {
                        globalComponents = new HashSet<>();
                    }
                    globalComponents.add(path);
                    globalDeps.put(DEPENDENCY_NAME_COMPONENT, globalComponents);
                    break;
                }
            }

            for (String assetPath : assets) {
                Set<String> parsedAssets = globalDeps.get(DEPENDENCY_NAME_ASSET);
                if (parsedAssets == null) {
                    parsedAssets = new HashSet<>();
                }
                if (parsedAssets.contains(assetPath)) {
                    continue;
                }
                if (assetPath.endsWith(DmConstants.CSS_PATTERN)) {
                    String content = contentService.getContentAsString(site, assetPath);
                    if (StringUtils.isNotEmpty(content)) {
                        StringBuffer sb = new StringBuffer(content);
                        try {
                            extractDependenciesStyle(site, assetPath, sb, globalDeps);
                        } catch (ServiceException e) {
                            logger.error("Failed to get style dependencies", e);
                        }
                    }

                } else if (assetPath.endsWith(DmConstants.JS_PATTERN)) {
                    String content = contentService.getContentAsString(site, assetPath);
                    if (StringUtils.isNotEmpty(content)) {
                        StringBuffer sb = new StringBuffer(content);
                        try {
                            extractDependenciesJavascript(site, assetPath, sb, globalDeps);
                        } catch (ServiceException e) {
                            logger.error("Failed to get javascript dependencies", e);
                        }
                    }
                }
            }
            for (String templatePath : templates) {
                Set<String> parsedTemplates = globalDeps.get(DEPENDENCY_NAME_RENDERING_TEMPLATE);
                if (parsedTemplates == null) {
                    parsedTemplates = new HashSet<>();
                }
                if (parsedTemplates.contains(templatePath)) {
                    continue;
                }
                String content = contentService.getContentAsString(site, templatePath);
                if (StringUtils.isNotEmpty(content)) {
                    StringBuffer sb = new StringBuffer(content);
                    try {
                        extractDependenciesTemplate(site, templatePath, sb, globalDeps);
                    } catch (ServiceException e) {
                        logger.error("Failed to get template dependencies", e);
                    }
                }
            }
            return dependency;

        } catch (IOException e) {
            logger.error("Failed to get direct dependency", e);
        }
        return null;
    }

    @Override
    public void extractDependenciesTemplate(String site, String path, StringBuffer templateContent, Map<String, Set<String>> globalDeps) throws ServiceException {
        if (globalDeps == null) {
            globalDeps = new HashMap<>();
        }
        List<String> assets = getDependentFileNames(site, templateContent, false, servicesConfig.getAssetPatterns(site));
        List<String> templates = getDependentFileNames(site, templateContent, false, servicesConfig.getRenderingTemplatePatterns(site));
        while (templates.contains(path)) {
            templates.remove(path);
        }
        Map<String, List<String>> dependency = new HashMap<>();
        dependency.put(DEPENDENCY_NAME_ASSET, assets);
        dependency.put(DEPENDENCY_NAME_RENDERING_TEMPLATE, templates);
        setDependencies(site, path, dependency);
        Set<String> parsedTemplates = globalDeps.get(DEPENDENCY_NAME_RENDERING_TEMPLATE);
        if (parsedTemplates == null) {
            parsedTemplates = new HashSet<>();
        }
        parsedTemplates.add(path);
        globalDeps.put(DEPENDENCY_NAME_RENDERING_TEMPLATE, parsedTemplates);
        for (String assetPath : assets) {
            Set<String> parsedAssets = globalDeps.get(DEPENDENCY_NAME_ASSET);
            if (parsedAssets == null) {
                parsedAssets = new HashSet<>();
            }
            if (parsedAssets.contains(assetPath)) {
                continue;
            }
            if (assetPath.endsWith(DmConstants.CSS_PATTERN)) {
                if (contentService.contentExists(site, assetPath)) {
                    StringBuffer sb = new StringBuffer(contentService.getContentAsString(site, assetPath));
                    extractDependenciesStyle(site, assetPath, sb, globalDeps);
                }
            } else if (assetPath.endsWith(DmConstants.JS_PATTERN)) {
                if (contentService.contentExists(site, assetPath)) {
                    StringBuffer sb = new StringBuffer(contentService.getContentAsString(site, assetPath));
                    extractDependenciesJavascript(site, assetPath, sb, globalDeps);
                }
            }
        }
        for (String templatePath : templates) {

            if (parsedTemplates.contains(templatePath)) {

                continue;
            }
            if (contentService.contentExists(site, templatePath)) {
                StringBuffer sb = new StringBuffer(contentService.getContentAsString(site, templatePath));
                extractDependenciesTemplate(site, templatePath, sb, globalDeps);
            }

        }
    }

    @Override
    public void extractDependenciesStyle(String site, String path, StringBuffer styleContent, Map<String, Set<String>> globalDeps) throws ServiceException {
        if (globalDeps == null) {
            globalDeps = new HashMap<>();
        }
        List<String> assets = getDependentFileNames(site, styleContent, false, servicesConfig.getAssetPatterns(site));
        while (assets.contains(path)) {
            assets.remove(path);
        }
        Map<String, List<String>> dependency = new HashMap<>();

        dependency.put(DEPENDENCY_NAME_ASSET, assets);
        setDependencies(site, path, dependency);
        Set<String> parsedAssets = globalDeps.get(DEPENDENCY_NAME_ASSET);
        if (parsedAssets == null) {
            parsedAssets = new HashSet<>();
        }
        parsedAssets.add(path);
        globalDeps.put(DEPENDENCY_NAME_ASSET, parsedAssets);
        for (String assetPath : assets) {

            if (parsedAssets.contains(assetPath)) {
                continue;
            }
            if (assetPath.endsWith(DmConstants.CSS_PATTERN)) {
                if (contentService.contentExists(site, assetPath)) {
                    StringBuffer sb = new StringBuffer(contentService.getContentAsString(site, assetPath));
                    extractDependenciesStyle(site, assetPath, sb, globalDeps);
                }
            }
        }

    }

    @Override
    public void extractDependenciesJavascript(String site, String path, StringBuffer javascriptContent, Map<String, Set<String>> globalDeps) throws ServiceException {
        if (globalDeps == null) {
            globalDeps = new HashMap<>();
        }
        List<String> assets = getDependentFileNames(site, javascriptContent, false, servicesConfig.getAssetPatterns(site));
        while (assets.contains(path)) {
            assets.remove(path);
        }
        Map<String, List<String>> dependency = new HashMap<>();
        dependency.put(DEPENDENCY_NAME_ASSET, assets);
        setDependencies(site, path, dependency);
        Set<String> parsedAssets = globalDeps.get(DEPENDENCY_NAME_ASSET);
        if (parsedAssets == null) {
            parsedAssets = new HashSet<>();
        }
        parsedAssets.add(path);
        globalDeps.put(DEPENDENCY_NAME_ASSET, parsedAssets);
        for (String assetPath : assets) {
            if (parsedAssets.contains(assetPath)) {
                continue;
            }
            if (assetPath.endsWith(DmConstants.JS_PATTERN)) {
                if (contentService.contentExists(site, assetPath)) {
                    StringBuffer sb = new StringBuffer(contentService.getContentAsString(site, assetPath));
                    extractDependenciesJavascript(site, assetPath, sb, globalDeps);
                }
            }
        }
    }

    protected List<String> getDependentLevelDescriptors(String site, String path, boolean b, String levelDescriptorName) {
        List<String> levelDescriptors = new ArrayList<>();
        // TODO: implement search for level descriptors
        return levelDescriptors;
    }

	@Override
    public void setDependencies(final String site, final String path, final Map<String, List<String>> dependencies) throws ServiceException {
        try {
            final Map<String, List<String>> filteredDependencies =  new HashMap<>();
            for (String type : dependencies.keySet()) {
                filteredDependencies.put(type, applyIgnoreDependenciesRules(site, dependencies.get(type)));
            }
            Map<String, Object> params = new HashMap<>();
            params.put("site", site);
            params.put("path", path);
            dependencyMapper.deleteAllSourceDependencies(params);
            if (dependencies != null) {
                for (String type : dependencies.keySet()) {
                    List<String> files = dependencies.get(type);
                    if (files != null && files.size() > 0) {
                        List<DependencyEntity> deps = new ArrayList<>();
                        for (String file : files) {
                            DependencyEntity dependencyObj = new DependencyEntity();
                            dependencyObj.setSite(site);
                            dependencyObj.setSourcePath(getCleanPath(path));
                            dependencyObj.setTargetPath(getCleanPath(file));
                            dependencyObj.setType(type);
                            deps.add(dependencyObj);
                        }
                        params = new HashMap<>();
                        params.put("dependencies", deps);
                        dependencyMapper.insertList(params);
                    }
                }
            }
        } catch (Exception e) {
            throw new ServiceException("Failed to set dependencies for " + path + " in " + site, e);
        }
    }

    private String getCleanPath(String path) {
        String cleanPath = path.replaceAll("//", "/");
        return cleanPath;
    }

    private List<String> applyIgnoreDependenciesRules(String site, List<String> dependencies) {
        List<String> filteredDependencies = new ArrayList<>();
        for (String dependency : dependencies) {
            boolean ignore = false;
            if (!contentService.contentExists(site, dependency)) {
                for (String rule : getIgnoreDependenciesRules()) {
                    if (dependency.matches(rule)) {
                        ignore = true;
                        break;
                    }
                }
            }
            if (!ignore) {
                filteredDependencies.add(dependency);
            }
        }
        return filteredDependencies;
     }



    @Override
    public void updateDependencies(String site, String path, String state) {
        DmDependencyTO dmDependencyTo = getDependencies(site, path, false, true);
        if (dmDependencyTo != null) {
            List<DmDependencyTO> pages = dmDependencyTo.getPages();
            updateDependency(site, state, pages);
            List<DmDependencyTO> components = dmDependencyTo.getComponents();
            updateDependency(site, state, components);
            List<DmDependencyTO> documents = dmDependencyTo.getDocuments();
            updateDependency(site, state, documents);
            List<DmDependencyTO> templates = dmDependencyTo.getRenderingTemplates();
            updateDependency(site, state, templates);
        }
    }

    protected void updateDependency(String site,String state,List<DmDependencyTO> dependencies) {
        // TODO: This is no longer used, clean up
    }

    /**
     *
     * Replace dependencies in the document based on the values in the Map original,target
     *
     * Used by copy/paste scenario where page dependencies are duplicated
     *
     * @param site
     * @param dependencies
     */
    @Override
    public InputStream replaceDependencies(String site, Document document, Map<String, String> dependencies) throws ServiceException {
        try {
            if(!dependencies.isEmpty()){
                String xml= XmlUtils.convertDocumentToString(document);
                for(String source:dependencies.keySet()){
                    String target = dependencies.get(source);
                    xml = xml.replace(source, target);
                }
                return new ByteArrayInputStream(xml.getBytes());
            }

        } catch (IOException e) {
            throw new ServiceException("Unable to replace dependencies " + e);
        }
        return ContentUtils.convertDocumentToStream(document, StudioConstants.CONTENT_ENCODING);
    }

    /**
     *
     * Return a map of <Dependency matching copy pattern, target location>
     */
    @Override
    public Map<String, String> getCopyDependencies(String site, String sourceContentPath, String dependencyPath) throws ServiceException {
        Map<String,String> copyDependency = new HashMap<String,String>();
        if(sourceContentPath.endsWith(DmConstants.XML_PATTERN) && dependencyPath.endsWith(DmConstants.XML_PATTERN)){
            ContentItemTO dependencyItem = contentService.getContentItem(site, sourceContentPath);
            if (dependencyItem != null) {
                String contentType = dependencyItem.getContentType();
                List<CopyDependencyConfigTO> copyDependencyPatterns = servicesConfig.getCopyDependencyPatterns(site, contentType);
                if (copyDependencyPatterns != null && copyDependencyPatterns.size() > 0) {
                    logger.debug("Copy Pattern provided for contentType" + contentType);
                    DmDependencyTO dmDependencyTo = getDependencies(site, dependencyPath, false, true);
                    if (dmDependencyTo != null) {
                        //TODO are pages also required?
                        List<DmDependencyTO> dependencyTOItems = dmDependencyTo.getDirectDependencies(); //documents,assets,components
                        for (DmDependencyTO dependency : dependencyTOItems) {
                            String assocFilePath = dependency.getUri();
                            for (CopyDependencyConfigTO copyConfig : copyDependencyPatterns) {
                                if (StringUtils.isNotEmpty(copyConfig.getPattern()) &&
                                        StringUtils.isNotEmpty(copyConfig.getTarget()) && assocFilePath.matches(copyConfig.getPattern())) {
                                    ContentItemTO assocItem = contentService.getContentItem(site, assocFilePath);
                                    if (assocItem != null) {
                                        copyDependency.put(dependency.getUri(), copyConfig.getTarget());
                                    }
                                }
                            }
                        }
                    }
                } else {
                    logger.debug("Copy Pattern is not provided for contentType" + contentType);
                }
            } else {
                logger.debug("Not found dependency item at site {0} path {!}", site, sourceContentPath);
            }
        }
        return copyDependency;
    }



    /**
	 *
	 * @param site
	 * @param sourcePath
	 * @param dependencies
	 * @return
	 * @throws ServiceException
	 *//*
	protected List<String> filterDependenicesMatchingDeletePattern(String site, String sourcePath, List<String> dependencies) throws ServiceException{
		List<String> matchingDep = new FastList<String>();
		if(sourcePath.endsWith(DmConstants.XML_PATTERN) && sourcePath.endsWith(DmConstants.XML_PATTERN)){
			List<DeleteDependencyConfigTO> deleteAssociations = getDeletePatternConfig(site,sourcePath);
			if (deleteAssociations != null && deleteAssociations.size() > 0) {
				for(String dependency:dependencies){
					for (DeleteDependencyConfigTO deleteAssoc : deleteAssociations) {
						if (dependency.matches(deleteAssoc.getPattern())) {
							matchingDep.add(dependency);
						}
					}
				}
			}
		}
		return matchingDep;
	}*/

    @Override
    public List<String> getDependencyPaths(String site, String path) {
        List<String> toRet = new ArrayList<>();
        Map<String, String> params = new HashMap<String, String>();
        params.put("site", site);
        params.put("sourcePath", getCleanPath(path));
        List<DependencyEntity> deps = dependencyMapper.getDependencies(params);
        for (DependencyEntity dep : deps) {
            toRet.add(dep.getTargetPath());
        }
        return toRet;
    }

    @Override
    public List<String> getDependantPaths(String site, String path) {
        List<String> toRet = new ArrayList<>();
        Map<String, String> params = new HashMap<String, String>();
        params.put("site", site);
        params.put("targetPath", getCleanPath(path));
        List<DependencyEntity> deps = dependencyMapper.getDependant(params);
        for (DependencyEntity dep : deps) {
            toRet.add(dep.getSourcePath());
        }
        return toRet;
    }


    @Override
    public void deleteDependenciesForSite(String site) {
        Map<String, String> params = new HashMap<>();
        params.put("site", site);
        dependencyMapper.deleteDependenciesForSite(params);
    }

    @Override
    public void deleteDependenciesForSiteAndPath(String site, String path) {
        Map<String, String> params = new HashMap<>();
        params.put("site", site);
        params.put("path", path);
        dependencyMapper.deleteDependenciesForSiteAndPath(params);
    }

    public List<String> getIgnoreDependenciesRules() {
        return Arrays.asList(studioConfiguration.getProperty(DEPENDENCIES_IGNORE_DEPENDENCIES_RULES).split(","));
    }

    public boolean isManualDependencyApprovingEnabled() {
        boolean toReturn = Boolean.parseBoolean(studioConfiguration.getProperty(DEPENDENCIES_MANUAL_DEPENDENCY_APPROVING_ENABLED));
        return toReturn;
    }

    public ContentService getContentService() { return contentService; }
    public void setContentService(ContentService contentService) { this.contentService = contentService; }

    public ServicesConfig getServicesConfig() { return servicesConfig; }
    public void setServicesConfig(ServicesConfig servicesConfig) { this.servicesConfig = servicesConfig; }

    public org.craftercms.studio.api.v1.service.objectstate.ObjectStateService getObjectStateService() { return objectStateService; }
    public void setObjectStateService(org.craftercms.studio.api.v1.service.objectstate.ObjectStateService objectStateService) { this.objectStateService = objectStateService; }

    public SubmitToApproveDependencyRule getSubmitToApproveDependencyRule() { return submitToApproveDependencyRule; }
    public void setSubmitToApproveDependencyRule(SubmitToApproveDependencyRule submitToApproveDependencyRule) { this.submitToApproveDependencyRule = submitToApproveDependencyRule; }

    public StudioConfiguration getStudioConfiguration() { return studioConfiguration; }
    public void setStudioConfiguration(StudioConfiguration studioConfiguration) { this.studioConfiguration = studioConfiguration; }

    protected ContentService contentService;
    protected ServicesConfig servicesConfig;
    protected org.craftercms.studio.api.v1.service.objectstate.ObjectStateService objectStateService;
    protected SubmitToApproveDependencyRule submitToApproveDependencyRule;
    protected StudioConfiguration studioConfiguration;

    @Autowired
    protected DependencyMapper dependencyMapper;
}
