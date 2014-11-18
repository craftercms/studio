/*******************************************************************************
 * Crafter Studio Web-content authoring solution
 *     Copyright (C) 2007-2013 Crafter Software Corporation.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.craftercms.cstudio.alfresco.dm.service.impl;

import javolution.util.FastComparator;
import javolution.util.FastList;
import javolution.util.FastTable;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.avm.AVMBadArgumentException;
import org.alfresco.service.cmr.avm.AVMException;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.DuplicateChildNodeNameException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.json.JSONObject;
import org.craftercms.cstudio.alfresco.cache.Scope;
import org.craftercms.cstudio.alfresco.cache.cstudioCacheManager;
import org.craftercms.cstudio.alfresco.constant.CStudioConstants;
import org.craftercms.cstudio.alfresco.constant.CStudioContentModel;
import org.craftercms.cstudio.alfresco.dm.constant.DmConstants;
import org.craftercms.cstudio.alfresco.dm.constant.DmXmlConstants;
import org.craftercms.cstudio.alfresco.dm.executor.ProcessContentExecutor;
import org.craftercms.cstudio.alfresco.dm.service.api.*;
import org.craftercms.cstudio.alfresco.dm.service.api.DmContentLifeCycleService.ContentLifeCycleOperation;
import org.craftercms.cstudio.alfresco.dm.service.impl.DmDependencyDiffService.DiffRequest;
import org.craftercms.cstudio.alfresco.dm.to.*;
import org.craftercms.cstudio.alfresco.dm.util.DmContentItemOrderComparator;
import org.craftercms.cstudio.alfresco.dm.util.DmUtils;
import org.craftercms.cstudio.alfresco.dm.util.api.ContentPropertyLoader;
import org.craftercms.cstudio.alfresco.service.AbstractRegistrableService;
import org.craftercms.cstudio.alfresco.service.api.*;
import org.craftercms.cstudio.alfresco.service.api.ActivityService.ActivityType;
import org.craftercms.cstudio.alfresco.service.exception.ContentNotFoundException;
import org.craftercms.cstudio.alfresco.service.exception.ServiceException;
import org.craftercms.cstudio.alfresco.to.ContentTypeConfigTO;
import org.craftercms.cstudio.alfresco.to.ResultTO;
import org.craftercms.cstudio.alfresco.util.ContentFormatUtils;
import org.craftercms.cstudio.alfresco.util.ContentUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Dejan Brkic
 */
public class DmContentServiceImpl extends AbstractRegistrableService implements DmContentService {

    private static final Logger logger = LoggerFactory.getLogger(DmContentServiceImpl.class);

    protected static final String MSG_WORKING_COPY_LABEL = "coci_service.working_copy_label";
    protected static final String EXTENSION_CHARACTER = ".";


    /**
     * file and foler name patterns for copied files and folders *
     */
    public final static Pattern COPY_FILE_PATTERN = Pattern.compile("(.+)-([0-9]+)\\.(.+)");
    public final static Pattern COPY_FOLDER_PATTERN = Pattern.compile("(.+)-([0-9]+)");

    protected cstudioCacheManager _cache;

    public cstudioCacheManager getCache() {
        return this._cache;
    }

    public void setCache(cstudioCacheManager cache) {
        this._cache = cache;
    }

    /**
     * extractors to extract content item properties upon generating content items
     */
    protected List<ContentPropertyLoader> _propertyLoaders;

    public List<ContentPropertyLoader> getPropertyLoaders() {
        return this._propertyLoaders;
    }

    public void setPropertyLoaders(List<ContentPropertyLoader> propertyLoaders) {
        this._propertyLoaders = propertyLoaders;
    }

    protected ProcessContentExecutor _contentProcessor;

    public ProcessContentExecutor getContentProcessor() {
        return this._contentProcessor;
    }

    public void setContentProcessor(ProcessContentExecutor contentProcessor) {
        this._contentProcessor = contentProcessor;
    }

    @Override
    public void register() {
        getServicesManager().registerService(DmContentService.class, this);
    }

    @Override
    public InputStream getContent(String site, String path, boolean edit, boolean mergePrototype) throws AccessDeniedException, ContentNotFoundException {
        String user = getService(PersistenceManagerService.class).getCurrentUserName();
        AuthenticationUtil.setFullyAuthenticatedUser(user);
        String fullPath = getContentFullPath(site, path);
        return _getContent(site, path, edit, user, fullPath, mergePrototype);
    }

    @Override
    public InputStream getContent(String site, String path) throws AccessDeniedException, ContentNotFoundException {
        String user = getService(PersistenceManagerService.class).getCurrentUserName();
        String fullPath = getContentFullPath(site, path);
        return _getContent(site, path, false, user, fullPath, false);
    }

    @Override
    public InputStream getContentFromDraft(String site, String path, boolean edit, boolean mergePrototype) throws AccessDeniedException, ContentNotFoundException {
        ;
        String user = getService(PersistenceManagerService.class).getCurrentUserName();
        //AuthenticationUtil.setFullyAuthenticatedUser(_servicesConfig.getSandbox(site));
        AuthenticationUtil.setFullyAuthenticatedUser(user);
        /* Disable DRAFT repo Dejan 29.03.2012 */
        //String previewPath = DmUtils.getPreviewPath(path);
        String fullPath = getContentFullPath(site, path, false);

        //fullPath = DmUtils.cleanRepositoryPath(fullPath) + previewPath;
        //return _getContent(site, previewPath, false, user, fullPath,mergePrototype, true);
        return _getContent(site, fullPath, false, user, fullPath, mergePrototype, true);
        /***************************************/
    }

    protected InputStream _getContent(String site, String path, boolean edit, String user, String fullPath, boolean mergePrototype) throws ContentNotFoundException {
        return _getContent(site, path, edit, user, fullPath, mergePrototype, false);
    }

    protected InputStream _getContent(String site, String path, boolean edit, String user, String fullPath, boolean mergePrototype, boolean isDraft) throws ContentNotFoundException {
        ServicesConfig servicesConfig = getService(ServicesConfig.class);
        String repoRootPath = servicesConfig.getRepositoryRootPath(site);
        /* Disable DRAFT repo Dejan 29.03.2012 */
        /*
        if (isDraft) {
        	repoRootPath = DmUtils.cleanRepositoryPath(repoRootPath);
        } */
        /***************************************/
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        NodeRef contentNode = persistenceManagerService.getNodeRef(repoRootPath + "/" + path);

        if (contentNode != null) {
            if (edit) {
                if (persistenceManagerService.getLockStatus(contentNode).equals(LockStatus.LOCKED)) {
                    if (logger.isWarnEnabled()) {
                        logger.warn("Content at " + path + " is already locked by another user.");
                    }
                } else {
                    persistenceManagerService.lock(contentNode, LockType.WRITE_LOCK);
                }
            }
            FileInfo fileInfo = persistenceManagerService.getFileInfo(contentNode);
            Map<QName, Serializable> contentProps = fileInfo.getProperties();
            Serializable contentTypeValue = contentProps.get(CStudioContentModel.PROP_CONTENT_TYPE);
            String contentType = (contentTypeValue != null) ? contentTypeValue.toString() : null;

            if (mergePrototype && !StringUtils.isEmpty(contentType)) {
                ContentTypeConfigTO config = servicesConfig.getContentTypeConfig(site, contentType);
                if (config != null) {
                    if (!DmConstants.CONTENT_TYPE_CONFIG_FORM_PATH_SIMPLE.equalsIgnoreCase(config.getFormPath())) {
                        Serializable versionValue = contentProps.get(CStudioContentModel.PROP_TEMPLATE_VERSION);
                        String version = (versionValue != null) ? versionValue.toString() : null;
                        DmContentTypeService dmContentTypeService = getService(DmContentTypeService.class);
                        return dmContentTypeService.mergeLastestTemplate(site, path, contentType, version, persistenceManagerService.getReader(contentNode).getContentInputStream());
                    }
                }
            }
            return persistenceManagerService.getReader(contentNode).getContentInputStream();
        } else {
            throw new ContentNotFoundException(path + " is not found in site: " + site);
        }
    }

