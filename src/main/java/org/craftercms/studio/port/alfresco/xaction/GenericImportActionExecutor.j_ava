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
package org.craftercms.cstudio.alfresco.action;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javolution.util.FastMap;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.NodeRef;

import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.craftercms.cstudio.alfresco.constant.CStudioConstants;
import org.craftercms.cstudio.alfresco.dm.constant.DmConstants;
import org.craftercms.cstudio.alfresco.dm.service.impl.DmContentServiceImpl;
import org.craftercms.cstudio.alfresco.service.api.PersistenceManagerService;
import org.craftercms.cstudio.alfresco.service.api.SearchService;
import org.craftercms.cstudio.alfresco.service.exception.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a generic class which will be extended further to implement load
 * xml's to Alfresco System
 * 
 */
public abstract class GenericImportActionExecutor extends ActionExecuterAbstractBase {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(GenericImportActionExecutor.class);

	/** import article date format **/
	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(
			"yyyy-MM-dd hh:mm:ss.S");

	/** path names **/
	public static final String PATH_PROCESSED = "processedPath";


	/** configuration file nodeRef **/
	protected NodeRef _configRef = null;
	
	protected PersistenceManagerService _persistenceManagerService;

	/** the last time configuration file updated **/
	protected Date _configTimeStamp = null;

	/** paths mapping **/
	protected Map<String, NodeRef> _paths = null;

	/** the location where to find the configuration file */
	protected String _configPath;

	/** configuration file name */
	protected String _configFileName;

	/** publish imported document to cstudio? **/
	protected boolean _publishToCStudio = false;


	/**
	 * CStudio SearchService This service is used to search and upload
	 * Configuration files
	 */
	protected SearchService _searchService;

	/** default namespace for all articles imported **/
	protected String _namespace = null;

	protected String _site;

	protected String _contentPath;

	protected DmContentServiceImpl _cstudioDmContentService;

	/**
	 * @return the cstudioWcmContentService
	 */
	public DmContentServiceImpl getCstudioDmContentService() {
		return _cstudioDmContentService;
	}

	/**
	 * @param cstudioDmContentService
	 *            the cstudioDmContentService to set
	 */
	public void setCstudioDmContentService(
			DmContentServiceImpl cstudioDmContentService) {
		this._cstudioDmContentService = cstudioDmContentService;
	}

	/**
	 * is configuration updated?
	 * 
	 * @return
	 */
	protected boolean isConfigUpdated() {
		if (_configRef == null) {
			return true;
		} else {
			Serializable modifiedDateVal = _persistenceManagerService.getProperty(_configRef, ContentModel.PROP_MODIFIED);
            if (modifiedDateVal == null) return false;
            Date modifiedDate = (Date)modifiedDateVal;
			return modifiedDate.after(_configTimeStamp);
		}
	}

	/**
	 * load configuration
	 */
	@SuppressWarnings("unchecked")
	protected synchronized void loadConfiguration() {
		LOGGER.debug("Start loadConfiguration");
		if (_configRef == null) {
			_configRef = _searchService.findNodeFromPath(
					CStudioConstants.STORE_REF, _configPath, _configFileName,
					true);
		}
		Document document = loadXml(_configRef);
		LOGGER.debug("Loaded Configuration document: "	+ document.asXML());

		_configTimeStamp = new Date();

		Element root = document.getRootElement();
		_site = root.valueOf("//site");
		_namespace = root.valueOf("//namespace");
		_contentPath = root.valueOf("//content-path");
		String publish = root.valueOf("//publish-to-cstudio");
        _publishToCStudio = (!StringUtils.isEmpty(publish) && publish.equalsIgnoreCase("true")) ? true : false;
            

		// get processed paths
		Map<String, NodeRef> paths = new HashMap<String, NodeRef>();
		List<Node> pathNodes = document.selectNodes("//paths/path");
		if (pathNodes != null) {
			for (Node node : pathNodes) {
				String name = node.valueOf("@name");
				String path = node.getText();
				NodeRef pathRef = _searchService.findNode(
						CStudioConstants.STORE_REF, "PATH:\"" + path + "\"");
				paths.put(name, pathRef);
			}
		}
		this._paths = paths;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.alfresco.repo.action.ParameterizedItemAbstractBase#
	 * addParameterDefinitions(java.util.List)
	 */
	@Override
	protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
	}

	
	protected String _typeOfXml = "";

