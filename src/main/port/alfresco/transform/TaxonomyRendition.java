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
package org.craftercms.cstudio.alfresco.transform;



import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.craftercms.cstudio.alfresco.constant.CStudioContentModel;
import org.craftercms.cstudio.alfresco.constant.CStudioXmlConstants;
import org.craftercms.cstudio.alfresco.service.ServicesManager;
import org.craftercms.cstudio.alfresco.service.api.NamespaceService;
import org.craftercms.cstudio.alfresco.service.api.PersistenceManagerService;
import org.craftercms.cstudio.alfresco.service.exception.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * 
 * this is essentially a utility bean for converting Language taxonomy to 
 * the rendition needed by the database
 * 
 */
public class TaxonomyRendition {

	private static final Logger LOGGER = LoggerFactory.getLogger(TaxonomyRendition.class);

    protected ServicesManager servicesManager;
    public ServicesManager getServicesManager() {
        return servicesManager;
    }
    public void setServicesManager(ServicesManager servicesManager) {
        this.servicesManager = servicesManager;
    }

    /**
	 * Write renditionResult along with the flattened XML file to WCM
	 * 
	 * @param articleRef
	 * @param levels
	 */
	public InputStream generateParentOutputFile(NodeRef articleRef, int levels) throws ServiceException {

		InputStream retStream = null;
				
		NodeRef newChildRef = articleRef;
        PersistenceManagerService persistenceManagerService = getServicesManager().getService(PersistenceManagerService.class);
		for (int i=1;i<=levels;i++) {			
			ChildAssociationRef assoc = persistenceManagerService.getPrimaryParent(newChildRef);
			if (assoc != null) {
				NodeRef parentRef = assoc.getParentRef();
				newChildRef = parentRef;
			}			
		}		
		
		retStream = generateOutputFile(newChildRef);

		return retStream;
	}


