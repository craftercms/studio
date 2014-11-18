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
package org.craftercms.cstudio.alfresco.util.impl;

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javolution.util.FastMap;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.executer.AddFeaturesActionExecuter;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.action.CompositeAction;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.rule.Rule;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.craftercms.cstudio.alfresco.constant.CStudioConstants;
import org.craftercms.cstudio.alfresco.constant.CStudioContentModel;
import org.craftercms.cstudio.alfresco.dm.util.DmUtils;
import org.craftercms.cstudio.alfresco.service.ServicesManager;
import org.craftercms.cstudio.alfresco.service.api.NamespaceService;
import org.craftercms.cstudio.alfresco.service.api.PersistenceManagerService;
import org.craftercms.cstudio.alfresco.service.api.SearchService;
import org.craftercms.cstudio.alfresco.service.api.SequenceService;
import org.craftercms.cstudio.alfresco.service.exception.SequenceException;
import org.craftercms.cstudio.alfresco.util.ContentFormatUtils;
import org.craftercms.cstudio.alfresco.util.ContentUtils;
import org.craftercms.cstudio.alfresco.util.api.ImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImportServiceImpl extends ImportServiceBase implements ImportService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImportServiceImpl.class);

    /**
     * default timezone 
     */
    protected String _timezone;

    @Override
    public void register() {
        getServicesManager().registerService(ImportService.class, this);
    }

    /*
    * (non-Javadoc)
    * @see org.craftercms.cstudio.alfresco.util.impl.ImportServiceBase#setupRepository(org.dom4j.Document, boolean, java.lang.String)
    */
    @SuppressWarnings("unchecked")
	protected void setupRepository(Document document, boolean importFromFilePath, String buildDataLocation) {
    	if (LOGGER.isInfoEnabled())
    		LOGGER.info("[IMPORTSERVICE] starting DM repository setup.");
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        Element root = document.getRootElement();
        Node node = root.selectSingleNode("//repository/folders");
        String rootPath = node.valueOf("@root");
        String fileRootPath = node.valueOf("@file-root");
        NodeRef rootRef = persistenceManagerService.getNodeRef(rootPath);
        String basePath = buildDataLocation + "/" + fileRootPath;
        if (LOGGER.isInfoEnabled())
        	LOGGER.info("[IMPORTSERVICE] importing from " + basePath + " to " + rootPath);
        
       if (rootRef != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat(CStudioConstants.DATE_PATTERN_WORKFLOW);
            dateFormat.setTimeZone(TimeZone.getTimeZone(_timezone));
            createChildren(basePath, importFromFilePath, rootRef, node.selectNodes("folder"), true, dateFormat);
            createChildren(basePath, importFromFilePath, rootRef, node.selectNodes("file"), false, dateFormat);
        } else {
            LOGGER.error("[IMPORTSERVICE] Cannot locate the root folder: " + rootPath);
        }
        List<String> groups = createGroups(root.selectNodes("groups/group"), null);
        for (String group : groups) {
            String groupName = persistenceManagerService.getName(AuthorityType.GROUP, group);
            persistenceManagerService.setPermission(rootRef, groupName, PermissionService.COORDINATOR, true);
        }
    }

    /**
     * create child folders and files 
     * 
     * @param parentClassFilePath
     * @param importFromFilePath
     * @param parentRef
     * @param childNodes
     * @param isFolder
     * @param dateFormat
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    protected void createChildren(String parentClassFilePath, boolean importFromFilePath, NodeRef parentRef, List<Node> childNodes,
                                boolean isFolder, SimpleDateFormat dateFormat) {
    	if (LOGGER.isInfoEnabled()) {
    		LOGGER.info("[IMPORTSERVICE] createChildren parentClassFilePath[" + parentClassFilePath + "]");
    	}
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        if (parentRef != null && childNodes != null) {
            for (Node childNode : childNodes) {
                String childName = childNode.valueOf("@name");
                boolean importAll = (isFolder) ? ContentFormatUtils.getBooleanValue(childNode.valueOf("@import-all")) : false;
                if (importAll) {
                    if (LOGGER.isInfoEnabled()) {
                        LOGGER.info("[IMPORTSERVICE] Importing all folders and files from " + childName);
                    }
                    NodeRef childRef = getFileFolderRef(parentRef, childName, ContentModel.TYPE_FOLDER);
                    List<Node> aspectNodes = childNode.selectNodes("common-aspects/aspect");
                    Map<QName, Map<QName, Serializable>> commonAspects = new FastMap<QName, Map<QName, Serializable>>();
                    if (aspectNodes != null && aspectNodes.size() > 0) {
                        for (Node aspectNode : aspectNodes) {
                            QName aspectName = persistenceManagerService.createQName(aspectNode.valueOf("@name"));
                            Map<QName, Serializable> properties = getProperties(aspectNode.selectNodes("properties/property"), dateFormat);
                            commonAspects.put(aspectName, properties);
                        }
                    }
                    importFileList(childRef, parentClassFilePath + "/" + childName, importFromFilePath, commonAspects);
                    createRules(childName, childRef, childNode.selectNodes("rule"));
                } else {
                    String childType = childNode.valueOf("@type");
                    QName type = persistenceManagerService.createQName(childType);
                    if (childName != null && type != null) {
                        createContent(childNode, parentClassFilePath, importFromFilePath, parentRef, childName, type, isFolder, dateFormat);
                    }
                }
            }
        }
    }
    

    /**
     * import all files and folders at the given parent class file path to DM
     *
     * @param parentRef
     * @param parentClassFilePath
     * @param importFromFilePath
     * @param commonAspects
     * @throws Exception
     */
    protected void importFileList(NodeRef parentRef, String parentClassFilePath, boolean importFromFilePath, Map<QName, Map<QName, Serializable>> commonAspects) {
    	if (LOGGER.isInfoEnabled()) {
    		LOGGER.info("[IMPORTSERVICE] importFileList parentClassFilePath[" + parentClassFilePath + "]");
    	}
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        URL resourceUrl = getResourceUrl(parentClassFilePath, importFromFilePath);
        if (resourceUrl != null) {
            String resourcePath = resourceUrl.getFile();
            File file = new File(resourcePath);
            if (file.isDirectory()) {
                String[] children = file.list();
                if (children != null && children.length > 0) {
                    for (String childName : children) {
                        File childFile = new File(resourcePath + "/" + childName);
                        String childClassFilePath = parentClassFilePath + "/" + childName;
                        QName childType = (childFile.isDirectory()) ? ContentModel.TYPE_FOLDER : ContentModel.TYPE_CONTENT;
                        NodeRef nodeRef = getFileFolderRef(parentRef, childName, childType);
                        if (childFile.isDirectory()) {
                            importFileList(nodeRef, childClassFilePath, importFromFilePath, commonAspects);
                        } else {
                            try {
                                copyFile(childClassFilePath, importFromFilePath, nodeRef, childName);
                                for (QName aspect : commonAspects.keySet()) {
                                    persistenceManagerService.addAspect(nodeRef, aspect, commonAspects.get(aspect));
                                }
                            } catch (Exception e) {
                                LOGGER.error("[IMPORTSERVICE] Error copying file ["+childClassFilePath+"]",e);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * get a file or folder nodeRef
     *
     * @param parentRef
     * @param childName
     * @param childType
     * @return a file/folder nodeRef
     */
    protected NodeRef getFileFolderRef(NodeRef parentRef, String childName, QName childType) {
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        NodeRef nodeRef = persistenceManagerService.getChildByName(parentRef, ContentModel.ASSOC_CONTAINS, childName);
        // create the folder if it does not exist
        if (nodeRef == null) {
            try {
                NamespaceService namespaceService = getService(NamespaceService.class);
                QName assocQName = namespaceService.createContentName(childName);
                FileInfo fileInfo = persistenceManagerService.create(parentRef, childName, childType, assocQName);
                nodeRef = fileInfo.getNodeRef();
                persistenceManagerService.setProperty(nodeRef, ContentModel.PROP_NAME, childName);
            } catch (IllegalArgumentException e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("[IMPORTSERVICE] Failed to create: " + childName + ", childType: " + childType + ", parentRef: " + parentRef, e);
                }
            }
        }
        return nodeRef;
    }

    /**
     * create folder or file
     *
     * @param node
     * @param parentClassFilePath
     * @param parentRef
     * @param name
     * @param type
     * @param isFolder
     * @param dateFormat
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    protected void createContent(Node node, String parentClassFilePath, boolean importFromFilePath, NodeRef parentRef, String name, QName type,
                               boolean isFolder, SimpleDateFormat dateFormat) {
    	if (LOGGER.isInfoEnabled()) {
    		LOGGER.info("[IMPORTSERVICE] createContent ["+parentClassFilePath+"]");
    	}
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        NodeRef nodeRef = getFileFolderRef(parentRef, name, type);
        addAspects(nodeRef, node, dateFormat);
        // set properties
        Map<QName, Serializable> properties = getProperties(node.selectNodes("properties/property"), dateFormat);
        if (properties != null) {
            persistenceManagerService.setProperties(nodeRef, properties);
        }
        String childClassFilePath = parentClassFilePath + "/" + name;
        if (isFolder) {
            // if the folder is identifiable, assign an ID unless the content already carries an id
            if (persistenceManagerService.hasAspect(nodeRef, CStudioContentModel.ASPECT_IDENTIFIABLE)) {
                String namespace = (String) persistenceManagerService.getProperty(nodeRef, CStudioContentModel.PROP_IDENTIFIABLE_NAMESPACE);
                Long order = (Long) persistenceManagerService.getProperty(nodeRef, CStudioContentModel.PROP_IDENTIFIABLE_ORDER);
                // if is-current flag is not found, it is current by default
                boolean current = (Boolean) persistenceManagerService.getProperty(nodeRef, CStudioContentModel.PROP_IDENTIFIABLE_CURRENT);
                assignIdentifier(name, type, nodeRef, namespace, order, current);
            }
            createChildren(childClassFilePath, importFromFilePath, nodeRef, node.selectNodes("folder"), true, dateFormat);
            createChildren(childClassFilePath, importFromFilePath, nodeRef, node.selectNodes("file"), false, dateFormat);
            createRules(name, nodeRef, node.selectNodes("rule"));
        } else {
            boolean overwrite = ContentFormatUtils.getBooleanValue(node.valueOf("@overwrite"));
            ;
            if (overwrite) {
                try {
                    copyFile(childClassFilePath, importFromFilePath, nodeRef, name);
                } catch (Exception e) {
                    LOGGER.error("[IMPORTSERVICE] error copying file ["+childClassFilePath+"]",e);
                }
            }
        }
    }


    /**
     * assign an identifier to the given node if the node does not have one
     *
     * @param nodeRef
     * @param namespace ID space
     * @param current
     */
    protected void assignIdentifier(String name, QName type, NodeRef nodeRef, String namespace, Long order, boolean current) {
        // don't create an id if no namespace specified
        // the sequence service supports no namespace and it would generate an id from the default namespace
        // however, we're not enabling it
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        if (!StringUtils.isEmpty(namespace)) {
            Long id = (Long) persistenceManagerService.getProperty(nodeRef, CStudioContentModel.PROP_IDENTIFIABLE_ID);
            if (id == null) {
                try {
                    SequenceService sequenceService = getService(SequenceService.class);
                    id = sequenceService.next(namespace, true);
                    persistenceManagerService.setProperty(nodeRef, CStudioContentModel.PROP_IDENTIFIABLE_ID, id);
                } catch (SequenceException e) {
                    LOGGER.error("[IMPORTSERVICE]  Failed to assign an id for " + name + " (type: " + type + "). "
                            + "Unable to generate an id in namespace: " + namespace + ".", e);
                }
            }
        }
    }

    /**
     * create rules for the space associated with the nodeRef
     *
     * @param nodeRef
     * @param nodes
     */
    protected void createRules(String name, NodeRef nodeRef, List<Node> nodes) {
        if (nodes != null) {
            PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
            for (Node node : nodes) {
                // clean up
                persistenceManagerService.removeAllRules(nodeRef);
                // create an action for the rule
                Action action = createAction(name, node.selectSingleNode("action"));
                if (action != null) {
                    Rule rule = new Rule();
                    rule.setRuleType(node.valueOf("@ruleType"));
                    rule.setDescription(node.valueOf("@description"));
                    rule.setTitle(node.valueOf("@title"));
                    boolean executeAsynchronously = Boolean.parseBoolean(node.valueOf("@executeAsynchronously"));
                    rule.setExecuteAsynchronously(executeAsynchronously);
                    boolean applyToChildren = Boolean.parseBoolean(node.valueOf("@applyToChildren"));
                    rule.applyToChildren(applyToChildren);
                    rule.setAction(action);
                    persistenceManagerService.saveRule(nodeRef, rule);
                }
            }
        }
    }

    /**
     * create an add-aspect action given action configuration
     *
     * @param name
     * @param node
     * @return
     */
    protected Action createAction(String name, Node node) {
        if (node != null) {
            String actionName = node.valueOf("@name");
            if (actionName != null && actionName.equals(AddFeaturesActionExecuter.NAME)) {
                return createAddAspectAction(name, node, actionName);
            } else if (!StringUtils.isEmpty(actionName)) {
                return createImportAction(name, node, actionName);
            } else {
                LOGGER.error("[IMPORTSERVICE] action name cannot be empty");
            }
        } else {
            LOGGER.error("[IMPORTSERVICE] An error occured while creating a rule in " + name
                    + ". No action specified to create an action.");
        }
        return null;
    }

    /**
     * 1. create Success Kit Import Action
     * 2. create Download Import Action
     *
     * @param name
     * @param node
     * @param actionName
     * @return
     */
    protected Action createImportAction(String name, Node node, String actionName) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Adding " + actionName + " in " + name);
        }
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        CompositeAction compositeAction = persistenceManagerService.createCompositeAction();
        // add action
        Action action = persistenceManagerService.createAction(actionName);
        compositeAction.addAction(action);
        // add action condition
        ActionCondition mimeTypeCondition = persistenceManagerService.createActionCondition(CompareMimeTypeEvaluator.NAME);
        mimeTypeCondition.setParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE, MimetypeMap.MIMETYPE_XML);
        compositeAction.addActionCondition(mimeTypeCondition);
        // disabled for now
        // ActionCondition textPropertyCondition =
        // actionService.createActionCondition(
        // ComparePropertyValueEvaluator.NAME);
        // textPropertyCondition.setParameterValue(ComparePropertyValueEvaluator.PARAM_CONTENT_PROPERTY,
        // DataTypeDefinition.TEXT);
        // textPropertyCondition.setParameterValue(ComparePropertyValueEvaluator.PARAM_PROPERTY,
        // "cm:name");
        // textPropertyCondition.setParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE,
        // "._");
        // textPropertyCondition.setParameterValue(ComparePropertyValueEvaluator.PARAM_OPERATION,
        // ComparePropertyValueOperation.BEGINS.toString());
        // textPropertyCondition.setParameterValue("notcondition", new
        // Boolean(true));
        // compositeAction.addActionCondition(textPropertyCondition);
        return compositeAction;
    }

    /**
     * create add aspect action
     *
     * @param name
     * @param node
     * @param actionName
     * @return
     */
    protected Action createAddAspectAction(String name, Node node, String actionName) {
        String paramName = node.valueOf("@paramName");
        String paramValue = node.valueOf("@paramValue");
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        QName aspectType = persistenceManagerService.createQName(paramValue);
        if (aspectType != null) {
            CompositeAction compositeAction = persistenceManagerService.createCompositeAction();
            // add action
            Action action = persistenceManagerService.createAction(actionName);
            action.setParameterValue(paramName, aspectType);
            compositeAction.addAction(action);
            // add action condition
            String actionConditionName = node.valueOf("condition/@name");
            ActionCondition actionCondition = persistenceManagerService.createActionCondition(actionConditionName);
            compositeAction.addActionCondition(actionCondition);
            return compositeAction;
        } else {
            LOGGER.error("An error occured while creating a rule in " + name
                    + ". No aspect specified to create an action.");
        }
        return null;
    }

    /**
     * copy file to the DM repository
     *
     * @param classFilePath
     * @param nodeRef
     * @param fileName
     * @throws Exception
     */
    protected void copyFile(String classFilePath, boolean importFromFilePath, NodeRef nodeRef, String fileName) throws Exception {
        InputStream in = getResourceStream(classFilePath, importFromFilePath);
        try {
            if (in != null) {
                PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
                ContentWriter writer = persistenceManagerService.getWriter(nodeRef);
                String mimeType = persistenceManagerService.guessMimetype(fileName);
                writer.setMimetype(mimeType);
                writer.putContent(in);
            } else {
                LOGGER.error("[IMPORTSERVICE] " + fileName + " cannot be found.");
            }
        } finally {
            ContentUtils.release(in);
        }
    }


    /**
     * add aspects to content
     *
     * @param nodeRef
     * @param node
     * @param dateFormat
     */
    @SuppressWarnings("unchecked")
    protected void addAspects(NodeRef nodeRef, Node node, SimpleDateFormat dateFormat) {
        List<Node> aspectNodes = node.selectNodes("aspects/aspect");
        if (aspectNodes != null && aspectNodes.size() > 0) {
            PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
            for (Node aspectNode : aspectNodes) {
                String name = aspectNode.valueOf("@name");
                QName type = persistenceManagerService.createQName(name);
                if (type != null) {
                    Map<QName, Serializable> properties = getProperties(aspectNode.selectNodes("properties/property"), dateFormat);
                    persistenceManagerService.addAspect(nodeRef, type, properties);
                }
            }
        }
    }

    /**
     * get a map of properties from the given nodes
     *
     * @param nodes
     * @param dateFormat
     * @return
     */
    protected Map<QName, Serializable> getProperties(List<Node> nodes, SimpleDateFormat dateFormat) {
        Map<QName, Serializable> properties = null;
        if (nodes != null && nodes.size() > 0) {
            properties = new HashMap<QName, Serializable>();
            NamespaceService namespaceService = getService(NamespaceService.class);
            PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
            for (Node propNode : nodes) {
                String propName = propNode.valueOf("@name");
                String value = propNode.getText();
                QName propType = namespaceService.createQName(propName);
                if (propType != null) {
                    PropertyDefinition propDef = persistenceManagerService.getProperty(propType);
                    if (propDef != null) {
                        String className = propDef.getDataType().getJavaClassName();
                        Serializable convertedValue = ContentFormatUtils.convertType(namespaceService, propType, className, value, dateFormat);
                        properties.put(propType, convertedValue);
                    }
                }
            }
        }
        return properties;
    }
    
    /**
     * create groups specified
     *
     * @param nodes
     * @param parentName parent group name. if null, groups will be created at root
     */
    @SuppressWarnings("unchecked")
    protected List<String> createGroups(List<Node> nodes, String parentName) {
    	if (LOGGER.isDebugEnabled())
    		LOGGER.debug("[IMPORTSERVICE] createGroups parentName[" + parentName + "]");
        if (nodes != null && nodes.size() > 0) {
            List<String> groups = new ArrayList<String>(nodes.size());
            PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
            for (Node node : nodes) {
                String parentGroup = null;
                if (parentName != null) {
                    parentGroup = "GROUP_" + parentName;
                }
                String name = node.valueOf("@name");
                if (!StringUtils.isEmpty(name)) {
                    if (!persistenceManagerService.authorityExists(name)) {
                    	// passing null for authority zone since it is not applicable
                        persistenceManagerService.createAuthority(AuthorityType.GROUP, parentGroup, name, null);
                        groups.add(name);
                    }
                    createGroups(node.selectNodes("group"), name);
                }
                // add users to the group if the list is provided
                String userList = node.valueOf("users");
                if (!StringUtils.isEmpty(userList)) {
                    String[] users = userList.split(",");
                    for (String user : users) {
                        if (persistenceManagerService.personExists(user)) {
                            persistenceManagerService.addAuthority("GROUP_" + name, user);
                        } else {
                            LOGGER.error("[IMPORTSERVICE] " + user + " does not exist and cannot be added to " + name);
                        }
                    }
                }
                // only return the top level groups to add groups to the DM space
            }
            return groups;
        }
        return new ArrayList<String>(0);
    }

    /**
     * @param timezone the timezone to set
     */
    public void setTimezone(String timezone) {
        this._timezone = timezone;
    }
}
