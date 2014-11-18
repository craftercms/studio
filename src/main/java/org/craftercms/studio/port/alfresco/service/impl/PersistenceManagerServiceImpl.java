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
package org.craftercms.cstudio.alfresco.service.impl;

/**
 * @author Dejan Brkic
 * @author Carlos Ortiz
 */

import javolution.util.FastList;
import javolution.util.FastMap;
import org.alfresco.model.ContentModel;
import org.alfresco.model.WCMWorkflowModel;
import org.alfresco.repo.nodelocator.CompanyHomeNodeLocator;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.action.*;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.publishing.PublishingEvent;
import org.alfresco.service.cmr.publishing.PublishingService;
import org.alfresco.service.cmr.publishing.Status;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.rule.Rule;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.*;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.cmr.workflow.*;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.craftercms.cstudio.alfresco.activityfeed.CStudioActivityFeedDaoService;
import org.craftercms.cstudio.alfresco.cache.api.Cache;
import org.craftercms.cstudio.alfresco.cache.api.CacheItem;
import org.craftercms.cstudio.alfresco.constant.CStudioContentModel;
import org.craftercms.cstudio.alfresco.dm.constant.DmConstants;
import org.craftercms.cstudio.alfresco.dm.constant.DmXmlConstants;
import org.craftercms.cstudio.alfresco.dm.dependency.DependencyDaoService;
import org.craftercms.cstudio.alfresco.dm.service.api.DmDependencyService;
import org.craftercms.cstudio.alfresco.dm.to.DmContentItemTO;
import org.craftercms.cstudio.alfresco.dm.to.DmOrderTO;
import org.craftercms.cstudio.alfresco.dm.to.DmPathTO;
import org.craftercms.cstudio.alfresco.dm.util.DmUtils;
import org.craftercms.cstudio.alfresco.dm.util.api.ContentPropertyLoader;
import org.craftercms.cstudio.alfresco.objectstate.ObjectStateDAOService;
import org.craftercms.cstudio.alfresco.pagenavigationordersequence.PageNavigationOrderSequenceDaoService;
import org.craftercms.cstudio.alfresco.service.AbstractRegistrableService;
import org.craftercms.cstudio.alfresco.service.api.ObjectStateService;
import org.craftercms.cstudio.alfresco.service.api.PersistenceManagerService;
import org.craftercms.cstudio.alfresco.service.api.ProfileService;
import org.craftercms.cstudio.alfresco.service.api.ServicesConfig;
import org.craftercms.cstudio.alfresco.service.exception.ContentNotFoundException;
import org.craftercms.cstudio.alfresco.service.exception.ServiceException;
import org.craftercms.cstudio.alfresco.to.ContentTypeConfigTO;
import org.craftercms.cstudio.alfresco.to.UserProfileTO;
import org.craftercms.cstudio.alfresco.util.CStudioUtils;
import org.craftercms.cstudio.alfresco.util.ContentFormatUtils;
import org.craftercms.cstudio.alfresco.util.ContentUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.transaction.UserTransaction;
import java.io.InputStream;
import java.io.Serializable;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