    /**
     * load common properties from the content
     *
     * @param site
     * @param node
     * @param item
     * @param populateDependencies
     * @param populateUpdatedDependecinesOnly
     *
     * @throws ServiceException
     */
    @SuppressWarnings("unchecked")
    protected void loadCommonProperties(String site, AVMNodeDescriptor node, DmContentItemTO item,
                                        boolean populateDependencies, boolean populateUpdatedDependecinesOnly) throws ServiceException {
        String path = node.getPath();
        if (logger.isDebugEnabled()) {
            logger.debug("loading common properties for " + path);
        }
        // set component flag
        ServicesConfig servicesConfig = getService(ServicesConfig.class);
        item.setComponent(matchesPatterns(item.getUri(), servicesConfig.getComponentPatterns(site)));
        if (item.getName().equals(servicesConfig.getLevelDescriptorName(site))) {
            item.setLevelDescriptor(true);
            // level descriptors are components
            item.setComponent(true);
        }
        // set document flag
        item.setDocument(matchesPatterns(item.getUri(), servicesConfig.getDocumentPatterns(site)));
        // if the content type meta is empty, populate properties from the file
        Document content = null;
        // read common metadata
        Serializable internalNameValue = getPropertyValue(path, CStudioContentModel.PROP_INTERNAL_NAME);
        String internalName = (internalNameValue != null) ? internalNameValue.toString() : null;
        if (!StringUtils.isEmpty(item.getContentType()) && !StringUtils.isEmpty(internalName)) {
            loadContentProperties(path, item);
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("content type is not found from the content metadata. loading properties from XML.");
            }
            content = loadPropertiesFromXml(site, path, item);
        }
        content = loadHideInAuthoringPropertyFromXml(site, path, item);
        if (content == null) {
            content = getDocumentFromDmContent(path);
        }
        // populate dependencies
        if (populateDependencies) {
            DmDependencyService dmDependencyService = getService(DmDependencyService.class);
            dmDependencyService.populateDependencyContentItems(site, item, populateUpdatedDependecinesOnly);
        }
        // TODO: remove this when order extraction is ready
        if (item.getOrders() == null) {
            if (content != null) {
                Element root = content.getRootElement();
                //item.setOrders(getItemOrders(root.selectNodes("//" + DmXmlConstants.ELM_ORDERS + "/"
                //        + DmXmlConstants.ELM_ORDER)));
                item.setOrders(getItemOrders(root.selectNodes("//" + DmXmlConstants.ELM_ORDER_DEFAULT)));
            }
        }
    }

    /**
     * load common properties from the content
     *
     * @param site
     * @param node
     * @param item
     * @param populateDependencies
     * @param populateUpdatedDependecinesOnly
     *
     * @throws ServiceException
     */
    @SuppressWarnings("unchecked")
    protected void loadCommonProperties(String site, FileInfo node, DmContentItemTO item,
                                        boolean populateDependencies, boolean populateUpdatedDependecinesOnly) throws ServiceException {
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        String path = persistenceManagerService.getNodePath(node.getNodeRef());
        if (logger.isDebugEnabled()) {
            logger.debug("loading common properties for " + path);
        }
        // set component flag
        ServicesConfig servicesConfig = getService(ServicesConfig.class);
        item.setComponent(matchesPatterns(item.getUri(), servicesConfig.getComponentPatterns(site)));
        if (item.getName().equals(servicesConfig.getLevelDescriptorName(site))) {
            item.setLevelDescriptor(true);
            // level descriptors are components
            item.setComponent(true);
        }
        // set document flag
        item.setDocument(matchesPatterns(item.getUri(), servicesConfig.getDocumentPatterns(site)));
        // if the content type meta is empty, populate properties from the file
        Document content = null;
        // read common metadata
        Serializable internalNameValue = getPropertyValue(path, CStudioContentModel.PROP_INTERNAL_NAME);
        String internalName = (internalNameValue != null) ? internalNameValue.toString() : null;
        if (!StringUtils.isEmpty(item.getContentType()) && !StringUtils.isEmpty(internalName)) {
            loadContentProperties(path, item);
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("content type is not found from the content metadata. loading properties from XML.");
            }
            content = loadPropertiesFromXml(site, path, item);
        }
        content = loadHideInAuthoringPropertyFromXml(site, path, item);
        if (content == null) {
            content = getDocumentFromDmContent(path);
        }
        // populate dependencies
        if (populateDependencies) {
            DmDependencyService dmDependencyService = getService(DmDependencyService.class);
            dmDependencyService.populateDependencyContentItems(site, item, populateUpdatedDependecinesOnly);
        }
        // TODO: remove this when order extraction is ready
        if (item.getOrders() == null) {
            if (content != null) {
                Element root = content.getRootElement();
                //item.setOrders(getItemOrders(root.selectNodes("//" + DmXmlConstants.ELM_ORDERS + "/"
                //        + DmXmlConstants.ELM_ORDER)));
                item.setOrders(getItemOrders(root.selectNodes("//" + DmXmlConstants.ELM_ORDER_DEFAULT)));
            }
        }
    }

    /**
     * load content properties from the content metadata
     *
     * @param path
     * @param item
     */
    @SuppressWarnings("unchecked")
    protected void loadContentProperties(String path, DmContentItemTO item) {
        // read common metadata
        Serializable internalNameValue = getPropertyValue(path, CStudioContentModel.PROP_INTERNAL_NAME);
        String internalName = (internalNameValue != null) ? internalNameValue.toString() : null;
        // set internal name
        if (!StringUtils.isEmpty(internalName)) {
            item.setInternalName(internalName);
            Serializable titleValue = getPropertyValue(path, CStudioContentModel.PROP_TITLE);
            String title = (titleValue != null) ? titleValue.toString() : null;
            if (!StringUtils.isEmpty(title)) item.setTitle(title);
        } else {
            Serializable titleValue = getPropertyValue(path, CStudioContentModel.PROP_TITLE);
            String title = (titleValue != null) ? titleValue.toString() : null;
            if (!StringUtils.isEmpty(title)) {
                item.setInternalName(title);
                item.setTitle(title);
            }
        }
        Serializable metaDescriptionValue = getPropertyValue(path, CStudioContentModel.PROP_META_DESCRIPTION);
        String metaDescription = (metaDescriptionValue != null) ? metaDescriptionValue.toString() : null;
        if (metaDescription != null)
            item.setMetaDescription(metaDescription);

        // set other status flags
        Serializable floatingValue = getPropertyValue(path, CStudioContentModel.PROP_FLOATING);
        Boolean floating = (Boolean) floatingValue;
        if (floating != null) {
            item.setFloating(floating);
        } else {
            item.setFloating(false);
        }
        Serializable disabledValue = getPropertyValue(path, CStudioContentModel.PROP_DISABLED);
        Boolean disabled = (Boolean) disabledValue;
        if (disabled != null) {
            item.setDisabled(disabled);
        } else {
            item.setDisabled(false);
        }
        // set orders
        Serializable ordersValue = getPropertyValue(path, CStudioContentModel.PROP_ORDER_DEFAULT);
        List<String> orders = (List<String>) ordersValue;
        item.setOrders(getItemOrdersFromProperty(orders));
    }

    /**
     * read orders from the list of text (stored in the format of orderName:orderValue)
     *
     * @param values
     * @return
     */
    protected List<DmOrderTO> getItemOrdersFromProperty(List<String> values) {
        if (values != null) {
            List<DmOrderTO> orders = new FastList<DmOrderTO>(values.size());
            for (String value : values) {
                if (!StringUtils.isEmpty(value)) {
                    String[] pair = value.split(":");
                    if (pair.length == 2) {
                        addOrderValue(orders, pair[0], pair[1]);
                    } else {
                        if (logger.isDebugEnabled()) {
                            logger.debug(value + " is not a valid order value. expected orderName:orderValue.");
                        }
                    }
                }
            }
            return orders;
        }
        return null;
    }

    /**
     * add order value to the list of orders
     *
     * @param orders
     * @param orderName
     * @param orderStr
     */
    protected void addOrderValue(List<DmOrderTO> orders, String orderName, String orderStr) {
        Double orderValue = null;
        try {
            orderValue = Double.parseDouble(orderStr);
        } catch (NumberFormatException e) {
            if (logger.isDebugEnabled()) {
                logger.debug(orderName + ", " + orderStr + " is not a valid order value pair.");
            }
        }
        if (!StringUtils.isEmpty(orderName) && orderValue != null) {
            DmOrderTO order = new DmOrderTO();
            order.setId(orderName);
            order.setOrder(orderValue);
            orders.add(order);
        }
    }

    /**
     * load properties from XML
     *
     * @param path
     * @param item
     * @throws ServiceException
     */
    @SuppressWarnings("unchecked")
    protected Document loadPropertiesFromXml(String site, String path, DmContentItemTO item) throws ServiceException {
        Document content = getDocumentFromDmContent(path);
        if (content != null) {
            Element root = content.getRootElement();
            String internalName = root.valueOf("//" + DmXmlConstants.ELM_INTERNAL_NAME);
            if (!StringUtils.isEmpty(internalName)) {
                item.setInternalName(internalName);
                String title = root.valueOf("//" + DmXmlConstants.ELM_TITLE);
                item.setTitle(title);
            }
            // if no content type from metadata, read it from xml
            if (StringUtils.isEmpty(item.getContentType())) {
                String contentType = root.valueOf("//" + DmXmlConstants.ELM_CONTENT_TYPE);
                if (!StringUtils.isEmpty(contentType)) {
                    item.setContentType(contentType);
                    loadContentTypeProperties(site, item, contentType);
                }
            }
            if (StringUtils.isEmpty(item.getMetaDescription())) {
                item.setMetaDescription(root.valueOf("//" + DmXmlConstants.ELM_META_DESCRIPTION));
            }
            if (StringUtils.isEmpty(item.getInternalName())) {
                String title = root.valueOf("//" + DmXmlConstants.ELM_TITLE);
                item.setInternalName(title);
                item.setTitle(title);
            }
            if (!item.isComponent() && !item.isDocument()) {
                item.setFloating(ContentFormatUtils.getBooleanValue(root.valueOf("//" + DmXmlConstants.ELM_FLOATING)));
                item.setDisabled(ContentFormatUtils.getBooleanValue(root.valueOf("//" + DmXmlConstants.ELM_DISABLED)));
                item.setNavigation(ContentFormatUtils.getBooleanValue(root.valueOf("//" + DmXmlConstants.ELM_PLACEINNAV)));
                //item.setOrders(getItemOrders(root.selectNodes("//" + DmXmlConstants.ELM_ORDERS + "/"
                //        + DmXmlConstants.ELM_ORDER)));
                item.setOrders(getItemOrders(root.selectNodes("//" + DmXmlConstants.ELM_ORDER_DEFAULT)));
            }
        }
        return content;
    }

    /**
     * get document from wcm content
     *
     * @param path
     * @return document
     * @throws ServiceException
     */
    protected Document getDocumentFromDmContent(String path) throws ServiceException {
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        NodeRef nodeRef = persistenceManagerService.getNodeRef(path);
        if (nodeRef == null) {
            if (logger.isInfoEnabled()) {
                logger.info("Node ref is null: " + path);
            }
            return null;
        }
        ContentReader reader = persistenceManagerService.getReader(nodeRef);
        SAXReader saxReader = new SAXReader();
        InputStream is = null;
        try {
            is = reader.getContentInputStream();
            return saxReader.read(is);
        } catch (DocumentException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Failed to read content from " + path, e);
            }
            return null;
            //throw new ServiceException("Failed to read content from " + path, e);
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Failed to read content from " + path, e);
            }
            return null;
        } finally {
            ContentUtils.release(is);
        }
    }

    /**
     * get WCM content item order metadata
     *
     * @param nodes
     * @return
     */
    protected List<DmOrderTO> getItemOrders(List<Node> nodes) {
        if (nodes != null) {
            List<DmOrderTO> orders = new FastList<DmOrderTO>(nodes.size());
            for (Node node : nodes) {
                //String orderName = node.valueOf(DmXmlConstants.ELM_ORDER_NAME);
                //String orderStr = node.valueOf(DmXmlConstants.ELM_ORDER_VALUE);

                String orderName = DmConstants.JSON_KEY_ORDER_DEFAULT;
                String orderStr = node.getText();
                addOrderValue(orders, orderName, orderStr);
            }
            return orders;
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    protected Document loadHideInAuthoringPropertyFromXml(String site, String path, DmContentItemTO item) throws ServiceException {
        Document content = getDocumentFromDmContent(path);
        if (content != null) {
            Element root = content.getRootElement();
            String hideInAuthoringValue = root.valueOf("//" + DmXmlConstants.ELM_HIDE_INAUTHORING);
            if (hideInAuthoringValue != null && !"".equals(hideInAuthoringValue)) {
                boolean hideInAuthoring = ContentFormatUtils.getBooleanValue(hideInAuthoringValue);
                item.setHideInAuthoring(hideInAuthoring);
            }
        }
        return content;
    }

    /**
     * populate any additional properties
     *
     * @param item
     */
    protected void populateProperties(NodeRef node, DmContentItemTO item) {
        if (logger.isDebugEnabled()) {
            logger.debug("Populating content properties for " + item.toString());
        }
        if (_propertyLoaders != null) {
            for (ContentPropertyLoader loader : _propertyLoaders) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Populating content properties by " + loader.getName());
                }
                if (loader.isEligible(node, item)) {
                    loader.loadProperties(node, item);
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("item is not eligible for " + loader.getName());
                    }
                }
            }
        }
    }

    /**
     * populate any additional properties
     *
     * @param item
     */
    protected void populateProperties(FileInfo node, DmContentItemTO item) {
        if (logger.isDebugEnabled()) {
            logger.debug("Populating content properties for " + item.toString());
        }
        if (_propertyLoaders != null) {
            for (ContentPropertyLoader loader : _propertyLoaders) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Populating content properties by " + loader.getName());
                }
                if (loader.isEligible(node.getNodeRef(), item)) {
                    loader.loadProperties(node.getNodeRef(), item);
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("item is not eligible for " + loader.getName());
                    }
                }
            }
        }
    }

    //