	/**
	 * Write renditionResult along with the flattened XML file to WCM
	 * 
	 * @param articleRef
	 */
	public InputStream generateOutputFile(NodeRef articleRef) throws ServiceException {

		InputStream retStream = null;
		
		try {
			
			Document document = retreiveXml(articleRef);

			String encoding = document.getXMLEncoding();
			if (encoding == null) {
				encoding = "UTF-16";
			}

			Writer stringOutputWriter = new StringWriter();
			OutputFormat opf = 	OutputFormat.createCompactFormat();
			opf.setSuppressDeclaration(true);
			XMLWriter writer = new XMLWriter(stringOutputWriter, opf);
			writer.write(document);
			writer.close();

			retStream = new ByteArrayInputStream(stringOutputWriter.toString().getBytes(encoding));

		} catch (ServiceException e) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("Cannot create output XML Document: " + e.getMessage(), e);
			}
			throw new ServiceException("Cannot create output XML Document: " + e.getMessage(), e);

		} catch (IOException e) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("Cannot create output XML Document: " + e.getMessage(), e);
			}
			throw new ServiceException("Cannot create output XML Document: " + e.getMessage(), e);
		}

		return retStream;
	}
	
	/**
	 * Get the folder to store the rendition in
	 * 
	 * @param nodeRef
	 * @param levels
	 * @param walkup
	 * @return parent folder name
	 * @throws ServiceException
	 */
	public String getParentFolderName(NodeRef nodeRef, int levels, int walkup) throws ServiceException {
		String folderName = "";
		
		NodeRef newChildRef = nodeRef;		
		String folders[] = new String[levels];
        PersistenceManagerService persistenceManagerService = getServicesManager().getService(PersistenceManagerService.class);
		for (int i=1;i<=levels;i++) {			
			ChildAssociationRef assoc = persistenceManagerService.getPrimaryParent(newChildRef);
			if (assoc != null) {
				NodeRef parentRef = assoc.getParentRef();
				newChildRef = parentRef;
				folders[i-1] = folderName + (String) persistenceManagerService.getProperty(newChildRef, ContentModel.PROP_NAME);
			}			
		}
		
		for (int j=levels;j>=walkup;j--) {
			if (j == walkup) {
				folderName = folderName + folders[j-1];
			} else {
				folderName = folderName + folders[j-1] + "/";
			}
		}
		
		return folderName;
	}

	/**
	 * Get the folder to store the rendition in
	 * 
	 * @param nodeRef
	 * @param levels
	 * @return folder name
	 * @throws ServiceException
	 */
	public String getFolderName(NodeRef nodeRef, int levels) throws ServiceException {
		String folderName = "";
		NodeRef newChildRef = nodeRef;
		
		String folders[] = new String[levels];
        PersistenceManagerService persistenceManagerService = getServicesManager().getService(PersistenceManagerService.class);
		for (int i=1;i<=levels;i++) {			
			if (i == 1) {
				folders[levels-i]= (String) persistenceManagerService.getProperty(nodeRef, ContentModel.PROP_NAME);
			} else {
				ChildAssociationRef assoc = persistenceManagerService.getPrimaryParent(newChildRef);
				if (assoc != null) {
					NodeRef parentRef = assoc.getParentRef();
					String value = (String) persistenceManagerService.getProperty(parentRef, ContentModel.PROP_NAME);
					folders[levels-i] = value;
					newChildRef = parentRef;
				}							
			}			
		}
		
		for (int j=0;j<levels;j++) {
			if (j == levels-1) {
				folderName = folderName + folders[j];
			} else {
				folderName = folderName + folders[j] + "/";
			}
		}

		return folderName;
	}
	
	/**
	 * Construct XML 
	 *
	 * @param nodeRef
	 * @return
	 */
	protected Document retreiveXml(NodeRef nodeRef) throws ServiceException {
		// Retreive values from NodeRef
        PersistenceManagerService persistenceManagerService = getServicesManager().getService(PersistenceManagerService.class);
        NamespaceService namespaceService = getServicesManager().getService(NamespaceService.class);
		QName nodeQType = persistenceManagerService.getType(nodeRef);
		String type_value = namespaceService.getPrefixedTypeName(nodeQType);
		
		String name_value = (String) persistenceManagerService.getProperty(nodeRef, ContentModel.PROP_NAME);
		Long id_value = (Long) persistenceManagerService.getProperty(nodeRef, CStudioContentModel.PROP_IDENTIFIABLE_ID);
		String description_value = (String) persistenceManagerService.getProperty(nodeRef, CStudioContentModel.PROP_IDENTIFIABLE_DESCRIPTION);
		Long order_value = (Long) persistenceManagerService.getProperty(nodeRef, CStudioContentModel.PROP_IDENTIFIABLE_ORDER);
		Boolean disabled_value = (Boolean) persistenceManagerService.getProperty(nodeRef, CStudioContentModel.PROP_IDENTIFIABLE_DELETED);
		Boolean is_live_value = (Boolean) persistenceManagerService.getProperty(nodeRef, CStudioContentModel.PROP_IDENTIFIABLE_CURRENT);
		Object iconPath_value = persistenceManagerService.getProperty(nodeRef, CStudioContentModel.PROP_IDENTIFIABLE_ICON_PATH);
		
		// Create Document
		Document document = DocumentHelper.createDocument();
		Element category = document.addElement(CStudioXmlConstants.DOCUMENT_CATEGORY);
		
		Element type = category.addElement(CStudioXmlConstants.DOCUMENT_CATEGORY_TYPE);
		type.setText(type_value!=null? type_value.toString(): "");
		
		Element name = category.addElement(CStudioXmlConstants.DOCUMENT_CATEGORY_NAME);
		name.setText(name_value!=null? name_value.toString(): "");
		
		Element id = category.addElement(CStudioXmlConstants.DOCUMENT_CATEGORY_ID);
		id.setText(id_value!=null? id_value.toString(): "");		
		
		Element description = category.addElement(CStudioXmlConstants.DOCUMENT_CATEGORY_DESCRIPTION);
		description.setText(description_value!=null? description_value.toString(): "");
		
		Element order = category.addElement(CStudioXmlConstants.DOCUMENT_CATEGORY_ORDER);
		order.setText(order_value!=null? order_value.toString(): "");
		
		if (iconPath_value != null) {
			Element iconPath = category.addElement(CStudioXmlConstants.DOCUMENT_CATEGORY_ICON_PATH);
			iconPath.setText((String)iconPath_value);
		}
		
		Element is_live = category.addElement(CStudioXmlConstants.DOCUMENT_CATEGORY_IS_LIVE);
		if (is_live_value!=null && is_live_value.equals(true)) {
			is_live.setText("true");
		} else {
			is_live.setText("false");
		}	
		
		Element disabled = category.addElement(CStudioXmlConstants.DOCUMENT_CATEGORY_DISABLED);
		if (disabled_value!=null && disabled_value.equals(true)) {
			disabled.setText("true");
		} else {
			disabled.setText("false");
		}		
		
		Element createdbyElement = category.addElement(CStudioXmlConstants.DOCUMENT_ELM_CREATED_BY);
		createdbyElement.setText(getPropertyValue(nodeRef, ContentModel.PROP_CREATOR)); 
		
		Element modifiedbyElement = category.addElement(CStudioXmlConstants.DOCUMENT_ELM_MODIFIED_BY);
		modifiedbyElement.setText(getPropertyValue(nodeRef, ContentModel.PROP_MODIFIER));			
		
		Element parent = category.addElement(CStudioXmlConstants.DOCUMENT_CATEGORY_PARENT);
		Long parent_id_value = null;
		return document;
	}	
	
	/**
	 * get specific value of nodeRef saved in the given property
	 * 
	 * @param nodeRef
	 * @param propertyValue
	 * @return property value
	 */
	protected String getPropertyValue(NodeRef nodeRef, QName propertyValue) {
        PersistenceManagerService persistenceManagerService = getServicesManager().getService(PersistenceManagerService.class);
		return (String) persistenceManagerService.getProperty(nodeRef, propertyValue);
	}
	
}