public class PersistenceManagerServiceImpl extends AbstractRegistrableService implements PersistenceManagerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PersistenceManagerServiceImpl.class);

    // TODO CodeRev: lets put all member vars together in future
   
    protected Cache _cache;

    public Cache getCache() {
        return this._cache;
    }

    public void setCache(Cache cache) {
        this._cache = cache;
    }

    protected CompanyHomeNodeLocator _companyHomeNodeLocator;

    public CompanyHomeNodeLocator getCompanyHomeNodeLocator() {
        return _companyHomeNodeLocator;
    }

    public void setCompanyHomeNodeLocator(CompanyHomeNodeLocator companyHomeNodeLocator) {
        this._companyHomeNodeLocator = companyHomeNodeLocator;
    }

    private StoreRef companyHomeStore;

    public void setCompanyHomeStore(String companyHomeStore) {
        this.companyHomeStore = new StoreRef(companyHomeStore);
    }

    private String companyHomePath;

    public void setCompanyHomePath(String companyHomePath) {
        this.companyHomePath = companyHomePath;
    }

    /**
     * DAO services *
     */
    protected CStudioActivityFeedDaoService _cStudioActivityFeedDaoService;

    public CStudioActivityFeedDaoService getcStudioActivityFeedDaoService() {
        return _cStudioActivityFeedDaoService;
    }

    public void setcStudioActivityFeedDaoService(CStudioActivityFeedDaoService cStudioActivityFeedDaoService) {
        this._cStudioActivityFeedDaoService = cStudioActivityFeedDaoService;
    }

    protected DependencyDaoService _dependencyDaoService;

    public DependencyDaoService getDependencyDaoService() {
        return _dependencyDaoService;
    }

    public void setDependencyDaoService(DependencyDaoService dependencyDaoService) {
        this._dependencyDaoService = dependencyDaoService;
    }

    protected ObjectStateDAOService _objectStateDAOService;

    public ObjectStateDAOService getObjectStateDAOService() {
        return _objectStateDAOService;
    }

    public void setObjectStateDAOService(ObjectStateDAOService objectStateDAOService) {
        this._objectStateDAOService = objectStateDAOService;
    }

    protected PageNavigationOrderSequenceDaoService _pageNavigationOrderSequenceDaoService;

    public PageNavigationOrderSequenceDaoService getPageNavigationOrderSequenceDaoService() {
        return _pageNavigationOrderSequenceDaoService;
    }

    public void setPageNavigationOrderSequenceDaoService(PageNavigationOrderSequenceDaoService pageNavigationOrderSequenceDaoService) {
        this._pageNavigationOrderSequenceDaoService = pageNavigationOrderSequenceDaoService;
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

    protected BehaviourFilter policyBehaviourFilter;
    public BehaviourFilter getPolicyBehaviourFilter() {
        return policyBehaviourFilter;
    }
    public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
        this.policyBehaviourFilter = policyBehaviourFilter;
    }

    @Override
    public void register() {
        getServicesManager().registerService(PersistenceManagerService.class, this);
    }

    @Override
    public NodeRef getCompanyHomeNodeRef() {
        try {
            return getCompanyHomeNodeLocator().getNode(null, null);
        } catch (NullPointerException npe) {
        	// TODO CodeRev: when does this case come up?  Is this the only way we can detect and handle this scenario?
        	SearchService searchService = getService(SearchService.class);
            NodeService nodeService = getService(NodeService.class);
            NamespaceService namespaceService = getService(NamespaceService.class);
            List<NodeRef> refs = searchService.selectNodes(nodeService.getRootNode(companyHomeStore), companyHomePath, null, namespaceService, false);
            if (refs.size() > 0) {
                return refs.get(0);
            } else {
                throw new IllegalStateException("Invalid company home path: " + companyHomePath + " - found: " + refs.size());
            }
        }
    }

    @Override
    public NodeRef getNodeRef(String fullPath) {
        return getNodeRef(getCompanyHomeNodeRef(), fullPath);
    }

    @Override
    public NodeRef getNodeRef(String rootPath, String relativePath) {
        return getNodeRef(getNodeRef(rootPath), relativePath);
    }

    @Override
    public NodeRef getNodeRef(NodeRef rootNode, String relativePath) {
        NodeService nodeService = getService(NodeService.class);
        // TODO CodeRev: check for nulls
        // TODO CodeRev: throw exceptions on errors 
        String[] contentPathParts = relativePath.split("/");
        NodeRef helperNode = rootNode;
        for (int i = 0; i < contentPathParts.length; i++) {
            if (!"".equals(contentPathParts[i])) {
                NodeRef prev = helperNode;
                helperNode = nodeService.getChildByName(helperNode, ContentModel.ASSOC_CONTAINS, contentPathParts[i]);
                if (helperNode == null) {
                    break;
                }
            }
        }
        return helperNode;
    }

    @Override
    public String getNodePath(NodeRef nodeRef) {
        return getRelativeNodePath(getCompanyHomeNodeRef(), nodeRef);
    }

    @Override
    public String getRelativeNodePath(NodeRef rootNodeRef, NodeRef nodeRef) {
        StringBuilder sb = new StringBuilder();
        NodeService nodeService = getService(NodeService.class);
        Path nodePath = nodeService.getPath(nodeRef);
        boolean foundRootNodeRef = false;

        for (Path.Element element : nodePath) {
            Path.ChildAssocElement assocElement = (Path.ChildAssocElement) element;
            ChildAssociationRef childAssociationRef = assocElement.getRef();
            NodeRef elementNode = childAssociationRef.getChildRef();
            Map<QName, Serializable> props = getProperties(elementNode);
        
            if (foundRootNodeRef) {
                String pathPart = (String) props.get(ContentModel.PROP_NAME);
                sb.append("/").append(pathPart);
            }
            foundRootNodeRef = foundRootNodeRef || elementNode.equals(rootNodeRef);
        }
        return sb.toString();
    }

    @Override
    public DmContentItemTO getContentItem(String fullPath) throws ContentNotFoundException, ServiceException {
        return getContentItem(fullPath, true);
    }

    @Override
    public DmContentItemTO getContentItem(String fullPath, boolean populateDependencies) throws ContentNotFoundException, ServiceException {

        NodeService nodeService = getService(NodeService.class);
        ServicesConfig servicesConfig = getService(ServicesConfig.class);
        NodeRef nodeRef = getNodeRef(fullPath);
        if (nodeRef == null) {
            LOGGER.error("Content not found at: " + fullPath);
            throw new ContentNotFoundException();
        }
        FileInfo fileInfo = getFileInfo(nodeRef);
        Map<QName, Serializable> nodeProperties = nodeService.getProperties(nodeRef);
        String site = "";
        Matcher m = DmConstants.DM_REPO_PATH_PATTERN.matcher(fullPath);
        if (m.matches()) {
            site = m.group(3);
        }
        if (nodeRef == null) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Content not found at: " + fullPath);
            }
            throw new ContentNotFoundException("Content not found at: " + fullPath);
        }
        // Get content item from cache
        Cache cache = getCache();
        String cacheScope = generateCacheScope(site);
        DmContentItemTO contentItemTO = null;
        try {
            CacheItem cacheItem = cache.get(cacheScope, generateKey(fullPath, nodeRef.getId()));
            if (cacheItem != null) {
                contentItemTO = (DmContentItemTO) cacheItem.getValue();
            }
        } catch (Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Failed to get content item from cache: " + fullPath);
            }
        }
        if (contentItemTO == null) {
            // Content item not found
            // Step 1: Get content item from repository

            contentItemTO = createDmContentItem(site, fullPath, nodeRef, nodeProperties);
            String name = contentItemTO.getName();
            // if the node is an XML file, load content metadata from the file
            if (name.endsWith(DmConstants.XML_PATTERN)) {
                loadCommonProperties(site, fullPath, nodeRef, contentItemTO, nodeProperties);
            } else if (fileInfo.isFolder()) {
                contentItemTO.setComponent(false);
                contentItemTO.setContainer(true);
            } else {
                contentItemTO.setAsset(CStudioUtils.matchesPatterns(contentItemTO.getUri(), servicesConfig.getAssetPatterns(site)));
                contentItemTO.setRenderingTemplate(CStudioUtils.matchesPatterns(contentItemTO.getUri(), servicesConfig.getRenderingTemplatePatterns(site)));
                contentItemTO.setComponent(true);
            }
            contentItemTO.setBrowserUri(getBrowserUri(contentItemTO));

            populateProperties(nodeRef, contentItemTO);

            // Step 2: Put item into cache
            try {
                if (!_cache.hasScope(cacheScope)) {
                	// TODO CodeRev: What is 5000?  Should it be configurable?
                	_cache.addScope(cacheScope, 5000);
                }
                // TODO CodeRev: BUT shouldn't first check to see if we actually got the item?
                _cache.put(cacheScope, generateKey(fullPath, nodeRef.getId()), contentItemTO);
            } catch (Exception e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("Error while putting item to cache " + fullPath);
                }
            }
        }
        
        // TODO CodeRev: What if ContentTO is null?
        DmContentItemTO toRet = new DmContentItemTO(contentItemTO);
        // populate dependencies
        if (populateDependencies) {
            DmDependencyService dmDependencyService = getService(DmDependencyService.class);
            dmDependencyService.populateDependencyContentItems(site, toRet, false);
        }
        populateStateProperties(fullPath, nodeRef, nodeProperties, toRet);
        if (fileInfo.isFolder()) {
            ObjectStateService objectStateService = getService(ObjectStateService.class);
            toRet.setNew(!objectStateService.isFolderLive(fullPath));
        }

        return toRet;
    }

    protected void populateStateProperties(String fullPath, NodeRef nodeRef, Map<QName, Serializable> nodeProperties, DmContentItemTO contentItemTO) {
        ObjectStateService objectStateService = getService(ObjectStateService.class);
        ObjectStateService.State objectState = objectStateService.getObjectState(fullPath);

        contentItemTO.setInFlight(ObjectStateService.State.isSystemProcessing(objectState));

        contentItemTO.setNew(ObjectStateService.State.isNew(objectState));
        contentItemTO.setDeleted(ObjectStateService.State.isDeleted(objectState));
        contentItemTO.setInProgress(ObjectStateService.State.isUpdateOrNew(objectState));
        contentItemTO.setPreviewable(contentItemTO.isPreviewable() && !ObjectStateService.State.isDeleted(objectState));

        // get the lock owner
        String owner = DefaultTypeConverter.INSTANCE.convert(String.class, nodeProperties.get(ContentModel.PROP_LOCK_OWNER));
        contentItemTO.setLockOwner(owner);

        contentItemTO.setSubmitted(ObjectStateService.State.isSubmitted(objectState));
        if (contentItemTO.isSubmitted()) {
            contentItemTO.setInProgress(false);
            String submittedBy = DefaultTypeConverter.INSTANCE.convert(String.class, nodeProperties.get(CStudioContentModel.PROP_WEB_WF_SUBMITTED_BY));
            if (!StringUtils.isEmpty(submittedBy)) {
                ProfileService profileService = getService(ProfileService.class);
                UserProfileTO profile = profileService.getUserProfile(submittedBy, null, false);
                if (profile != null) {
                    contentItemTO.setSubmittedByFirstName(profile.getProfile().get(ContentModel.PROP_FIRSTNAME.getLocalName()));
                    contentItemTO.setSubmittedByLastName(profile.getProfile().get(ContentModel.PROP_LASTNAME.getLocalName()));
                }
            }
            String submissionComment = DefaultTypeConverter.INSTANCE.convert(String.class, nodeProperties.get(Version2Model.PROP_QNAME_VERSION_DESCRIPTION));
            if (!StringUtils.isEmpty(submissionComment)) {
                contentItemTO.setSubmissionComment(submissionComment);
            }
        }
        boolean isScheduled = ObjectStateService.State.isScheduled(objectState);
        contentItemTO.setScheduled(isScheduled);
        if (isScheduled) {
            Date date = DefaultTypeConverter.INSTANCE.convert(Date.class, nodeProperties.get(WCMWorkflowModel.PROP_LAUNCH_DATE));
            contentItemTO.setScheduledDate(date);
            contentItemTO.setInProgress(false);
            Boolean submittedForDelete = DefaultTypeConverter.INSTANCE.convert(Boolean.class, nodeProperties.get(CStudioContentModel.PROP_WEB_WF_SUBMITTEDFORDELETION));
            if (submittedForDelete != null) {
                contentItemTO.setSubmittedForDeletion(submittedForDelete);
            }
        }
    }

    // TODO CodeRev:Why concept of DM item?
    protected DmContentItemTO createDmContentItem(String site, String fullPath, NodeRef nodeRef, Map<QName, Serializable> nodeProperties) {
        DmPathTO path = new DmPathTO(fullPath);
        String name = DefaultTypeConverter.INSTANCE.convert(String.class, nodeProperties.get(ContentModel.PROP_NAME));

        String relativePath = path.getRelativePath();
        ServicesConfig servicesConfig = getService(ServicesConfig.class);
        String timeZone = servicesConfig.getDefaultTimezone(site);
        DmContentItemTO item = new DmContentItemTO();
        item.setTimezone(timeZone);
        item.setInternalName(name);
        item.setNodeRef(nodeRef.toString());
        boolean isDisabled = false;
        Serializable value = nodeProperties.get(CStudioContentModel.PROP_DISABLED);
        if (value != null) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("isDisabled: " + value.toString());
            }
            isDisabled = DefaultTypeConverter.INSTANCE.convert(Boolean.class, value);
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("isDisabled property not found");
            }
        }
        item.setDisabled(isDisabled);

        /**
         * Setting isNavigation property
         */
        boolean placeInNav = false;
        Serializable placeInNavProp = nodeProperties.get(CStudioContentModel.PROP_PLACEINNAV);
        if (placeInNavProp != null) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("placeInNav: " + placeInNavProp.toString());
            }
            placeInNav = DefaultTypeConverter.INSTANCE.convert(Boolean.class, placeInNavProp);
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("placeInNav property not found");
            }
        }
        item.setNavigation(placeInNav);

        item.setName(name);

        //Checks that the node is a folder, not if cont
        boolean isFolder = this.getFileInfo(nodeRef).isFolder();
        item.setContainer(isFolder || fullPath.endsWith("/" + DmConstants.INDEX_FILE));
        if (isFolder) {
            item.setUri(relativePath);
            String folderPath = (name.equals(DmConstants.INDEX_FILE)) ? relativePath.replace("/" + name, "") : relativePath;
            item.setPath(folderPath);
        } else {
            item.setUri(relativePath);
            if (relativePath != null) {
                int index = path.getRelativePath().lastIndexOf("/");
                if (index > 0) {
                    item.setPath(relativePath.substring(0, index));
                }
            }
        }
        item.setDefaultWebApp(path.getDmSitePath());

        Date lastEditDate = DefaultTypeConverter.INSTANCE.convert(Date.class,
            nodeProperties.get(CStudioContentModel.PROP_WEB_LAST_EDIT_DATE));
        if (lastEditDate == null) {
            lastEditDate = DefaultTypeConverter.INSTANCE.convert(Date.class,
                nodeProperties.get(ContentModel.PROP_MODIFIED));
        }
        item.setLastEditDate(lastEditDate);

        // default event date is the modified date
        item.setEventDate(DefaultTypeConverter.INSTANCE.convert(Date.class, nodeProperties.get(ContentModel.PROP_MODIFIED)));
        // read the author information
        String modifier = DefaultTypeConverter.INSTANCE.convert(String.class, nodeProperties.get(CStudioContentModel.PROP_LAST_MODIFIED_BY));
        if (modifier != null && !StringUtils.isEmpty(modifier.toString())) {
            item.setUser(modifier);
            ProfileService profileService = getService(ProfileService.class);
            UserProfileTO profile = profileService.getUserProfile(modifier, site, false);
            if (profile != null) {
                item.setUserFirstName(profile.getProfile().get(ContentModel.PROP_FIRSTNAME.getLocalName()));
                item.setUserLastName(profile.getProfile().get(ContentModel.PROP_LASTNAME.getLocalName()));
            }
        }

        // get the content type and form page info
        String contentType = DefaultTypeConverter.INSTANCE.convert(String.class, nodeProperties.get(CStudioContentModel.PROP_CONTENT_TYPE));
        if (contentType != null && !StringUtils.isEmpty(contentType)) {
            item.setContentType(contentType);
            loadContentTypeProperties(site, item, contentType);
        } else {
            FileNameMap fileNameMap = URLConnection.getFileNameMap();
            String mimeType = fileNameMap.getContentTypeFor(item.getUri());
            if (mimeType != null && !StringUtils.isEmpty(mimeType)) {
                item.setPreviewable(DmUtils.matchesPattern(mimeType, servicesConfig.getPreviewableMimetypesPaterns(site)));
            }
        }

        item.setTimezone(servicesConfig.getDefaultTimezone(site));

        return item;
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
        // TODO CodeRev:but what if the config is null?
    }

    /**
     * load common properties from the content
     *
     * @param fullPath
     * @param nodeRef
     * @param item
     * @throws org.craftercms.cstudio.alfresco.service.exception.ServiceException
     *
     */
    @SuppressWarnings("unchecked")
    protected void loadCommonProperties(String site, String fullPath, NodeRef nodeRef, DmContentItemTO item, Map<QName, Serializable> nodeProperties) throws ServiceException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("loading common properties for " + fullPath);
        }
        // set component flag
        ServicesConfig servicesConfig = getService(ServicesConfig.class);
        item.setComponent(CStudioUtils.matchesPatterns(item.getUri(), servicesConfig.getComponentPatterns(site)));
        if (item.getName().equals(servicesConfig.getLevelDescriptorName(site))) {
            item.setLevelDescriptor(true);
            // level descriptors are components
            item.setComponent(true);
        }
        // set document flag
        item.setDocument(CStudioUtils.matchesPatterns(item.getUri(), servicesConfig.getDocumentPatterns(site)));
        // if the content type meta is empty, populate properties from the file
        Document content = null;
        // read common metadata
        String internalName = DefaultTypeConverter.INSTANCE.convert(String.class, nodeProperties.get(CStudioContentModel.PROP_INTERNAL_NAME));
        if (!StringUtils.isEmpty(item.getContentType()) && !StringUtils.isEmpty(internalName)) {
            loadContentProperties(fullPath, nodeRef, item, nodeProperties);
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("content type is not found from the content metadata. loading properties from XML.");
            }
            content = loadPropertiesFromXml(site, fullPath, nodeRef, item);
        }

    }

    /**
     * load content properties from the content metadata
     */
    @SuppressWarnings("unchecked")
    protected void loadContentProperties(String fullPath, NodeRef nodeRef, DmContentItemTO item, Map<QName, Serializable> nodeProperties) {
        // read common metadata
        String internalName = DefaultTypeConverter.INSTANCE.convert(String.class, nodeProperties.get(CStudioContentModel.PROP_INTERNAL_NAME));
        // set internal name
        if (!StringUtils.isEmpty(internalName)) {
            item.setInternalName(internalName);
            String title = DefaultTypeConverter.INSTANCE.convert(String.class, nodeProperties.get(CStudioContentModel.PROP_TITLE));
            if (!StringUtils.isEmpty(title)) {
                item.setTitle(title);
            }
        } else {
            String title = DefaultTypeConverter.INSTANCE.convert(String.class, nodeProperties.get(CStudioContentModel.PROP_INTERNAL_NAME));
            if (!StringUtils.isEmpty(title)) {
                item.setInternalName(title);
                item.setTitle(title);
            }
        }
        String metaDescription = DefaultTypeConverter.INSTANCE.convert(String.class, nodeProperties.get(CStudioContentModel.PROP_META_DESCRIPTION));
        if (metaDescription != null) {
            item.setMetaDescription(metaDescription);
        }

        // set other status flags
        Boolean floating = DefaultTypeConverter.INSTANCE.convert(Boolean.class, nodeProperties.get(CStudioContentModel.PROP_FLOATING));
        if (floating != null) {
            item.setFloating(floating);
        } else {
            item.setFloating(false);
        }
        Boolean disabled = DefaultTypeConverter.INSTANCE.convert(Boolean.class, nodeProperties.get(CStudioContentModel.PROP_DISABLED));
        if (disabled != null) {
            item.setDisabled(disabled);
        } else {
            item.setDisabled(false);
        }
        // set orders
        Float orderDefault = DefaultTypeConverter.INSTANCE.convert(Float.class, nodeProperties.get(CStudioContentModel.PROP_ORDER_DEFAULT));
        if (orderDefault != null) {
            List<DmOrderTO> orders = new FastList<DmOrderTO>();
            addOrderValue(orders, DmConstants.JSON_KEY_ORDER_DEFAULT, orderDefault.toString());
            item.setOrders(orders);
        }
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
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug(value + " is not a valid order value. expected orderName:orderValue.");
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
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(orderName + ", " + orderStr + " is not a valid order value pair.");
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

    /**
     * load properties from XML
     *
     * @throws ServiceException
     */
    @SuppressWarnings("unchecked")
    protected Document loadPropertiesFromXml(String site, String fullPath, NodeRef nodeRef, DmContentItemTO item) throws ServiceException {
        Document content = getDocumentFromDmContent(fullPath, nodeRef);
  
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

                item.setNavigation(ContentFormatUtils.getBooleanValue(root.valueOf("//" + DmXmlConstants.ELM_PLACEINNAV)));
                //item.setOrders(getItemOrders(root.selectNodes("//" + DmXmlConstants.ELM_ORDERS + "/"
                //        + DmXmlConstants.ELM_ORDER)));
                item.setOrders(getItemOrders(root.selectNodes("//" + DmXmlConstants.ELM_ORDER_DEFAULT)));
            }
            item.setDisabled(ContentFormatUtils.getBooleanValue(root.valueOf("//" + DmXmlConstants.ELM_DISABLED)));
            String hideInAuthoringValue = root.valueOf("//" + DmXmlConstants.ELM_HIDE_INAUTHORING);
            if (hideInAuthoringValue != null && !"".equals(hideInAuthoringValue)) {
                boolean hideInAuthoring = ContentFormatUtils.getBooleanValue(hideInAuthoringValue);
                item.setHideInAuthoring(hideInAuthoring);
            }

            String isSkipDependenciesValue = root.valueOf("//" + DmXmlConstants.ELM_SKIP_DEPENDENCIES);
            if (isSkipDependenciesValue != null && !"".equals(isSkipDependenciesValue)) {
                boolean skipDependencies = ContentFormatUtils.getBooleanValue(isSkipDependenciesValue);
                item.setSkipDependencies(skipDependencies);
            }
        }
        return content;
    }

    /**
     * get document from dm content
     *
     * @param fullPath
     * @param nodeRef
     * @return document
     * @throws ServiceException
     */
    protected Document getDocumentFromDmContent(String fullPath, NodeRef nodeRef) throws ServiceException {
        if (nodeRef == null) {
            return null;
        }
        FileFolderService fileFolderService = getService(FileFolderService.class);
        ContentReader reader = fileFolderService.getReader(nodeRef);
        SAXReader saxReader = new SAXReader();
        InputStream is = null;
        try {
            is = reader.getContentInputStream();
            return saxReader.read(is);
        } catch (DocumentException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Failed to read content from " + fullPath, e);
            }
            return null;
        } catch (Exception e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Failed to read content from " + fullPath, e);
            }
            return null;
        } finally {
            ContentUtils.release(is);
            is = null;
            reader = null;
            saxReader.resetHandlers();
            saxReader = null;
        }
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
     * populate any additional properties
     *
     * @param item
     */
    protected void populateProperties(NodeRef nodeRef, DmContentItemTO item) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Populating content properties for " + item.toString());
        }
        if (_propertyLoaders != null) {
            for (ContentPropertyLoader loader : _propertyLoaders) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Populating content properties by " + loader.getName());
                }
                if (loader.isEligible(nodeRef, item)) {
                    loader.loadProperties(nodeRef, item);
                } else {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("item is not eligible for " + loader.getName());
                    }
                }
            }
        }
    }

    /*
    * (non-Javadoc)
    * @see org.craftercms.cstudio.alfresco.service.api.ContentService#getContentAsStream(org.alfresco.service.cmr.repository.NodeRef)
    */
    public InputStream getContentAsStream(NodeRef nodeRef) {
        FileFolderService fileFolderService = getService(FileFolderService.class);
        ContentReader reader = fileFolderService.getReader(nodeRef);
        return reader.getContentInputStream();
    }

    /*
      * (non-Javadoc)
      * @see org.craftercms.cstudio.alfresco.service.api.ContentService#getContentAsString(org.alfresco.service.cmr.repository.NodeRef)
      */
    public String getContentAsString(NodeRef nodeRef) {
        FileFolderService fileFolderService = getService(FileFolderService.class);
        ContentReader reader = fileFolderService.getReader(nodeRef);
        InputStream in = reader.getContentInputStream();
        return ContentUtils.convertStreamToString(in);
    }

    /*
      * (non-Javadoc)
      * @see org.craftercms.cstudio.alfresco.service.api.ContentService#getMimeType(org.alfresco.service.cmr.repository.NodeRef)
      */
    public String getMimeType(NodeRef nodeRef) {
        FileFolderService fileFolderService = getService(FileFolderService.class);
        ContentReader reader = fileFolderService.getReader(nodeRef);
        return reader.getMimetype();
    }

    /*
      * (non-Javadoc)
      *
      * @see org.craftercms.cstudio.alfresco.service.api.ContentService#loadXml(
      * org.alfresco.service.cmr.repository.NodeRef)
      */
    public Document loadXml(NodeRef nodeRef) {
        if (nodeRef == null) {
            return null;
        }
        FileFolderService fileFolderService = getService(FileFolderService.class);
        ContentReader contentReader = fileFolderService.getReader(nodeRef);
        InputStream in = null;
        SAXReader reader = new SAXReader();
        try {
            in = contentReader.getContentInputStream();
            Document document = reader.read(in);
            return document;
        } catch (Exception e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Failed to load a file.", e);
            }
        } finally {
            ContentUtils.release(in);
            in = null;
            contentReader = null;
            reader.resetHandlers();
            reader = null;
        }
        return null;
    }

    @Override
    public void setObjectState(String fullPath, ObjectStateService.State state) {
        setObjectState(getNodeRef(fullPath), state);
    }

    @Override
    public void setObjectState(NodeRef nodeRef, ObjectStateService.State state) {
        _objectStateDAOService.setObjectState(nodeRef.getId(), state);
    }

    @Override
    public void transition(NodeRef nodeRef, ObjectStateService.TransitionEvent event) {
        String fullPath = getNodePath(nodeRef);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[" + Thread.currentThread().getName() + "]" + " Executing state transition for path: " + fullPath + " and event: " + event.toString());
        }
        transition(fullPath, nodeRef, event);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[" + Thread.currentThread().getName() + "]" + " Finished state transition for path: " + fullPath + " and event: " + event.toString());
        }
    }

    @Override
    public void transition(String fullPath, ObjectStateService.TransitionEvent event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[" + Thread.currentThread().getName() + "]" + " Executing state transition for path: " + fullPath + " and event: " + event.toString());
        }
        NodeRef nodeRef = getNodeRef(fullPath);
        transition(fullPath, nodeRef, event);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[" + Thread.currentThread().getName() + "]" + " Finished state transition for path: " + fullPath + " and event: " + event.toString());
        }
    }

    protected void transition(String fullPath, NodeRef nodeRef, ObjectStateService.TransitionEvent event) {
        ObjectStateService objectStateService = getService(ObjectStateService.class);
        objectStateService.transition(nodeRef, event);
        if (ObjectStateService.TransitionEvent.isCacheInvalidateNeeded(event)) {
            String site = "";
            Matcher m = DmConstants.DM_REPO_PATH_PATTERN.matcher(fullPath);
            if (m.matches()) {
                site = m.group(3);
            }
            String cacheScope = generateCacheScope(site);
            try {
                _cache.remove(cacheScope, generateKey(fullPath, nodeRef.getId()));
            } catch (Exception e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("Error while invalidating cache for item " + fullPath, e);
                }
            }
        }
    }

    @Override
    public void setSystemProcessing(String fullPath, final boolean isSystemProcessing) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[" + Thread.currentThread().getName() + "]" + " Set system processing for path: " + fullPath);
        }
        ObjectStateService objectStateService = getService(ObjectStateService.class);
        NodeRef nodeRef = getNodeRef(fullPath);
        if (nodeRef != null) {
            objectStateService.setSystemProcessing(nodeRef, isSystemProcessing);
        } else {
            LOGGER.error(String.format("Error setting system processing flag. Content at path %s does not exist", fullPath));
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[" + Thread.currentThread().getName() + "]" + " Finished set system processing for path: " + fullPath);
        }
    }

    @Override
    public void setSystemProcessing(NodeRef nodeRef, final boolean isSystemProcessing) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[" + Thread.currentThread().getName() + "]" + " Set system processing for path: " + getNodePath(nodeRef));
        }
        ObjectStateService objectStateService = getService(ObjectStateService.class);
        objectStateService.setSystemProcessing(nodeRef, isSystemProcessing);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[" + Thread.currentThread().getName() + "]" + " Finished set system processing for path: " + getNodePath(nodeRef));
        }
    }

    @Override
    public void setSystemProcessingBulk(List<String> objectIds, final boolean isSystemProcessing) {
        if (objectIds == null || objectIds.isEmpty()) {
            return;
        }
        ObjectStateService objectStateService = getService(ObjectStateService.class);
        objectStateService.setSystemProcessingBulk(objectIds, isSystemProcessing);
    }

    /*
      * (non-Javadoc)
      *
      * @see org.craftercms.cstudio.alfresco.service.api.ContentService#writeContent(
      * org.alfresco.service.cmr.repository.NodeRef, java.lang.String,
      * java.io.InputStream)
      */
    public void writeContent(NodeRef nodeRef, String fileName, InputStream in) {
        FileFolderService fileFolderService = getService(FileFolderService.class);
        ContentWriter writer = fileFolderService.getWriter(nodeRef);
        MimetypeService mimetypeService = getService(MimetypeService.class);
        String mimeType = mimetypeService.guessMimetype(fileName);
        writer.setMimetype(mimeType);
        writer.putContent(in);
    }


    @Override
    public ObjectStateService.State getObjectState(String fullPath) {
        ObjectStateService objectStateService = getService(ObjectStateService.class);
        return objectStateService.getObjectState(getNodeRef(fullPath));
    }

    @Override
    public ObjectStateService.State getObjectState(NodeRef nodeRef) {
        ObjectStateService objectStateService = getService(ObjectStateService.class);
        return objectStateService.getObjectState(nodeRef);
    }

    @Override
    public Document loadXml(String fullPath) {
        return loadXml(getNodeRef(fullPath));
    }

    @Override
    public List<FileInfo> listFolders(String sitesConfigPath) {
        return listFolders(getNodeRef(sitesConfigPath));
    }

    @Override
    public List<FileInfo> listFolders(NodeRef nodeRef) {
        return getService(FileFolderService.class).listFolders(nodeRef);
    }

    @Override
    public Serializable getProperty(NodeRef siteRef, QName propName) {
        return getService(NodeService.class).getProperty(siteRef, propName);
    }

    /*
      * (non-Javadoc)
      *
      * @see
      * org.craftercms.cstudio.alfresco.service.api.NamespaceService#getType(
      * java.lang.String)
      */
    public QName createQName(String prefixedQName) {
        if (!StringUtils.isEmpty(prefixedQName) && prefixedQName.contains(":")) {
            org.alfresco.service.namespace.NamespaceService alfrescoNamespaceService = getServicesManager().getService(org.alfresco.service.namespace.NamespaceService.class);
            String[] values = prefixedQName.split(":");
            if (values.length == 2) {
                return QName.createQName(values[0], values[1], alfrescoNamespaceService);
            }
        }
        return null;
    }

    @Override
    public String getContentAsString(String fullPath) {
        return getContentAsString(getNodeRef(fullPath));
    }

    @Override
    public NodeRef getChildByName(NodeRef nodeRef, QName assocTypeQName, String childName) {
        return getService(NodeService.class).getChildByName(nodeRef, assocTypeQName, childName);
    }

    @Override
    public Path getPath(NodeRef nodeRef) {
        return getService(NodeService.class).getPath(nodeRef);
    }

    @Override
    public ChildAssociationRef createNode(NodeRef parentRef, QName assocTypeQName, QName assocQName, QName typeFolder, Map<QName, Serializable> nodeProperties) {
        return getService(NodeService.class).createNode(parentRef, assocTypeQName, assocQName, typeFolder, nodeProperties);
    }

    @Override
    public List<FileInfo> list(NodeRef parentNodeRef) {
        return getService(FileFolderService.class).list(parentNodeRef);
    }

    @Override
    public ContentWriter getWriter(NodeRef nodeRef, QName propertyQName, boolean update) {
        return getService(ContentService.class).getWriter(nodeRef, propertyQName, update);
    }

    @Override
    public ChildAssociationRef getPrimaryParent(NodeRef nodeRef) {
        return getService(NodeService.class).getPrimaryParent(nodeRef);
    }

    @Override
    public void deleteNode(NodeRef nodeRef) {
        String fullPath = getNodePath(nodeRef);
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Deleting path: " + fullPath);
        }
        DmPathTO dmPathTO = new DmPathTO(fullPath);
        String site = dmPathTO.getSiteName();
        String cacheScope = generateCacheScope(site);
        NodeService nodeService = getService(NodeService.class);
        nodeService.deleteNode(nodeRef);
        _cache.remove(cacheScope, generateKey(fullPath, nodeRef.getId()));
    }

    @Override
    public NodeRef copy(NodeRef sourceNodeRef, NodeRef targetParentNodeRef, QName assocTypeQName, QName assocQName) {
        return getService(CopyService.class).copy(sourceNodeRef, targetParentNodeRef, assocTypeQName, assocQName);
    }

    @Override
    public FileInfo getFileInfo(String fullPath) {
        NodeRef nodeRef = getNodeRef(fullPath);
        if (nodeRef != null) {
            return getFileInfo(nodeRef);
        } else {
            return null;
        }
    }

    @Override
    public FileInfo getFileInfo(NodeRef nodeRef) {
        return getService(FileFolderService.class).getFileInfo(nodeRef);
    }

    @Override
    public boolean exists(NodeRef nodeRef) {
        if (nodeRef == null) {
            return false;
        } else {
            return getService(FileFolderService.class).exists(nodeRef);
        }
    }

    @Override
    public boolean exists(String fullPath) {
        NodeRef nodeRef = getNodeRef(fullPath);
        if (nodeRef == null) {
            return false;
        }
        return exists(getNodeRef(fullPath));
    }

    @Override
    public LockStatus getLockStatus(NodeRef nodeRef) {
        return getService(LockService.class).getLockStatus(nodeRef);
    }

    @Override
    public void lock(NodeRef nodeRef, LockType lockType) {
        getService(LockService.class).lock(nodeRef, lockType);
    }

    @Override
    public void unlock(NodeRef nodeRef) {
        getService(LockService.class).unlock(nodeRef);

    }

    @Override
    public ContentReader getReader(NodeRef nodeRef) {
        return getService(FileFolderService.class).getReader(nodeRef);
    }

    @Override
    public List<FileInfo> listFiles(NodeRef contextNodeRef) {
        return getService(FileFolderService.class).listFiles(contextNodeRef);
    }

    @Override
    public List<FileInfo> getNamePath(NodeRef rootNodeRef, NodeRef nodeRef) throws FileNotFoundException {
        return getService(FileFolderService.class).getNamePath(rootNodeRef, nodeRef);
    }

    @Override
    public NodeRef searchSimple(NodeRef contextNodeRef, String name) {
        return getService(FileFolderService.class).searchSimple(contextNodeRef, name);
    }

    @Override
    public List<PublishingEvent> getUnpublishEventsForNode(NodeRef unpublishedNode) {
        return getService(PublishingService.class).getUnpublishEventsForNode(unpublishedNode);
    }

    @Override
    public String getCurrentUserName() {
        return getService(AuthenticationService.class).getCurrentUserName();
    }

    @Override
    public VersionHistory getVersionHistory(NodeRef nodeRef) {
        return getService(VersionService.class).getVersionHistory(nodeRef);
    }


    @Override
    public Set<String> getContainedAuthorities(AuthorityType type, String name, boolean immediate) {
        return getService(AuthorityService.class).getContainedAuthorities(type, name, immediate);
    }

    @Override
    public void addAuthority(String parentName, String childName) {
        getService(AuthorityService.class).addAuthority(parentName, childName);

    }

    @Override
    public Set<String> getContainingAuthorities(AuthorityType type, String name, boolean immediate) {
        return getService(AuthorityService.class).getContainingAuthorities(type, name, immediate);
    }

    @Override
    public void removeAuthority(String parentName, String childName) {
        getService(AuthorityService.class).removeAuthority(parentName, childName);
    }

    @Override
    public Set<String> getAuthoritiesForUser(String user) {
        return getService(AuthorityService.class).getAuthoritiesForUser(user);
    }

    @Override
    public FileInfo create(NodeRef parentNodeRef, String name, QName typeQName, QName assocQName) {
        return getService(FileFolderService.class).create(parentNodeRef, name, typeQName, assocQName);
    }

    @Override
    public boolean hasAspect(NodeRef nodeRef, QName aspectTypeQName) {
        return getService(NodeService.class).hasAspect(nodeRef, aspectTypeQName);
    }

    @Override
    public List<AssociationRef> getTargetAssocs(NodeRef sourceRef, RegexQNamePattern qnamePattern) {
        return getService(NodeService.class).getTargetAssocs(sourceRef, qnamePattern);
    }

    @Override
    public ContentWriter getWriter(String fullPath) {
        return getService(FileFolderService.class).getWriter(getNodeRef(fullPath));
    }

    @Override
    public void setProperty(NodeRef nodeRef, QName qname, Serializable value) {
        getService(NodeService.class).setProperty(nodeRef, qname, value);
    }

    @Override
    public Set<QName> getAspects(NodeRef nodeRef) {
        return getService(NodeService.class).getAspects(nodeRef);
    }

    @Override
    public void addAspect(NodeRef nodeRef, QName aspectTypeQName, Map<QName, Serializable> aspectProperties) {
        getService(NodeService.class).addAspect(nodeRef, aspectTypeQName, aspectProperties);
    }

    @Override
    public List<ChildAssociationRef> getChildAssocs(NodeRef nodeRef, Set<QName> childNodeTypeQNames) {
        return getService(NodeService.class).getChildAssocs(nodeRef, childNodeTypeQNames);
    }

    @Override
    public QName getType(NodeRef nodeRef) {
        return getService(NodeService.class).getType(nodeRef);
    }

    @Override
    public List<ChildAssociationRef> getChildAssocs(NodeRef nodeRef) {
        return getService(NodeService.class).getChildAssocs(nodeRef);
    }

    @Override
    public Map<String, Object> buildDefaultModel(NodeRef person, NodeRef companyHome, NodeRef userHome, NodeRef script, NodeRef document, NodeRef space) {
        return getService(ScriptService.class).buildDefaultModel(person, companyHome, userHome, script, document, space);
    }

    @Override
    public void executeScript(NodeRef scriptRef, QName contentProp, Map<String, Object> model) {
        getService(ScriptService.class).executeScript(scriptRef, contentProp, model);
    }

    @Override
    public NodeRef getPerson(String userName) {
        return getService(PersonService.class).getPerson(userName);
    }

    @Override
    public Map<QName, Serializable> getProperties(NodeRef nodeRef) {
        return getService(NodeService.class).getProperties(nodeRef);
    }

    @Override
    public void deleteNode(String fullPath) {
        deleteNode(getNodeRef(fullPath));
    }

    @Override
    public List<FileInfo> list(String fullPath) {
        return list(getNodeRef(fullPath));
    }

    @Override
    public Serializable getProperty(String fullPath, QName propName) {
        return getProperty(getNodeRef(fullPath), propName);
    }

    @Override
    public void move(NodeRef sourceNodeRef, NodeRef targetParentRef, String newName) throws FileExistsException, FileNotFoundException {
        getService(FileFolderService.class).move(sourceNodeRef, targetParentRef, newName);

    }

    @Override
    public void setProperty(String fullPath, QName propName, Serializable value) {
        setProperty(getNodeRef(fullPath), propName, value);
    }

    @Override
    public void removeAspect(NodeRef nodeRef, QName aspectTypeQName) {
        getService(NodeService.class).removeAspect(nodeRef, aspectTypeQName);
    }

    @Override
    public void removeAspect(String fullPath, QName aspectTypeQName) {
        removeAspect(getNodeRef(fullPath), aspectTypeQName);
    }

    @Override
    public void removeProperty(NodeRef nodeRef, QName propName) {
        getService(NodeService.class).removeProperty(nodeRef, propName);
    }

    @Override
    public void setProperties(NodeRef nodeRef, Map<QName, Serializable> properties) {
        getService(NodeService.class).setProperties(nodeRef, properties);
    }


    @Override
    public RetryingTransactionHelper getRetryingTransactionHelper() {
        return getService(TransactionService.class).getRetryingTransactionHelper();
    }

    @Override
    public boolean isReadOnly() {
        return getService(TransactionService.class).isReadOnly();
    }

    @Override
    public void unlock(String fullPath) {
        unlock(getNodeRef(fullPath));
    }


    @Override
    public void cancelWorkflow(String workflowId) {
        getService(WorkflowService.class).cancelWorkflow(workflowId);

    }

    @Override
    public WorkflowTask getTaskById(String taskId) {
        return getService(WorkflowService.class).getTaskById(taskId);
    }

    @Override
    public void endTask(String taskId, String transitionId) {
        getService(WorkflowService.class).endTask(taskId, transitionId);
    }

    @Override
    public void updateTask(String taskId, Map<QName, Serializable> properties, Map<QName, List<NodeRef>> add, Map<QName, List<NodeRef>> remove) {
        getService(WorkflowService.class).updateTask(taskId, properties, add, remove);
    }


    @Override
    public void copy(NodeRef sourceNodeRef, NodeRef destinationNodeRef) {
        getService(CopyService.class).copy(sourceNodeRef, destinationNodeRef);
    }

    @Override
    public List<ChildAssociationRef> getChildAssocsByPropertyValue(NodeRef nodeRef, QName propertyQName, Status value) {
        return getService(NodeService.class).getChildAssocsByPropertyValue(nodeRef, propertyQName, value);
    }

    @Override
    public List<WorkflowTask> queryTasks(WorkflowTaskQuery query) {
        return getService(WorkflowService.class).queryTasks(query);
    }

    @Override
    public ChildAssociationRef createNode(NodeRef parentRef, QName assocTypeQName, QName assocQName, QName nodeTypeQName) {
        return getService(NodeService.class).createNode(parentRef, assocTypeQName, assocQName, nodeTypeQName);
    }

    @Override
    public ContentWriter getWriter(NodeRef nodeRef) {
        return getService(FileFolderService.class).getWriter(nodeRef);
    }

    @Override
    public boolean createMissingPeople() {
        return getService(PersonService.class).createMissingPeople();
    }

    @Override
    public void setCreateMissingPeople(boolean createMissing) {
        getService(PersonService.class).setCreateMissingPeople(createMissing);
    }

    @Override
    public NodeRef createPerson(Map<QName, Serializable> properties) {
        return getService(PersonService.class).createPerson(properties);
    }

    @Override
    public void createAuthentication(String userName, char[] password) {
        getService(MutableAuthenticationService.class).createAuthentication(userName, password);
    }

    @Override
    public String getName(AuthorityType type, String shortName) {
        return getService(AuthorityService.class).getName(type, shortName);
    }

    @Override
    public void setPermission(NodeRef nodeRef, String authority, String permission, boolean allow) {
        getService(PermissionService.class).setPermission(nodeRef, authority, permission, allow);
    }

    @Override
    public void removeAllRules(NodeRef nodeRef) {
        getService(RuleService.class).removeAllRules(nodeRef);
    }

    @Override
    public void saveRule(NodeRef nodeRef, Rule rule) {
        getService(RuleService.class).saveRule(nodeRef, rule);
    }

    @Override
    public CompositeAction createCompositeAction() {
        return getService(ActionService.class).createCompositeAction();
    }

    @Override
    public Action createAction(String actionName) {
        return getService(ActionService.class).createAction(actionName);
    }

    @Override
    public ActionCondition createActionCondition(String name) {
        return getService(ActionService.class).createActionCondition(name);
    }

    @Override
    public String guessMimetype(String fileName) {
        return getService(MimetypeService.class).guessMimetype(fileName);
    }

    @Override
    public PropertyDefinition getProperty(QName propertyName) {
        return getService(DictionaryService.class).getProperty(propertyName);
    }

    @Override
    public boolean authorityExists(String name) {
        return getService(AuthorityService.class).authorityExists(name);
    }

    @Override
    public void createAuthority(AuthorityType type, String shortName, String authorityDisplayName, Set<String> authorityZones) {
        getService(AuthorityService.class).createAuthority(type, shortName, authorityDisplayName, authorityZones);
    }

    @Override
    public boolean personExists(String userName) {
        return getService(PersonService.class).personExists(userName);
    }

    @Override
    public void authenticate(String userName, char[] password) {
        getService(AuthenticationService.class).authenticate(userName, password);
    }

    @Override
    public CompositeActionCondition createCompositeActionCondition() {
        return getService(ActionService.class).createCompositeActionCondition();
    }

    @Override
    public List<Rule> getRules(NodeRef nodeRef) {
        return getService(RuleService.class).getRules(nodeRef);
    }

    @Override
    public void removeChild(NodeRef parentRef, NodeRef childRef) {
        getService(NodeService.class).removeChild(parentRef, childRef);
    }

    @Override
    public void moveNode(NodeRef nodeToMoveRef, NodeRef newParentRef, QName assocTypeQName, QName assocQName) {
        getService(NodeService.class).moveNode(nodeToMoveRef, newParentRef, assocTypeQName, assocQName);
    }

    @Override
    public NodeRef createNewFolder(String fullPath) {
        return createNewFolder(fullPath, new FastMap<QName, Serializable>());
    }

    @Override
    public NodeRef createNewFolder(String fullPath, Map<QName, Serializable> nodeProperties) {
        int idx = StringUtils.lastIndexOf(fullPath, '/');
        String parentPath = StringUtils.substring(fullPath, 0, idx - 1);
        String folderName = StringUtils.substring(fullPath, idx + 1);
        if (!nodeProperties.containsKey(ContentModel.PROP_NAME)) {
            nodeProperties.put(ContentModel.PROP_NAME, folderName);
        }
        return createNewFolder(parentPath, folderName, nodeProperties);
    }

    @Override
    public NodeRef createNewFolder(String parentPath, String folderName) {
        Map<QName, Serializable> nodeProperties = new FastMap<QName, Serializable>();
        nodeProperties.put(ContentModel.PROP_NAME, folderName);
        return createNewFolder(parentPath, folderName, nodeProperties);
    }

    @Override
    public NodeRef createNewFolder(String parentPath, String folderName, Map<QName, Serializable> nodeProperties) {
        NodeRef parentNodeRef = getNodeRef(parentPath);
        if (!nodeProperties.containsKey(ContentModel.PROP_NAME)) {
            nodeProperties.put(ContentModel.PROP_NAME, folderName);
        }
        return createNewFolder(parentNodeRef, folderName, nodeProperties);
    }

    @Override
    public NodeRef createNewFolder(NodeRef parentNodeRef, String folderName) {
        Map<QName, Serializable> nodeProperties = new FastMap<QName, Serializable>();
        nodeProperties.put(ContentModel.PROP_NAME, folderName);
        return createNewFolder(parentNodeRef, folderName, nodeProperties);
    }

    @Override
    public NodeRef createNewFolder(NodeRef parentNodeRef, String folderName, Map<QName, Serializable> nodeProperties) {
        QName assocQName = QName.createQName(ContentModel.TYPE_CONTENT.getNamespaceURI(), QName.createValidLocalName(folderName));
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        //updateLastNavigationOrder(parentNodeRef);
        if (!nodeProperties.containsKey(ContentModel.PROP_NAME)) {
            nodeProperties.put(ContentModel.PROP_NAME, folderName);
        }
        NodeRef result = persistenceManagerService.createNode(parentNodeRef, ContentModel.ASSOC_CONTAINS, assocQName, ContentModel.TYPE_FOLDER, nodeProperties).getChildRef();
        return result;
    }

    @Override
    public NodeRef createNewFile(String fullPath, InputStream content) {
        return createNewFile(fullPath, content, new FastMap<QName, Serializable>());
    }

    @Override
    public NodeRef createNewFile(String fullPath, InputStream content, Map<QName, Serializable> nodeProperties) {
        int idx = StringUtils.lastIndexOf(fullPath, '/');
        String parentPath = StringUtils.substring(fullPath, 0, idx - 1);
        String fileName = StringUtils.substring(fullPath, idx + 1);
        if (!nodeProperties.containsKey(ContentModel.PROP_NAME)) {
            nodeProperties.put(ContentModel.PROP_NAME, fileName);
        }
        return createNewFile(parentPath, fileName, content, nodeProperties);
    }

    @Override
    public NodeRef createNewFile(String parentPath, String fileName, InputStream content) {
        NodeRef parentNodeRef = getNodeRef(parentPath);
        Map<QName, Serializable> nodeProperties = new FastMap<QName, Serializable>();
        nodeProperties.put(ContentModel.PROP_NAME, fileName);
        return createNewFile(parentNodeRef, fileName, content, nodeProperties);
    }

    @Override
    public NodeRef createNewFile(String parentPath, String fileName, InputStream content, Map<QName, Serializable> nodeProperties) {
        NodeRef parentNodeRef = getNodeRef(parentPath);
        if (!nodeProperties.containsKey(ContentModel.PROP_NAME)) {
            nodeProperties.put(ContentModel.PROP_NAME, fileName);
        }
        return createNewFile(parentNodeRef, fileName, content, nodeProperties);
    }

    @Override
    public NodeRef createNewFile(NodeRef parentNodeRef, String fileName, InputStream content) {
        Map<QName, Serializable> nodeProperties = new FastMap<QName, Serializable>();
        nodeProperties.put(ContentModel.PROP_NAME, fileName);
        return createNewFile(parentNodeRef, fileName, content, nodeProperties);
    }

    @Override
    public NodeRef createNewFile(NodeRef parentNodeRef, String fileName, InputStream content, Map<QName, Serializable> nodeProperties) {
        QName assocQName = QName.createQName(ContentModel.TYPE_CONTENT.getNamespaceURI(), QName.createValidLocalName(fileName));
        if (!nodeProperties.containsKey(ContentModel.PROP_NAME)) {
            nodeProperties.put(ContentModel.PROP_NAME, fileName);
        }
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        NodeRef newFileNodeRef = persistenceManagerService.createNode(parentNodeRef, ContentModel.ASSOC_CONTAINS, assocQName, ContentModel.TYPE_CONTENT, nodeProperties).getChildRef();
        ContentWriter writer = persistenceManagerService.getWriter(newFileNodeRef, ContentModel.PROP_CONTENT, true);
        MimetypeService mimetypeService = getService(MimetypeService.class);
        String mimeType = mimetypeService.guessMimetype(fileName);
        writer.setMimetype(mimeType);
        writer.putContent(content);
        IOUtils.closeQuietly(content);
        return newFileNodeRef;
    }

    @Override
    public ContentReader getReader(String fullPath) {
        return getReader(getNodeRef(fullPath));
    }

    @Override
    public Version createVersion(NodeRef nodeRef, Map<String, Serializable> versionProperties) {
        return getService(VersionService.class).createVersion(nodeRef, versionProperties);
    }

    @Override
    public void revert(NodeRef nodeRef, Version versionTo) {
        getService(VersionService.class).revert(nodeRef, versionTo);
    }

    @Override
    public ContentReader getReader(NodeRef nodeRef, QName propertyQName) {
        return getService(ContentService.class).getReader(nodeRef, propertyQName);
    }

    @Override
    public TypeDefinition getType(QName qName) {
        return getService(DictionaryService.class).getType(qName);
    }

    @Override
    public Serializable createPackage(NodeRef container) {
        return getService(WorkflowService.class).createPackage(container);
    }

    @Override
    public Object executeScriptString(String script, Map<String, Object> model) {
        return getService(ScriptService.class).executeScriptString(script, model);
    }

    @Override
    public UserTransaction getNonPropagatingUserTransaction() {
        return getService(TransactionService.class).getNonPropagatingUserTransaction();
    }

    @Override
    public void insertNewObjectEntry(String fullPath) {
        ObjectStateService objectStateService = getService(ObjectStateService.class);
        objectStateService.insertNewObjectEntry(fullPath);
    }

    @Override
    public void insertNewObjectEntry(NodeRef nodeRef) {
        ObjectStateService objectStateService = getService(ObjectStateService.class);
        objectStateService.insertNewObjectEntry(nodeRef);
    }

    @Override
    public void deleteSite(String site) {
        _cStudioActivityFeedDaoService.deleteActivitiesForSite(site);
        _dependencyDaoService.deleteDependenciesForSite(site);
        _objectStateDAOService.deleteObjectStatesForSite(site);
        _pageNavigationOrderSequenceDaoService.deleteSequencesForSite(site);
        _cache.removeScope(generateCacheScope(site));
    }

    @Override
    public void deleteObjectState(String objectId) {
        ObjectStateService objectStateService = getService(ObjectStateService.class);
        objectStateService.deleteObjectState(objectId);
    }

    @Override
    public void deleteObjectStateForPath(String site, String path) {
        ObjectStateService objectStateService = getService(ObjectStateService.class);
        objectStateService.deleteObjectStateForPath(site, path);
    }

    @Override
    public void deleteObjectStateForPaths(String site, List<String> paths) {
        if (paths == null && paths.isEmpty()) {
            return;
        }
        ObjectStateService objectStateService = getService(ObjectStateService.class);
        objectStateService.deleteObjectStateForPaths(site, paths);
    }

    protected String generateKey(Object... params) {
        String key = "";
        // generate the key by concatenating parameters
        if (params != null) {
            int max = params.length;
            for (int index = 0; index < max; index++) {
                key += (params[index] == null) ? "" : params[index].toString();
                if ((index + 1 == max)) {
                    key += ",";
                }
            }
        }
        return key;
    }

    protected String generateCacheScope(String site) {
        if (StringUtils.isEmpty(DmConstants.CACHE_CSTUDIO_SITE_SCOPE)) {
            return site;
        }
        if (StringUtils.isEmpty(site)) {
            return DmConstants.CACHE_CSTUDIO_SITE_SCOPE.replace("{site}", "");
        }
        return DmConstants.CACHE_CSTUDIO_SITE_SCOPE.replace("{site}", site);
    }

    @Override
    public List<NodeRef> getSubmittedItems(String site) {
        ObjectStateService objectStateService = getService(ObjectStateService.class);
        return objectStateService.getSubmittedItems(site);
    }


    @Override
    public void setManagerPermissions(NodeRef nodeRef, String authority) {
        setPermission(nodeRef, authority, PermissionService.COORDINATOR, true);
    }

    @Override
    public void rename(NodeRef nodeRef, String newName) throws FileNotFoundException {
        getService(FileFolderService.class).rename(nodeRef, newName);
    }

    @Override
    public void updateObjectPath(String fullPath, String newPath) {
        getService(ObjectStateService.class).updateObjectPath(fullPath, newPath);
    }

    @Override
    public void updateObjectPath(NodeRef nodeRef, String newPath) {
        getService(ObjectStateService.class).updateObjectPath(nodeRef, newPath);
    }

    @Override
    public boolean isUpdatedOrNew(String fullPath) {
        NodeRef nodeRef = getNodeRef(fullPath);
        if (nodeRef == null) {
            return false;
        }
        return isUpdatedOrNew(nodeRef);
    }

    @Override
    public boolean isUpdatedOrNew(NodeRef nodeRef) {
        if (nodeRef == null) {
            return false;
        }
        ObjectStateService objectStateService = getService(ObjectStateService.class);
        FileInfo fileInfo = getFileInfo(nodeRef);
        if (fileInfo.isFolder()) {
            return false;
        }
        if (objectStateService.isUpdatedOrNew(nodeRef)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isNew(String fullPath) {
        NodeRef nodeRef = getNodeRef(fullPath);
        if (nodeRef == null) {
            return false;
        }
        return isNew(nodeRef);
    }

    @Override
    public boolean isNew(NodeRef nodeRef) {
        if (nodeRef == null) {
            return false;
        }
        ObjectStateService objectStateService = getService(ObjectStateService.class);
        FileInfo fileInfo = getFileInfo(nodeRef);
        if (fileInfo.isFolder()) {
            return false;
        }
        if (objectStateService.isNew(nodeRef)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isScheduled(String fullPath) {
        NodeRef nodeRef = getNodeRef(fullPath);
        if (nodeRef == null) {
            return false;
        }
        return isScheduled(nodeRef);
    }

    @Override
    public boolean isScheduled(NodeRef nodeRef) {
        if (nodeRef == null) {
            return false;
        }
        ObjectStateService objectStateService = getService(ObjectStateService.class);
        FileInfo fileInfo = getFileInfo(nodeRef);
        if (fileInfo.isFolder()) {
            return false;
        }
        if (objectStateService.isScheduled(nodeRef)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isInWorkflow(String fullPath) {
        NodeRef nodeRef = getNodeRef(fullPath);
        if (nodeRef == null) {
            return false;
        }
        return isInWorkflow(nodeRef);
    }

    @Override
    public boolean isInWorkflow(NodeRef nodeRef) {
        if (nodeRef == null) {
            return false;
        }
        ObjectStateService objectStateService = getService(ObjectStateService.class);
        FileInfo fileInfo = getFileInfo(nodeRef);
        if (fileInfo.isFolder()) {
            return false;
        }
        if (objectStateService.isInWorkflow(nodeRef)) {
            return true;
        }
        return false;
    }

    @Override
    public void initializeCacheScope(String site) {
        initializeCacheScope(site, 5000);
    }

    @Override
    public void initializeCacheScope(String site, int maxItems) {
        String cacheScope = generateCacheScope(site);
        if (!_cache.hasScope(cacheScope)) {
            _cache.addScope(cacheScope, maxItems);
        }
    }

    @Override
    public List<NodeRef> getChangeSet(String site) {
        ObjectStateService objectStateService = getService(ObjectStateService.class);
        return objectStateService.getChangeSet(site);
    }

    @Override
    public void disableBehaviour(QName className) {
        this.policyBehaviourFilter.disableBehaviour(className);
    }

    @Override
    public void disableBehaviour(NodeRef nodeRef, QName className) {
        this.policyBehaviourFilter.disableBehaviour(nodeRef, className);
    }

    @Override
    public void enableBehaviour(QName className) {
        this.policyBehaviourFilter.enableBehaviour(className);
    }

    @Override
    public void enableBehaviour(NodeRef nodeRef, QName className) {
        this.policyBehaviourFilter.enableBehaviour(nodeRef, className);
    }

    @Override
    public void transitionBulk(List<String> objectIds, ObjectStateService.TransitionEvent event, ObjectStateService.State defaultTargetState) {
        if (objectIds == null || objectIds.isEmpty()) {
            return;
        }
        ObjectStateService objectStateService = getService(ObjectStateService.class);
        objectStateService.transitionBulk(objectIds, event, defaultTargetState);
    }

    @Override
    public void createLiveRepository(String site) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getAdministratorUserName() {
        return AuthenticationUtil.getAdminUserName();
    }

    @Override
    public NodeRef.Status getNodeStatus(final NodeRef nodeRef) {
        NodeService nodeService = getService(NodeService.class);
        return nodeService.getNodeStatus(nodeRef);
    }
}