//    @Override
//    public Document getContentAsDocument(String site, String relativePath) throws AccessDeniedException, ContentNotFoundException {
//        return null;  //To change body of implemented methods use File | Settings | File Templates.
//    }
//
    @Override
    public DmContentItemTO createDummyDmContentItemForDeletedNode(String site, String relativePath) {
        //AuthenticationUtil.setFullyAuthenticatedUser(_servicesConfig.getSandbox(site));
        String absolutePath = getContentFullPath(site, relativePath);
        DmPathTO path = new DmPathTO(absolutePath);
        DmContentItemTO item = new DmContentItemTO();
        ServicesConfig servicesConfig = getService(ServicesConfig.class);
        String timeZone = servicesConfig.getDefaultTimezone(site);
        item.setTimezone(timeZone);
        String name = path.getName();
        //String relativePath = path.getRelativePath();
        String fullPath = path.toString();
        String folderPath = (name.equals(DmConstants.INDEX_FILE)) ? relativePath.replace("/" + name, "") : relativePath;
        item.setPath(folderPath);
        /**
         * Internal name should be just folder name
         */
        String internalName = folderPath;
        int index = folderPath.lastIndexOf('/');
        if (index != -1)
            internalName = folderPath.substring(index + 1);

        item.setInternalName(internalName);
        item.setTitle(internalName);
        item.setDisabled(false);
        item.setNavigation(false);
        item.setName(name);
        item.setUri(relativePath);

        item.setDefaultWebApp(path.getDmSitePath());
        //set content type based on the relative Path
        String contentType = getContentType(site, relativePath);
        item.setContentType(contentType);
        if (contentType.equals(DmConstants.CONTENT_TYPE_COMPONENT)) {
            item.setComponent(true);
        } else if (contentType.equals(DmConstants.CONTENT_TYPE_DOCUMENT)) {
            item.setDocument(true);
        }
        // set if the content is new
        item.setDeleted(true);
        item.setContainer(false);
        item.setNewFile(false);
        item.setNew(false);
        item.setInProgress(false);
        item.setTimezone(servicesConfig.getDefaultTimezone(site));
        item.setPreviewable(false);
        item.setBrowserUri(getBrowserUri(item));

        return item;
    }

    @Override
    public boolean contentExists(String site, String relativePath, String originalPath) {
        String fullPath = getContentFullPath(site, relativePath);
        try {
            if (fullPath.endsWith("/" + DmConstants.INDEX_FILE)) {
                fullPath = DmUtils.getParentUrl(fullPath);
            }
            PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
            NodeRef contentNode = persistenceManagerService.getNodeRef(fullPath);

            //deleted content but not in staging
            if (contentNode == null) { //&& contentNode.isDeleted() && contentInStaging == null) {
                return false;
            }

            //we get the node if deleted as well and check if its a revert rename case which is allowed
            DmRenameService dmRenameService = getService(DmRenameService.class);
            if (!dmRenameService.isRevertRename(site, originalPath, relativePath)) {
                return true;
            } else {
                return false;
            }
        } catch (AVMBadArgumentException e) {
            // this exception means that the path is not valid. ignore the error
            return false;
        }
    }

    /*
     * (non-Javadoc)
	 *
	 * @see
	 * org.craftercms.cstudio.alfresco.dm.service.api.DmContentService#getItems
	 * (java.lang.String, java.lang.String, java.lang.String, int, boolean,
	 * java.lang.String, boolean)
	 */
    @SuppressWarnings("unchecked")
    public DmContentItemTO getItems(String site, String sub, String relativePath, int depth, boolean isPage,
                                    String orderName, boolean checkChildren) throws ContentNotFoundException {
        return getItems(null, site, sub, relativePath, depth, isPage, orderName, checkChildren);
    }

    @SuppressWarnings("unchecked")
    public DmContentItemTO getItems(String site, String sub, String relativePath, int depth, boolean isPage,
                                    String orderName, boolean checkChildren, boolean populateDependencies) throws ContentNotFoundException {
        return getItems(null, site, sub, relativePath, depth, isPage, orderName, checkChildren, populateDependencies);
    }

    @Override
    public DmContentItemTO getItems(DmContentItemTO rootItem, String site, String sub, String relativePath, int depth, boolean isPage, String orderName, boolean checkChildren) throws ContentNotFoundException {
        return getItems(rootItem, site, sub, relativePath, depth, isPage, orderName, checkChildren, true);
    }

    @Override
    public DmContentItemTO getItems(DmContentItemTO rootItem, String site, String sub, String relativePath, int depth, boolean isPage, String orderName, boolean checkChildren, boolean populateDependencies) throws ContentNotFoundException {
        long start = System.currentTimeMillis();
        try {
            PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
            String fullPath = getContentFullPath(site, relativePath);
            if(!persistenceManagerService.exists(fullPath)){
            	return new DmContentItemTO();//Return a empty so other parts dont fail
            }
            NodeRef parentNodeRef = persistenceManagerService.getNodeRef(fullPath);
            FileInfo parentFileInfo = persistenceManagerService.getFileInfo(parentNodeRef);
            boolean deletedParent = false; // by marking this, it will mark all child items as deleted too

            //if root item is passed as null, create content item else use it.
            if (rootItem == null)
                rootItem = createDmContentItem(site, parentFileInfo, true, false);
            if (!parentFileInfo.isFolder()) {
                return rootItem;
            }
            List<DmContentItemTO> items = null;
            FastComparator<DmContentItemTO> comparator = new DmContentItemOrderComparator(orderName, true, true, true);
            ServicesConfig servicesConfig = getService(ServicesConfig.class);
            List<String> excludePaths = servicesConfig.getExcludePaths(site);
            if (!excludePaths.contains(relativePath)) {
                // depth < 0 means read all
                if (depth != 0 || depth < 0) {
                    depth--;
                    List<FileInfo> children = persistenceManagerService.list(parentNodeRef);
                    if (children != null) {
                        items = new FastTable<DmContentItemTO>(children.size());
                        for (FileInfo child : children) {
                            if (!isTemp(child)) {
                                DmContentItemTO item = getChildItem(site, child.getNodeRef(), depth, isPage, comparator, excludePaths, checkChildren, deletedParent, populateDependencies);
                                if (item != null) {
                                    if (deletedParent) {
                                        item.setDeleted(true);
                                        item.setPreviewable(false);
                                    }
                                    if (isPage && DmConstants.INDEX_FILE.equalsIgnoreCase(child.getName())) {
                                        rootItem = item;
                                        rootItem.setContainer(true);
                                    } else {
                                        items.add(item);
                                    }
                                }
                            } else {
                                if (logger.isDebugEnabled()) {
                                    logger.debug("SKIPPING " + persistenceManagerService.getNodePath(child.getNodeRef()));
                                }
                            }
                        }
                    }
                }
            }
            items = (items == null) ? new FastTable<DmContentItemTO>(0) : items;
            // sort the items
            long currTime = System.currentTimeMillis();
            ((FastTable) (items)).setValueComparator(comparator);
            ((FastTable) (items)).sort();
            if (logger.isDebugEnabled()) {
                logger.debug("Time took to sort items: " + (System.currentTimeMillis() - currTime));
            }
            rootItem.setChildren(items);
            return rootItem;
        } finally {
            long end = System.currentTimeMillis();
            if (logger.isDebugEnabled()) {
                logger.debug("getItems [" + relativePath + "] time[" + (end - start) + "]");
            }
        }
    }

    protected boolean isTemp(FileInfo child) {
        return child.isFolder() && child.getName().equals("tmp");
    }


    /**
     * create DMContent Item with DM related properties
     *
     * @param site
     * @param isFolder
     * @param isDeleted
     * @param node
     * @return
     */
    protected DmContentItemTO createDmContentItem(String site, FileInfo node, boolean isFolder, boolean isDeleted) {
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        NodeRef ndRef = node.getNodeRef();
        String dmPath = persistenceManagerService.getNodePath(ndRef);
        DmPathTO path = new DmPathTO(dmPath);
        String name = node.getName();
        try {
            return persistenceManagerService.getContentItem(dmPath);
        } catch (ServiceException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;
    }

    /**
     * get a property value from the content at the specified path
     *
     * @param path
     * @return property value
     */
    protected Serializable getPropertyValue(String path, QName name) {
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        NodeRef nodeRef = persistenceManagerService.getNodeRef(path);
        if (nodeRef != null) {
            FileInfo fileInfo = persistenceManagerService.getFileInfo(nodeRef);
            Map<QName, Serializable> props = fileInfo.getProperties();
            Serializable value = props.get(name);
            return value;
        }
        return null;
    }

    /**
     * load content type properties to the given content item
     *
     * @param site
     * @param item
     * @param contentType
     */
    protected void loadContentTypeProperties(String site, DmContentItemTO item, String contentType) {
        ServicesConfig servicesConfig = getService(ServicesConfig.class);
        ContentTypeConfigTO config = servicesConfig.getContentTypeConfig(site, contentType);
        if (config != null) {
            item.setForm(config.getForm());
            item.setFormPagePath(config.getFormPath());
            item.setPreviewable(config.isPreviewable());
        }
    }

    /**
     * get child item
     *
     * @param site
     * @param node
     * @param depth
     * @param isPage        if this is set to true, getCotnentItem call will look for
     *                      index.xml to load folder metadata otherwise, it will simply
     *                      return folder info only (e.g. name and URI)
     * @param comparator
     * @param excludePaths  a list paths to exclude from reading child items
     * @param checkChildren
     * @param deletedParent is the parent item of this child deleted?
     * @return child page
     */
    @SuppressWarnings("unchecked")
    protected DmContentItemTO getChildItem(String site, NodeRef node, int depth, boolean isPage,
                                           FastComparator<DmContentItemTO> comparator, List<String> excludePaths, boolean checkChildren, boolean deletedParent, boolean populateDependencies) {
        long currTime = System.currentTimeMillis();
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        try {
            if (node != null && (depth != 0 || depth < 0)) {
                String nodePath = persistenceManagerService.getNodePath(node);
                FileInfo nodeFileInfo = persistenceManagerService.getFileInfo(node);
                DmContentItemTO rootItem = persistenceManagerService.getContentItem(nodePath, populateDependencies);
                if (nodeFileInfo.isFolder()) {
                    if (persistenceManagerService.exists(nodePath + "/" + DmConstants.INDEX_FILE)) {
                        rootItem = persistenceManagerService.getContentItem(nodePath + "/" + DmConstants.INDEX_FILE, populateDependencies);
                    }
                    DmPathTO path = new DmPathTO(nodePath);
                    if (!excludePaths.contains(path.getRelativePath())) {
                        List<FileInfo> children = persistenceManagerService.list(node);
                        if (children != null) {
                            List<DmContentItemTO> items = new FastTable<DmContentItemTO>(children.size());
                            depth--;
                            for (FileInfo child : children) {
                                if (!isTemp(child)) {
                                    DmContentItemTO item = getChildItem(site, child.getNodeRef(), depth, isPage, comparator, excludePaths, checkChildren, deletedParent, populateDependencies);
                                    if (item != null) {
                                        if (deletedParent) {
                                            item.setDeleted(true);
                                            item.setPreviewable(false);
                                        }
                                        if (isPage && DmConstants.INDEX_FILE.equalsIgnoreCase(child.getName())) {
                                            rootItem = item;
                                            rootItem.setContainer(true);
                                        } else {
                                            items.add(item);
                                        }
                                    }
                                    /*if (item != null && !rootItem.getBrowserUri().equals(item.getBrowserUri())) {
                                            items.add(item);
                                    } */
                                }
                            }
                            // sort the items
                            ((FastTable) (items)).setValueComparator(comparator);
                            ((FastTable) (items)).sort();
                            rootItem.setChildren(items);
                        }
                    }
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("Time took to getting child item - " + nodePath + ": " + (System.currentTimeMillis() - currTime));
                }
                return rootItem;
            } else {
                String nodePath = persistenceManagerService.getNodePath(node);
                DmContentItemTO contentItem = null;
                try {
                    FileInfo fileInfo = persistenceManagerService.getFileInfo(node);
                    String indexPath = nodePath + "/" + DmConstants.INDEX_FILE;
                    if (fileInfo.isFolder() && persistenceManagerService.exists(indexPath)) {
                        contentItem = persistenceManagerService.getContentItem(indexPath, populateDependencies);
                        contentItem.setContainer(true);
                    } else {
                        contentItem = persistenceManagerService.getContentItem(nodePath, populateDependencies);
                    }
                } catch (ContentNotFoundException e) {
                    if (logger.isDebugEnabled()) {
                        logger.debug(nodePath + " could be deleted");
                    }
                    return null;  //This is a deleted Item
                }
                // if this is a leaf node and call is set to check whether there are children of the current item
                // check on # of items under this content and populate in the numOfChildren
                FileInfo nodeFileInfo = persistenceManagerService.getFileInfo(node);
                if (checkChildren && nodeFileInfo.isFolder()) {
                    List<FileInfo> children = persistenceManagerService.list(node);
                    if (children != null) {
                        if (children.size() == 1 && children.get(0).getName().equals(DmConstants.INDEX_FILE)) {
                            contentItem.setNumOfChildren(0);
                        } else {
                            int no = 0;
                            for (FileInfo child : children) {
                                if (!isTemp(child) && !child.getName().equals(DmConstants.INDEX_FILE)) {
                                    no++;
                                }
                            }
                            contentItem.setNumOfChildren(no);
                        }
                    }
                }
                return contentItem;
            }
        } catch (ServiceException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Failed to get content item at " + persistenceManagerService.getNodePath(node) + " site: " + site, e);
            }
            return null;
        } finally {
            long end = System.currentTimeMillis();
            if (logger.isDebugEnabled()) {
                logger.debug("getChilditem [" + persistenceManagerService.getNodePath(node) + "] time[" + (end - currTime) + "]");
            }
        }
    }

    /*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.craftercms.cstudio.alfresco.wcm.service.api.WcmContentService#getOrders
	 * (java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
    public List<DmOrderTO> getOrders(String site, String relativePath, String sub, String orderName, boolean includeFloating)
            throws ContentNotFoundException {
        //AuthenticationUtil.setFullyAuthenticatedUser(_servicesConfig.getSandbox(site));
        // if the path ends with index.xml, remove index.xml and also remove the last folder
        // otherwise remove the file name only
        if (!StringUtils.isEmpty(relativePath)) {
            if (relativePath.endsWith(DmConstants.XML_PATTERN)) {
                int index = relativePath.lastIndexOf("/");
                if (index > 0) {
                    String fileName = relativePath.substring(index + 1);
                    String path = relativePath.substring(0, index);
                    if (DmConstants.INDEX_FILE.equals(fileName)) {
                        int secondIndex = path.lastIndexOf("/");
                        if (secondIndex > 0) {
                            path = path.substring(0, secondIndex);
                        }
                    }
                    relativePath = path;
                }
            }
        }
        // get the root item and its children
        DmContentItemTO item = getItems(site, sub, relativePath, 1, true, orderName, false);
        //DmContentItemTO item = persistenceManagerService.getItems(site, sub, relativePath, 1, true, orderName, false);
        if (item.getChildren() != null) {
            List<DmOrderTO> orders = new FastList<DmOrderTO>(item.getChildren().size());
            String pathIndex = relativePath + "/" + DmConstants.INDEX_FILE;
            for (DmContentItemTO child : item.getChildren()) {
                // exclude index.xml, the level descriptor and floating pages at the path
                if (!(pathIndex.equals(child.getUri()) || child.isLevelDescriptor() || child.isDeleted()) && (!child.isFloating() || includeFloating)) {
                    DmOrderTO order = new DmOrderTO();
                    order.setId(child.getUri());
                    Double orderNumber = child.getOrder(orderName);
                    // add only if the page contains order information
                    if (orderNumber != null && orderNumber > 0) {
                        order.setOrder(child.getOrder(orderName));
                        order.setName(child.getInternalName());
                        if (child.isDisabled())
                            order.setDisabled("true");
                        else
                            order.setDisabled("false");

                        if (child.isNavigation())
                            order.setPlaceInNav("true");
                        else
                            order.setPlaceInNav("false");

                        orders.add(order);
                    }
                }
            }
            return orders;
        }
        return null;
    }

    /*
      * (non-Javadoc)
      *
      * @see
      * org.craftercms.cstudio.alfresco.wcm.service.api.WcmContentService#reOrderContent
      * (java.lang.String, java.lang.String, java.lang.String, java.lang.String,
      * java.lang.String, java.lang.String)
      */
    public double reOrderContent(String site, String relativePath, String sub, String before, String after,
                                 String orderName) throws ServiceException {
        //AuthenticationUtil.setFullyAuthenticatedUser(_servicesConfig.getSandbox(site));
        Double beforeOrder = null;
        Double afterOrder = null;
        DmOrderTO beforeOrderTO = null;
        DmOrderTO afterOrderTO = null;
        String fullPath = getContentFullPath(site, relativePath);
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        // get the order of the content before
        // if the path is not provided, the order is 0
        if (!StringUtils.isEmpty(before)) {
            String beforePath = getContentFullPath(site, before);
            DmContentItemTO beforeItem = persistenceManagerService.getContentItem(beforePath);
            beforeOrder = beforeItem.getOrder(orderName);
            beforeOrderTO = new DmOrderTO();
            beforeOrderTO.setId(before);
            if (beforeOrder != null && beforeOrder > 0) {
                beforeOrderTO.setOrder(beforeOrder);
            }
        }
        // get the order of the content after
        // if the path is not provided, the order is the order of before +
        // ORDER_INCREMENT
        if (!StringUtils.isEmpty(after)) {
            String afterPath = getContentFullPath(site, after);
            DmContentItemTO afterItem = persistenceManagerService.getContentItem(afterPath);
            afterOrder = afterItem.getOrder(orderName);
            afterOrderTO = new DmOrderTO();
            afterOrderTO.setId(after);
            if (afterOrder != null && afterOrder > 0) {
                afterOrderTO.setOrder(afterOrder);
            }
        }

        // if no after and before provided, the initial value is ORDER_INCREMENT
        DmPageNavigationOrderService dmPageNavigationOrderService = getService(DmPageNavigationOrderService.class);
        if (afterOrder == null && beforeOrder == null) {
            return dmPageNavigationOrderService.getNewNavOrder(site, DmUtils.getParentUrl(relativePath.replace("/" + DmConstants.INDEX_FILE, "")));
        } else if (beforeOrder == null) {
            return (0 + afterOrder) / 2;
        } else if (afterOrder == null) {
            if (logger.isInfoEnabled()) {
                logger.info("afterOrder == null");
            }
            return dmPageNavigationOrderService.getNewNavOrder(site, DmUtils.getParentUrl(relativePath.replace("/" + DmConstants.INDEX_FILE, "")));
        } else {
            //return (beforeOrder + afterOrder) / 2;
            return computeReOrder(site, sub, relativePath, beforeOrderTO, afterOrderTO, orderName);
        }
    }

    /**
     * Will need to include the floating pages as well for orderValue computation
     * Since the beforeOrder and afterOrder in the UI does not include floating pages will need to do special processing
     */
    protected double computeReOrder(String site, String sub, String relativePath, DmOrderTO beforeOrderTO, DmOrderTO afterOrderTO, String orderName) throws ContentNotFoundException {

        List<DmOrderTO> orderTO = getOrders(site, relativePath, sub, orderName, true);
        Collections.sort(orderTO);

        int beforeIndex = orderTO.indexOf(beforeOrderTO);
        int afterIndex = orderTO.indexOf(afterOrderTO);

        if (!(beforeIndex + 1 == afterIndex)) {
            beforeOrderTO = orderTO.get(afterIndex - 1);
        }
        return (beforeOrderTO.getOrder() + afterOrderTO.getOrder()) / 2;
    }

    @Override
    public boolean isNew(String site, String path) {
        try {
            String fullPath = getContentFullPath(site, path);
            PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
            NodeRef contentNode = persistenceManagerService.getNodeRef(fullPath);
            if (contentNode == null) {
                return true;
            } else {
                FileInfo contentNodeInfo = persistenceManagerService.getFileInfo(contentNode);
                if (contentNodeInfo.isFolder()) {
                    List<FileInfo> children = persistenceManagerService.listFiles(contentNode);
                    for (FileInfo child : children) {
                        if (child.getName().equals(DmConstants.INDEX_FILE)) {
                            contentNode = child.getNodeRef();
                            break;
                        }
                    }
                }
                String version = (String) persistenceManagerService.getProperty(contentNode, ContentModel.PROP_VERSION_LABEL);
                if (version == null) {
                    return true;
                }
                if ("1.0".compareTo(version) > 0) {
                    return true;
                } else {
                    return false;
                }
            }
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Cannot find the asset info at " + path, e);
            }
            return false;
        }
    }

    /**
     * check if the given content is updated or newly created by comparing the
     * content status in staging sandbox
     *
     * @param site
     * @param relativePath
     * @return true if the content is updated or newly created
     */
    @Override
    public boolean isUpdatedOrNew(String site, String relativePath) {
        String fullPath = getContentFullPath(site, relativePath);
        try {
            PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
            NodeRef node = persistenceManagerService.getNodeRef(fullPath);
            if (node != null) {
                FileInfo fileInfo = persistenceManagerService.getFileInfo(node);
                if (fileInfo.isFolder()) {
                    return false;
                }
                if (persistenceManagerService.isUpdatedOrNew(node)) {
                    return true;
                }
                String versionLabel = (String) persistenceManagerService.getProperty(node, ContentModel.PROP_VERSION_LABEL);
                if (versionLabel == null) {
                    return false;
                }
                if ("1.0".compareTo(versionLabel) > 0) {
                    return true;
                }
                String[] verisonNumbers = versionLabel.split("\\.");
                if (verisonNumbers[1].compareTo("0") > 0) {
                    return true;
                } else {
                    return false;
                }
            } else {

                // TODO: implement deleted check
            }
        } catch (AVMException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Cannot find the content at " + fullPath, e);
            }
        }
        return false;
    }

    /*
      * (non-Javadoc)
      * @see org.craftercms.cstudio.alfresco.wcm.service.api.WcmContentService#processContent(java.lang.String, java.io.InputStream, boolean, java.util.Map, java.lang.String)
      */
    @Override
    public ResultTO processContent(String id, InputStream input, boolean isXml, final Map<String, String> params, final String chainName) throws ServiceException {
        // get sandbox if not provided
        long start = System.currentTimeMillis();
        try {

            String[] strings = id.split(":");
            final String path = strings[1];
            final String site = strings[0];
            final String sub = params.get(DmConstants.KEY_SUB);
            /*if (isXml) {
                _cleanDraftDependencies(params, site);
            }*/
            ResultTO to = doSave(id, input, isXml, params, chainName, path, site);
            final String user = params.get(DmConstants.KEY_USER);
            if (isXml) {
                //DmTransactionService dmTransactionService = getService(DmTransactionService.class);
                //TransactionHelper tx = dmTransactionService.getTransactionHelper();
                //tx.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback() {
                //    @Override
                //    public Object execute() throws Throwable {
                String parentPagePath = params.get(DmConstants.KEY_FOLDER_PATH);
                String parentFileName = params.get(DmConstants.KEY_FILE_NAME);
                String parentPath = parentPagePath + "/" + parentFileName;
                DmDependencyService dmDependencyService = getService(DmDependencyService.class);
                PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
                DmDependencyTO dmDependencyTO = dmDependencyService.getDependencies(site, null, parentPath, false, true);
                List<DmDependencyTO> dmDependencyTOList = new FastList<DmDependencyTO>();
                if (dmDependencyTO != null) {
                    dmDependencyTOList = dmDependencyTO.flattenChildren();
                }
                for (DmDependencyTO dependencyTO : dmDependencyTOList) {
                    String depFullPath = getContentFullPath(site, dependencyTO.getUri());
                    NodeRef node = persistenceManagerService.getNodeRef(depFullPath);
                    DraftStatus draftCopyStatus = getDraftCopyStatus(depFullPath, site);
                    switch (draftCopyStatus) {
                        case MODIFIED:
                            if (logger.isDebugEnabled()) {
                                logger.debug("New draft copy for " + depFullPath);
                            }
                            boolean isNew = node == null;
                            String uri = dependencyTO.getUri();
                            String fileName = uri.substring(uri.lastIndexOf("/") + 1, uri.length());
                            String pathHelp = isNew || !DmUtils.isContentXML(uri) ? DmUtils.getParentUrl(dependencyTO.getUri()) : uri;
                            String idHelp = site + ":" + path + ":" + fileName + ":" + "";
                            InputStream fromDraft = getContentFromDraft(site, dependencyTO.getUri(), false, false);
                            HashMap<String, String> paramsHelp = new HashMap<String, String>();
                            paramsHelp.put(DmConstants.KEY_SITE, site);
                            paramsHelp.put(DmConstants.KEY_SUB, sub);
                            paramsHelp.put(DmConstants.KEY_PATH, pathHelp);
                            paramsHelp.put(DmConstants.KEY_CREATE_FOLDERS, "true");
                            paramsHelp.put(DmConstants.KEY_EDIT, "false");
                            paramsHelp.put(DmConstants.KEY_USER, user);
                            //params.put(DmConstants.KEY_SANDBOX, sandBox);
                            paramsHelp.put(DmConstants.KEY_FILE_NAME, fileName);
                            String contentType = getContentType(site, uri);
                            boolean isXML = false;
                            String dependencyChainName = "";
                            doSave(idHelp, fromDraft, DmUtils.isContentXML(uri), paramsHelp, getChainNameByContentType(site, uri, true), pathHelp, site);
                            break;
                        case DELETED:
                            if (logger.isDebugEnabled()) {
                                logger.debug("Draft copy deleted for" + depFullPath);
                            }

                    }

                }
                //return null;
                //    }
                //});
            }
            return to;

        } finally {
            AuthenticationUtil.setFullyAuthenticatedUser(params.get(DmConstants.KEY_USER));
            long end = System.currentTimeMillis();
            if (logger.isDebugEnabled()) {
                logger.debug("Write complete for [" + id + "] in time [" + (end - start) + "]");
            }
        }
    }

    public enum DraftStatus {
        DELETED,
        MODIFIED,
        NOCHANGE
    }

    protected DraftStatus getDraftCopyStatus(String nodePath, String site) {
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        ServicesConfig servicesConfig = getService(ServicesConfig.class);
        /* Disable DRAFT repo Dejan 29.03.2012 */
        String repoRootPath = servicesConfig.getRepositoryRootPath(site);
        /*
        String workBasePath = _servicesConfig.getRepositoryRootPath(site);
        String repoRootPath;

        repoRootPath = DmUtils.cleanRepositoryPath(workBasePath);
        */

        NodeRef node = persistenceManagerService.getNodeRef(repoRootPath + "/" + nodePath);
        /*
    	NodeRef node = DmUtils.getNodeRef(_nodeService, nodePath); //TODO:tag coment nodePath => work absolute path
    	  */
        /*if (nodePath.startsWith(repoRootPath)) {  // Managing new temp folder
            nodePath = nodePath.substring(repoRootPath.length());
        }*/
        //String s = DmUtils.getPreviewPath(nodePath);
        /*
        String absoluteDraftPath = DmUtils.getAbsoluteDraftPath(nodePath, workBasePath); //TODO:tag Both started:/wem-projects/site/site/work-area/
        */
        // AVMNodeDescriptor previewNode = _avmService.lookup(-1, s);
        //TODO
        /*
        NodeRef previewNode = DmUtils.getNodeRef(_nodeService, absoluteDraftPath);
        */
        NodeRef previewNode = node;
        /*
        if (previewNode == null) {
        	previewNode = DmUtils.getNodeRef(_nodeService, repoRootPath + absoluteDraftPath); //TODO:tag Saving page repoRootPath should
        																	// be = /wem-projects/acmecom/acmecom/
        	  																// s= absolute path but temp
        }
        */
        /****************************************/
        if (node == null && previewNode == null) {
            return DraftStatus.NOCHANGE;
        }
        if (previewNode == null) {
            return DraftStatus.DELETED;
        }
        if (node == null) {
            return DraftStatus.MODIFIED;
        }


        Serializable lastEditVal = persistenceManagerService.getProperty(node, CStudioContentModel.PROP_WEB_LAST_EDIT_DATE);
        Date lastEdit = (lastEditVal == null) ? null : (Date) lastEditVal;
        Serializable previewLastEditVal = persistenceManagerService.getProperty(previewNode, CStudioContentModel.PROP_WEB_LAST_EDIT_DATE);
        Date previewLastEdit = (previewLastEditVal == null) ? null : (Date) previewLastEditVal;
        if (lastEdit == null || previewLastEdit == null) {
            if (logger.isWarnEnabled()) {
                logger.warn("Last edit date is not set for [" + nodePath + "]");
            }
            return DraftStatus.NOCHANGE;
        }
        boolean change = previewLastEdit.getTime() > lastEdit.getTime();
        return change ? DraftStatus.MODIFIED : DraftStatus.NOCHANGE;
    }

    protected ResultTO doSave(String id, InputStream input, boolean isXml, Map<String, String> params, String chainName, final String path, final String site) throws ServiceException {
        //AuthenticationUtil.setFullyAuthenticatedUser(_servicesConfig.getSandbox(site));
        String fullPath = getContentFullPath(site, path);
        if (fullPath.endsWith("/")) fullPath = fullPath.substring(0, fullPath.length() - 1);
        ResultTO to = _contentProcessor.processContent(id, input, isXml, params, chainName);
        GoLiveQueue queue = (GoLiveQueue) _cache.get(Scope.DM_SUBMITTED_ITEMS, CStudioConstants.DM_GO_LIVE_CACHE_KEY, site);
        if (queue != null) {
            queue.remove(path);
        }
        return to;
    }

    public String getChainNameByContentType(String site, String uri) {
        return getChainNameByContentType(site, uri, false);
    }

    public String getChainNameByContentType(String site, String uri, boolean assetCleanWithClean) {
        String contentType = getContentType(site, uri);
        if (uri.endsWith(DmConstants.XML_PATTERN)) {
            return DmConstants.CONTENT_CHAIN_FORM;
        } else if (contentType.equals(DmConstants.CONTENT_TYPE_ASSET) && assetCleanWithClean) {
            return DmConstants.CONTENT_CHAIN_ASSET_CLEAN_DRAFT;
        } else if (contentType.equals(DmConstants.CONTENT_TYPE_ASSET)) {
            return DmConstants.CONTENT_CHAIN_ASSET;
        }
        return "";
    }

    @Override
    public String getContentFullPath(String site, final String relativePath) {
        return getContentFullPath(site, relativePath, true);
    }

    public String getContentFullPath(String site, final String relativePath, boolean isPathRelative) {
        String s = _cache.generateKey(site);
        String basepath = (String) _cache.get(Scope.DM_SITE, s);
        ServicesConfig servicesConfig = getService(ServicesConfig.class);
        if (null == basepath) {
            basepath = servicesConfig.getRepositoryRootPath(site);
            _cache.put(Scope.DM_SITE, s, basepath);
        }
        if (basepath == null) {
            return null;
        }
        if (isPathRelative) {
            return basepath + relativePath;
        } else {
            return basepath;
        }
    }

    @Override
    public void cancelEditing(String site, String path) {
        String fullPath = getContentFullPath(site, path);
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        NodeRef node = persistenceManagerService.getNodeRef(fullPath);
        if (node != null) {
            ObjectStateService objectStateService = getService(ObjectStateService.class);
            persistenceManagerService.setSystemProcessing(node, true);

            if (persistenceManagerService.getLockStatus(node).equals(LockStatus.LOCK_OWNER))
                persistenceManagerService.unlock(node);
            DmDependencyService dmDependencyService = getService(DmDependencyService.class);
            DmDependencyTO dmDependencyTO = dmDependencyService.getDependencies(site, null, path, false, true);
            List<DmDependencyTO> dmDependencyTOList = new FastList<DmDependencyTO>();
            if (dmDependencyTO != null) {
                dmDependencyTOList = dmDependencyTO.flattenChildren();
            }
            for (DmDependencyTO dependencyTO : dmDependencyTOList) {
                String depFullPath = getContentFullPath(site, dependencyTO.getUri());
                DraftStatus draftCopyStatus = getDraftCopyStatus(depFullPath, site);
                switch (draftCopyStatus) {
                    case MODIFIED:
                    case DELETED:
                        try {
                            DmPreviewService dmPreviewService = getService(DmPreviewService.class);
                            dmPreviewService.cleanContent(site, dependencyTO.getUri());
                        } catch (ServiceException e) {
                            if (logger.isErrorEnabled()) {
                                logger.error("Could not clean from preview path [" + path + "]", e);
                            }
                        } catch (RuntimeException e) {
                            if (logger.isErrorEnabled()) {
                                logger.error("Could not clean from preview path [" + path + "]", e);
                            }
                        }

                }

            }
            objectStateService.transition(node, ObjectStateService.TransitionEvent.CANCEL_EDIT);
            persistenceManagerService.setSystemProcessing(node, false);
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug(fullPath + " no longer exists. skipping dependency content cleanning.");
            }
        }
    }

    /*
	 * (non-Javadoc)
	 * @see org.craftercms.cstudio.alfresco.wcm.service.api.WcmContentService#getContentXml(java.lang.String, java.lang.String, java.lang.String)
	 */
    @Override
    public Document getContentXml(String site, String sub, String path) throws AccessDeniedException, ContentNotFoundException {
        InputStream is = null;
        InputStreamReader isReader = null;
        try {
            is = getContent(site, path, false, false);
            isReader = new InputStreamReader(is, CStudioConstants.CONTENT_ENCODING);
            SAXReader saxReader = new SAXReader();
            Document content = saxReader.read(isReader);
            return content;
        } catch (DocumentException e) {
            throw new ContentNotFoundException("Failed to load content. " + path + " is not a valid xml content.");
        } catch (UnsupportedEncodingException e) {
            throw new ContentNotFoundException("Failed to load content. " + path + " is not a valid xml content.");
        } finally {
            ContentUtils.release(is);
            ContentUtils.release(isReader);
        }
    }

    @Override
    public Document getContentXmlByVersion(String site, String sub, String path, String version) throws AccessDeniedException, ContentNotFoundException {
        InputStream is = null;
        try {
            is = getContentByVersion(site, path, false, version);
            SAXReader saxReader = new SAXReader();
            Document content = saxReader.read(is);
            return content;
        } catch (DocumentException e) {
            throw new ContentNotFoundException("Failed to load content. " + path + " is not a valid xml content.");
        } finally {
            ContentUtils.release(is);
        }
    }

    protected InputStream getContentByVersion(String site, String path, boolean edit, String version) throws AccessDeniedException, ContentNotFoundException {
        String fullPath = getContentFullPath(site, path);
        try {
            PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
            NodeRef contentNode = persistenceManagerService.getNodeRef(fullPath);
            if (contentNode != null) {
                if (edit) {
                    getService(PersistenceManagerService.class).lock(contentNode, LockType.WRITE_LOCK);
                }
                FileInfo contentNodeInfo = persistenceManagerService.getFileInfo(contentNode);
                if (contentNodeInfo.isFolder()) {
                    List<FileInfo> children = persistenceManagerService.list(contentNode);
                    for (FileInfo child : children) {
                        if (child.getName().equals(DmConstants.INDEX_FILE)) {
                            contentNode = child.getNodeRef();
                            break;
                        }
                    }
                }

                VersionHistory history = getService(PersistenceManagerService.class).getVersionHistory(contentNode);
                Version ver = history.getVersion(version);
                NodeRef contentVersionRef = ver.getFrozenStateNodeRef();
                return persistenceManagerService.getReader(contentVersionRef).getContentInputStream();
            } else {
                throw new ContentNotFoundException(path + " is not found in site: " + site);
            }
        } catch (AVMBadArgumentException e) {
            throw new ContentNotFoundException(path + " is not found in site: " + site, e);
        }
    }

    @Override
    public String getContentType(String site, String uri) {
        ServicesConfig servicesConfig = getService(ServicesConfig.class);
        if (matchesPatterns(uri, servicesConfig.getComponentPatterns(site)) || uri.endsWith("/" + servicesConfig.getLevelDescriptorName(site))) {
            return DmConstants.CONTENT_TYPE_COMPONENT;
        } else if (matchesPatterns(uri, servicesConfig.getDocumentPatterns(site))) {
            return DmConstants.CONTENT_TYPE_DOCUMENT;
        } else if (matchesPatterns(uri, servicesConfig.getAssetPatterns(site))) {
            return DmConstants.CONTENT_TYPE_ASSET;

        } else if (matchesPatterns(uri, servicesConfig.getRenderingTemplatePatterns(site))) {
            return DmConstants.CONTENT_TYPE_RENDERING_TEMPLATE;
        }
        return DmConstants.CONTENT_TYPE_PAGE;
    }

    /*
	 * (non-Javadoc)
	 * @see org.craftercms.cstudio.alfresco.wcm.service.api.WcmContentService#getNextAvailableName(java.lang.String, java.lang.String, java.lang.String)
	 */
    @Override
    public String getNextAvailableName(String site, String sub, String path) {
        String fullPath = getContentFullPath(site, path);
        String[] levels = path.split("/");
        int length = levels.length;
        if (length > 0) {
            try {
                PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
                NodeRef node = persistenceManagerService.getNodeRef(fullPath);
                if (node != null) {
                    FileInfo nodeInfo = persistenceManagerService.getFileInfo(node);
                    String name = nodeInfo.getName();
                    //String parentPath = fullPath.replace("/" + name, "");
                    String parentPath = DmUtils.getParentUrl(fullPath);
                    NodeRef parentNode = persistenceManagerService.getPrimaryParent(node).getParentRef();
                    if (parentNode != null) {
                        int lastIndex = name.lastIndexOf(".");
                        String ext = (nodeInfo.isFolder()) ? "" : name.substring(lastIndex);
                        String originalName = (nodeInfo.isFolder()) ? name : name.substring(0, lastIndex);
                        List<FileInfo> children = persistenceManagerService.list(parentNode);
                        // pattern matching doesn't work here
                        // String childNamePattern = originalName + "%" + ext;
                        int lastNumber = 0;
                        String namePattern = originalName + "\\-[0-9]+" + ext;
                        if (children != null && children.size() > 0) {
                            // since it is already sorted, we only care about the last matching item
                            for (FileInfo child : children) {
                                if ((nodeInfo.isFolder() == child.isFolder())) {
                                    String childName = child.getName();
                                    if (childName.matches(namePattern)) {
                                        Pattern pattern = (nodeInfo.isFolder()) ? COPY_FOLDER_PATTERN : COPY_FILE_PATTERN;
                                        Matcher matcher = pattern.matcher(childName);
                                        if (matcher.matches()) {
                                            int helper = ContentFormatUtils.getIntValue(matcher.group(2));
                                            lastNumber = (helper > lastNumber) ? helper : lastNumber;
                                        }
                                    }
                                }
                            }
                        }
                        String nextName = originalName + "-" + ++lastNumber + ext;
                        return nextName;
                    } else {
                        // if parent doesn't exist, it is new item so the current name is available one
                    }
                }
            } catch (AVMBadArgumentException e) {
                // ignore the exception
            }
        } else {
            // cannot generate a name
            return "";
        }
        // if not found the current name is available
        return levels[length - 1];
    }

    @Override
    public String getBrowserUri(String site, String relativeUri, String name) {
        boolean isComponent = false;
        boolean isLevelDescriptor = false;
        boolean isDocument = false;

        ServicesConfig servicesConfig = getService(ServicesConfig.class);
        isComponent = matchesPatterns(relativeUri, servicesConfig.getComponentPatterns(site));
        if (name.equals(servicesConfig.getLevelDescriptorName(site))) {
            isLevelDescriptor = true;
            // level descriptors are components
            isComponent = true;
        }
        // set document flag
        isDocument = matchesPatterns(relativeUri, servicesConfig.getDocumentPatterns(site));
        boolean isAsset = false;

        if (!name.endsWith(DmConstants.XML_PATTERN))
            isAsset = true;

        String replacePattern = "";
        if (isLevelDescriptor) {
            replacePattern = DmConstants.ROOT_PATTERN_PAGES;
        } else if (isComponent) {
            replacePattern = DmConstants.ROOT_PATTERN_COMPONENTS;
        } else if (isAsset) {
            replacePattern = DmConstants.ROOT_PATTERN_ASSETS;
        } else if (isDocument) {
            replacePattern = DmConstants.ROOT_PATTERN_DOCUMENTS;
        } else {
            replacePattern = DmConstants.ROOT_PATTERN_PAGES;
        }
        boolean isPage = !(isComponent || isAsset || isDocument);
        return DmUtils.getBrowserUri(relativeUri, replacePattern, isPage);
    }

    /**
     * get the browser URI of the given item
     * e.g. /products_services/xenapp/ROI.html for /site/website/products_servcies/xenapp/ROI.xml
     *
     * @param item
     * @return browser URI
     */
    protected String getBrowserUri(DmContentItemTO item) {
        String replacePattern = "";
        if (item.isLevelDescriptor()) {
            replacePattern = DmConstants.ROOT_PATTERN_PAGES;
        } else if (item.isComponent()) {
            replacePattern = DmConstants.ROOT_PATTERN_COMPONENTS;
        } else if (item.isAsset()) {
            replacePattern = DmConstants.ROOT_PATTERN_ASSETS;
        } else if (item.isDocument()) {
            replacePattern = DmConstants.ROOT_PATTERN_DOCUMENTS;
        } else {
            replacePattern = DmConstants.ROOT_PATTERN_PAGES;
        }
        boolean isPage = !(item.isComponent() || item.isAsset() || item.isDocument());
        return DmUtils.getBrowserUri(item.getUri(), replacePattern, isPage);
    }


    /**
     * does the path matches one of URI patterns?
     *
     * @return true if the path matches one of URI patterns
     */
    protected boolean matchesPatterns(String uri, List<String> patterns) {
        if (patterns != null) {
            for (String pattern : patterns) {
                if (uri.matches(pattern)) {
                    return true;
                }
            }
        }
        return false;
    }


    @Override
    public boolean matchesDisplayPattern(String site, String uri) {
        ServicesConfig servicesConfig = getService(ServicesConfig.class);
        List<String> displayPatterns = servicesConfig.getDisplayInWidgetPathPatterns(site);
        if (DmUtils.matchesPattern(uri, displayPatterns)) {
            return true;
        }
        return false;
    }

    @Override
    public String getTextPropertyByRelativePath(String site, String relativePath, String propertyName) {
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        String val = "";
        try {
            String fullPath = getContentFullPath(site, relativePath);
            NamespaceService namespaceService = getService(NamespaceService.class);
            QName name = namespaceService.createQName(propertyName);
            NodeRef node = persistenceManagerService.getNodeRef(fullPath);
            if (node != null) {
                Serializable propVal = persistenceManagerService.getProperty(node, name);
                val = (String) propVal;
            }
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error in reading " + propertyName + " property of " + relativePath + ". " + e);
            }
        }
        return val;
    }

    @Override
    public void createFolder(String site, String path, String name) throws ServiceException {
        CStudioNodeService cStudioNodeService = getService(CStudioNodeService.class);
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        String fullPath = getContentFullPath(site, path);
        NodeRef parentNode = persistenceManagerService.getNodeRef(fullPath);
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>(11);
        properties.put(ContentModel.PROP_NAME, name);
        NodeRef folderNodeRef = null;
        try {
            folderNodeRef = cStudioNodeService.createNewFolder(parentNode, name, properties);
        } catch (DuplicateChildNodeNameException e) {
            String[] message = {"Error while creating ", name, "folder at ", path, " in ", site};
            if (logger.isErrorEnabled()) {
                logger.error(org.craftercms.cstudio.alfresco.util.StringUtils.createMessage(message), e);
            }
            String[] message2 = {name, " exists already at ", path};
            throw new ServiceException(org.craftercms.cstudio.alfresco.util.StringUtils.createMessage(message2));
        }
        if (folderNodeRef == null) {
            String[] message = {"Error while creating ", name, "folder at ", path, " in ", site};
            throw new ServiceException(org.craftercms.cstudio.alfresco.util.StringUtils.createMessage(message));
        }
    }

    @Override
    public GoLiveDeleteCandidates getDeleteCandidates(String site, String relativePath) throws ServiceException {
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        List<String> items = new FastList<String>();
        String fullPath = _getFullPathToDelete(site, relativePath);
        NodeRef contentNode = persistenceManagerService.getNodeRef(fullPath);
        GoLiveDeleteCandidates deletedItems = new GoLiveDeleteCandidates(site, this);
        if (contentNode != null) {
            childDeleteItems(site, contentNode, deletedItems);
            //update summary for all uri's delete
        }
        //AuthenticationUtil.setFullyAuthenticatedUser(user);
        return deletedItems;
    }

    protected String _getFullPathToDelete(String site, String relativePath) {
        String path = relativePath;
        if (relativePath.endsWith(DmConstants.INDEX_FILE)) {
            path = relativePath.replaceFirst("/" + DmConstants.INDEX_FILE, "");
        }
        if(isPathFullPath(path))
        	return path;
        else
        	return getContentFullPath(site, path);
    }

    @Override
    public boolean isPathFullPath(String path) {
		return path.matches(DmConstants.DM_REPO_TYPE_PATH_PATTERN.pattern());
	}

	/**
     * Iterate over all paths inside the folder
     */
    protected void childDeleteItems(String site, NodeRef contentNode, GoLiveDeleteCandidates items) throws ServiceException {
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        String contentPath = persistenceManagerService.getNodePath(contentNode);
        FileInfo contentInfo = persistenceManagerService.getFileInfo(contentNode);
        DmPathTO path = new DmPathTO(contentPath);
        DmWorkflowService dmWorkflowService = getService(DmWorkflowService.class);
        //dmWorkflowService.removeFromWorkflow(site, null, path.getRelativePath(), true);
        if (contentInfo.isFolder()) {
            List<FileInfo> children = persistenceManagerService.list(contentNode);
            if (children != null) {
                for (FileInfo child : children) {
                    childDeleteItems(site, child.getNodeRef(), items);
                }
            }
        } else {
        	if(!path.getAreaName().equalsIgnoreCase(DmConstants.DM_LIVE_REPO_FOLDER)){
        		addDependenciesToDelete(site, path.getRelativePath(), path.getRelativePath(), items,false);	
        		addRemovedDependenicesToDelete(site, path.getRelativePath(), items);
        	}
        }
        //add the child path
        items.getPaths().add(contentPath);
    }

    /**
     * Recursively compute the dependency to be deleted based on configuration provided for the content type of the sourcePage
     */
    protected void addDependenciesToDelete(String site, String sourceContentPath, String dependencyPath, GoLiveDeleteCandidates candidates,boolean isLiveRepo) throws ServiceException {
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        Set<String> dependencyParentFolder = new HashSet<String>();
        //add dependencies as well

        DmDependencyService dmDependencyService = getService(DmDependencyService.class);
        Set<DmDependencyTO> dmDependencyTOs = dmDependencyService.getDeleteDependencies(site, sourceContentPath, dependencyPath,isLiveRepo);
        for (DmDependencyTO dependency : dmDependencyTOs) {
            if (candidates.addDependency(dependency.getUri())) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Added to delete" + dependency.getUri());
                }
                if (dependency.isDeleteEmptyParentFolder()) {
                    dependencyParentFolder.add(DmUtils.getParentUrl(dependency.getUri()));
                }
            }
            addDependenciesToDelete(site, sourceContentPath, dependency.getUri(), candidates,false); //recursively add dependencies of the dependency
        }

        //Find if any folder would get empty if remove the items and add just the folder
        for (String parentFolderToDelete : dependencyParentFolder) {
            String fullParentFolderPath = getContentFullPath(site, parentFolderToDelete);
            NodeRef parentNode = persistenceManagerService.getNodeRef(fullParentFolderPath);
            List<String> childItems = new FastList<String>();
            SearchService searchService = getService(SearchService.class);
            DmUtils.getChildrenUri(persistenceManagerService, searchService, parentNode, childItems);
            if (candidates.getAllItems().containsAll(childItems)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Added parentFolder for delete" + parentFolderToDelete);
                }
                candidates.addDependencyParentFolder(parentFolderToDelete);
            }
        }
    }

    /**
     * @param site
     * @param relativePath
     * @param candidates
     * @throws ServiceException
     */
    protected void addRemovedDependenicesToDelete(String site, String relativePath, GoLiveDeleteCandidates candidates) throws ServiceException {
        if (relativePath.endsWith(DmConstants.XML_PATTERN) && !isNew(site, relativePath)) {
            DiffRequest diffRequest = new DiffRequest(site, relativePath, null, null, site, true);
            DmDependencyService dmDependencyService = getService(DmDependencyService.class);
            List<String> deleted = dmDependencyService.getRemovedDependenices(diffRequest, true);
            if (logger.isDebugEnabled()) {
                logger.debug("Removed dependenices for path[" + relativePath + "] : " + deleted);
            }
            for (String dependency : deleted) {
                String dependencyFullPath = getContentFullPath(site, dependency);
                candidates.getLiveDependencyItems().add(dependencyFullPath);
            }
        }
    }

    @Override
    public Document getContentAsDocument(String site, String relativePath) throws AccessDeniedException, ContentNotFoundException {
        InputStream content = getContent(site, relativePath);
        Document document = null;
        try {
            document = ContentUtils.convertStreamToXml(content);
        } catch (DocumentException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return document;
    }

    @Override
    public List<String> deleteContents(String site, List<String> itemsToDelete,
                                       boolean generateActivity, String approver) throws ServiceException {
        approver = (StringUtils.isEmpty(approver)) ? getService(PersistenceManagerService.class).getCurrentUserName() : approver;
        if (itemsToDelete != null) {
            List<String> deletedItems = new FastList<String>();
            for (String path : itemsToDelete) {
                GoLiveDeleteCandidates candidate = deleteContent(site, path, true, generateActivity, approver);
                deletedItems.addAll(candidate.getAllItems());
            }
            return deletedItems;
        } else {
            return new FastList<String>(0);
        }
    }

    /**
     * returns full path of the deleted contents
     *
     * @param site
     * @param relativePath
     * @param generateActivity
     * @param approver
     * @return
     * @throws ServiceException
     */

    @Override
    public GoLiveDeleteCandidates deleteContent(String site, String relativePath, boolean recursive, boolean generateActivity, String approver) throws ServiceException {
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        approver = (StringUtils.isEmpty(approver)) ? persistenceManagerService.getCurrentUserName() : approver;
        Set<String> allItems = new HashSet<String>();
        GoLiveDeleteCandidates items = null;
        if (recursive) {
            items = getDeleteCandidates(site, relativePath);
            deleteDependency(site, items.getDependencies(), generateActivity, approver);
            if (items != null && items.getAllItems() != null) {
                allItems = items.getAllItems();
            }
        }
        if (generateActivity && items != null) {
            generateChildDeletionActivity(site, relativePath, items.getPaths(), approver);
        }

        ActivityService activityService = getService(ActivityService.class);

        for (String item : allItems) {
            if (item.endsWith(DmConstants.XML_PATTERN)) {
                try {
                    DmContentItemTO itemTo = persistenceManagerService.getContentItem(item);
                    if (itemTo != null) {
                        JSONObject jo = new JSONObject();
                        jo.put(CStudioConstants.CONTENT_TYPE, itemTo.getContentType());
                        jo.put(CStudioConstants.INTERNAL_NAME, itemTo.getInternalName());
                        jo.put(CStudioConstants.BROWSER_URI, itemTo.getBrowserUri());
                        String newsummary = jo.toString();
                        activityService.updateContentSummary(site, itemTo.getUri(), newsummary);
                    }
                } catch (Exception e) {
                    if (logger.isWarnEnabled()) {
                        logger.warn("Unable to update activity summary for " + item, e);
                    }
                }
            }
            if (logger.isDebugEnabled()) {
                logger.debug("deleted item" + item + "Updating cache");
            }
            DmPathTO pathTO = new DmPathTO(item);
            String path = pathTO.getRelativePath();
            GoLiveQueue queue = (GoLiveQueue) _cache.get(Scope.DM_SUBMITTED_ITEMS, CStudioConstants.DM_GO_LIVE_CACHE_KEY, site);
            if (null != queue) {
                queue.remove(path);
            }
        }

        delete(site, relativePath, generateActivity, approver);

        return items;
    }

    /**
     * Delete the paths provided
     *
     * @param site
     * @param fullPaths        - avmPath
     * @param generateActivity
     * @param approver
     */
    protected void deleteDependency(String site, Set<String> fullPaths, boolean generateActivity, String approver) {
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        for (String path : fullPaths) {
            DmPathTO dmPathTO = new DmPathTO(path);
            NodeRef assocNode = persistenceManagerService.getNodeRef(path);
            if (assocNode != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Deleting dependency " + path);
                }
                DmWorkflowService dmWorkflowService = getService(DmWorkflowService.class);
                dmWorkflowService.removeFromWorkflow(site, null, dmPathTO.getRelativePath(), true);
                deleteContent(site, assocNode, generateActivity, approver); //delete the content
            }
        }
    }

    /**
     * delete the given content
     *
     * @param site
     * @param contentNode
     * @param generateActivity
     * @param approver
     */
    protected void deleteContent(String site, NodeRef contentNode, boolean generateActivity, String approver) {
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        DmPathTO path = new DmPathTO(persistenceManagerService.getNodePath(contentNode));
        String contentName = (String) persistenceManagerService.getProperty(contentNode, ContentModel.PROP_NAME);
        if (generateActivity) {
            generateDeleteActivity(site, path, approver);
        }
        //removeContentFromSandbox(site, path, contentName, generateActivity, approver);
    }

    /**
     * generate deletion activity for each child item
     *
     * @param site
     * @param relativePath
     * @param paths
     * @param approver
     */
    protected void generateChildDeletionActivity(String site, String relativePath, Set<String> paths, String approver) {
        if (relativePath.endsWith(DmConstants.INDEX_FILE)) {
            relativePath = relativePath.replace("/" + DmConstants.INDEX_FILE, "");
        }
        // generate deletion activities for all candidates
        for (String path : paths) {
            DmPathTO dmPath = new DmPathTO(path);
            generateDeleteActivity(site, dmPath, approver);
        }
    }

    @Override
    public void generateDeleteActivity(String site, List<String> paths, String approver) {
        for (String path : paths) {
            DmPathTO dmPath = new DmPathTO();
            dmPath.setRelativePath(path);
            dmPath.setAreaName(DmConstants.DM_WORK_AREA_REPO_FOLDER);
            generateDeleteActivity(site, dmPath, approver);
        }
    }

    /**
     * generate delete activity
     *
     * @param site
     * @param path
     * @param approver
     */
    protected void generateDeleteActivity(String site, DmPathTO path, String approver) {
    	if(path.getAreaName().equalsIgnoreCase(DmConstants.DM_LIVE_REPO_FOLDER))
    		return; //live path does not have activity
        String currentUser = getService(PersistenceManagerService.class).getCurrentUserName();
        String fullPath = getContentFullPath(site, path.getRelativePath());
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        if (!path.getRelativePath().contains(".")) {
            fullPath = fullPath + "/" + DmConstants.INDEX_FILE;
            fullPath = path.toString();
            FileInfo nodeInfo = persistenceManagerService.getFileInfo(fullPath);
            if (nodeInfo == null || nodeInfo.isFolder()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("[DELETE] " + path.toString() + " is a folder. skipping delete activity generation.");
                }
                return;
            }
        }
        String relativePath = path.getRelativePath();
        NodeRef node = persistenceManagerService.getNodeRef(fullPath);
        if (node != null) {
            Serializable value = persistenceManagerService.getProperty(node, CStudioContentModel.PROP_WEB_WF_SUBMITTED_BY);
            String user = (value != null && !StringUtils.isEmpty((String) value)) ? (String) value : approver;
            Map<String, String> extraInfo = new HashMap<String, String>();
            if (relativePath.endsWith(DmConstants.XML_PATTERN)) {
                extraInfo.put(DmConstants.KEY_CONTENT_TYPE, this.getContentType(site, relativePath));
            }
            if (logger.isDebugEnabled()) {
                logger.debug("[DELETE] posting delete activity on " + relativePath + " by " + user + " in " + site);
            }
            ActivityService activityService = getService(ActivityService.class);
            activityService.postActivity(site, user, relativePath, ActivityType.DELETED, extraInfo);
            // process content life cycle
            if (relativePath.endsWith(DmConstants.XML_PATTERN)) {
                Serializable contentTypeValue = persistenceManagerService.getProperty(node, CStudioContentModel.PROP_CONTENT_TYPE);
                String contentType = (contentTypeValue != null) ? (String) contentTypeValue : null;
                DmContentLifeCycleService dmContentLifeCycleService = getService(DmContentLifeCycleService.class);
                dmContentLifeCycleService.process(site, user, relativePath,
                        contentType, ContentLifeCycleOperation.DELETE, null);
            }
        }
        // return authentication after posting activity
        AuthenticationUtil.setFullyAuthenticatedUser(currentUser);
    }

    /**
     * @param site
     * @param relativePath
     * @param generateActivity
     * @param approver
     */
    protected void delete(String site, String relativePath, boolean generateActivity, String approver) {
        String fullPathToDelete = _getFullPathToDelete(site, relativePath);
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        NodeRef contentNode1 = persistenceManagerService.getNodeRef(fullPathToDelete);
        if (contentNode1 != null) {

            deleteContent(site, contentNode1, generateActivity, approver);
            // put the original user's authentication back
        } else {
            if (logger.isWarnEnabled()) {
                logger.warn(relativePath + " is already deleted.");
            }
        }
    }

    /**
     * remove or revert the content from sandbox
     *
     * @param site
     * @param path
     * @param name
     * @param generateActivity
     * @param approver
     */
    protected void removeContentFromSandbox(String site, DmPathTO path, String name, boolean generateActivity, String approver) {
        try {
            if (generateActivity) {
                generateDeleteActivity(site, path, approver);
            }
            PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
            NodeRef nodeToDelete = persistenceManagerService.getNodeRef(path.toString());
            NodeRef unpublishedNode = nodeToDelete;
            FileInfo nodeInfo = persistenceManagerService.getFileInfo(nodeToDelete);
            if (nodeInfo.isFolder()) {
                unpublishedNode = persistenceManagerService.searchSimple(nodeToDelete, DmConstants.INDEX_FILE);
            }
            if (unpublishedNode != null) {
                if (!isNew(site, path.getRelativePath())) {
                    //delete results in conflicts, hence below code to Revert-Delete-Revert-Delete.
                    if (logger.isDebugEnabled()) {
                        logger.debug("removeContentFromSandbox - Remove [" + path.toString() + "]");
                    }
                    if (persistenceManagerService.getUnpublishEventsForNode(unpublishedNode).isEmpty()) {
                        persistenceManagerService.deleteNode(nodeToDelete);
                        persistenceManagerService.deleteObjectState(nodeToDelete.getId());
                    }
                    if (logger.isDebugEnabled()) {
                        logger.debug("Resolving conflicts " + path.toString());
                    }
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("removeContentFromSandbox - Page is New [" + path.toString() + "]");
                    }
                    if (persistenceManagerService.getUnpublishEventsForNode(unpublishedNode).isEmpty()) {
                        persistenceManagerService.deleteNode(nodeToDelete);
                        persistenceManagerService.deleteObjectState(nodeToDelete.getId());
                    }
                }
            } else {
                persistenceManagerService.deleteNode(nodeToDelete);
                persistenceManagerService.deleteObjectState(nodeToDelete.getId());
            }
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error while deleting content " + path, e);
            }
        }
    }
}