	protected void startExecutor(Action action, NodeRef nodeRef,
			String idFieldNameInXML, String typeOfXmL) {
		LOGGER
				.debug("*********************************************************************************");
		LOGGER.debug("Start import process");
		_typeOfXml = typeOfXmL;
		if (isConfigUpdated() || _namespace == null) {
			loadConfiguration();
		}
		if (_persistenceManagerService.exists(nodeRef)) {
			Document document = loadXml(nodeRef);
			InputStream in = getDocumentStream(nodeRef);
			LOGGER.debug("Loaded document");
			// Publish to WCM
			if (_publishToCStudio) {
				try {
					publishToAlfrescoWCM(document, in, idFieldNameInXML, nodeRef);
				} catch (ServiceException se) {
					se.printStackTrace();
				}
			}

			moveFileToProcessedFolder(nodeRef);

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(typeOfXmL + " is successfully imported");
			}

		} else {
			LOGGER.error("No article metadata found. Failed to import " + name);
		}
	}

	protected void moveFileToProcessedFolder(NodeRef nodeRef) {
		// Move the file to the processed folder upon success
		String qname = QName.createValidLocalName(name);
		QName assocQName = QName
				.createQName(
						org.alfresco.service.namespace.NamespaceService.CONTENT_MODEL_1_0_URI,
						qname);
		// remove the file if it is already in the processed folder
		NodeRef childRef = _persistenceManagerService.getChildByName(_paths
				.get(PATH_PROCESSED), ContentModel.ASSOC_CONTAINS, name);
		if (childRef != null) {
			_persistenceManagerService.removeChild(_paths.get(PATH_PROCESSED), childRef);
		}
		_persistenceManagerService.moveNode(nodeRef, _paths.get(PATH_PROCESSED),
				ContentModel.ASSOC_CONTAINS, assocQName);
	}

	/**
	 * publish to WCM
	 * 
	 * @param document
	 * @param iStr
	 * @param idFieldName
	 * @param nodeRef
	 * @throws ServiceException
	 */
	protected void publishToAlfrescoWCM(Document document, InputStream iStr,
			String idFieldName, NodeRef nodeRef) throws ServiceException {
		Path path = _persistenceManagerService.getPath(nodeRef);
		String parentPathName = path.last().getElementString();
		
		String id = document.valueOf("//" + idFieldName);
		String fileName = document.valueOf("//file-name");
		String contentType = document.valueOf("//content-type")+"/"+parentPathName;
		writeContent(_site, _contentPath, fileName, contentType, iStr, "true");
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Published " + _typeOfXml + " :" + id + " to CStudio.");
		}
	}

	/**
	 * 
	 * @param site
	 * @param path
	 * @param fileName
	 * @param contentType
	 * @param input
	 * @param createFolders
	 * @throws ServiceException
	 */
	protected void writeContent(String site, String path, String fileName,
			String contentType, InputStream input, String createFolders)
			throws ServiceException {
		Map<String, String> params = new FastMap<String, String>();
		params.put(DmConstants.KEY_SITE, site);
		params.put(DmConstants.KEY_PATH, path);
		params.put(DmConstants.KEY_FILE_NAME, fileName);
		params.put(DmConstants.KEY_CONTENT_TYPE, contentType);
		params.put(DmConstants.KEY_CREATE_FOLDERS, createFolders);
		String id = site + ":" + path + ":" + fileName;
		// processContent will close the input stream
		_cstudioDmContentService.processContent(id, input, true, params, DmConstants.CONTENT_CHAIN_IMPORT);
	}

	/**
	 * load document given a nodeRef
	 * @param nodeRef
	 * @return Document
	 */
	protected Document loadXml(NodeRef nodeRef) {
		ContentReader contentReader = _persistenceManagerService.getReader(nodeRef);
		InputStream in = null;
		try {
			in = contentReader.getContentInputStream();
			SAXReader reader = new SAXReader();
			Document document = reader.read(in);
			return document;
		} catch (DocumentException e) {
			LOGGER.error(
					"Failed to load a file for Import: "
							+ _persistenceManagerService.getProperty(nodeRef,
									ContentModel.PROP_NAME), e);
		} finally {
			release(in);
		}
		return null;
	}

	/**
	 * close inputstream
	 * 
	 * @param in
	 */
	protected void release(InputStream in) {
		if (in != null) {
			try {
				in.close();
			} catch (IOException e) {
				LOGGER.error("Failed to close an inputstream", e);
			}
		}
	}

	/**
	 * 
	 * @param nodeRef
	 * @return
	 */
	protected InputStream getDocumentStream(NodeRef nodeRef) {
		ContentReader contentReader = _persistenceManagerService.getReader(nodeRef);

		InputStream in = null;
		try {
			in = contentReader.getContentInputStream();
			return in;
		} catch (ContentIOException e) {
			LOGGER.error(
					"Failed to load a file for Import: "
							+ _persistenceManagerService.getProperty(nodeRef,
									ContentModel.PROP_NAME), e);
		}
		return null;

	}


	
	public void setSearchService(SearchService searchService) {
		this._searchService = searchService;
	}
	
	public void setPersistenceManagerService(
			PersistenceManagerService persistenceManagerService) {
		this._persistenceManagerService = persistenceManagerService;
	}

	/**
	 * set configuration path
	 * 
	 * @param configPath
	 */
	public void setConfigPath(String configPath) {
		this._configPath = configPath;
	}

	/**
	 * set configuration file name
	 * 
	 * @param configFileName
	 */
	public void setConfigFileName(String configFileName) {
		this._configFileName = configFileName;
	}
	
	
}
