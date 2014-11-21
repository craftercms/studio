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
/**
 * 
 */
package org.craftercms.cstudio.alfresco.service.impl;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.alfresco.service.cmr.repository.NodeRef;
import org.dom4j.DocumentException;
import org.dom4j.io.DOMWriter;
import org.craftercms.cstudio.alfresco.service.AbstractRegistrableService;
import org.craftercms.cstudio.alfresco.service.api.CStudioNodeService;
import org.craftercms.cstudio.alfresco.service.api.FormService;
import org.craftercms.cstudio.alfresco.service.api.PersistenceManagerService;
import org.craftercms.cstudio.alfresco.service.exception.ServiceException;
import org.craftercms.cstudio.alfresco.to.FormConfigTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 * The form service is responsible for providing form defintions and properties from 
 * the repository
 */
public class FormServiceImpl extends AbstractRegistrableService implements FormService {

	private static final Logger LOGGER = LoggerFactory.getLogger(FormServiceImpl.class);
	
	protected String xformPath;
	protected String _includeTagPattern;

	
	/**
	 * the location where to find the configuration file
	 */
	protected String _configPath;
	
	/**
	 * configuration file name
	 */
	protected String _configFileName;
	
	Map<String, FormConfigTO> _siteTypes = new HashMap<String, FormConfigTO>();

    @Override
    public void register() {
        getServicesManager().registerService(FormService.class, this);
    }

    /*
      * (non-Javadoc)
      *
      * @see
      * org.craftercms.cstudio.alfresco.service.api.DmService#loadForm(java.lang.String
      * )
      */
	public Document loadForm(String formId) throws ServiceException {
		StringBuilder sbFileLocation = new StringBuilder(getXformPath());
        if (!formId.startsWith("/"))
            sbFileLocation.append("/");
        sbFileLocation.append(formId);
        String filename = "xform.xml";
        sbFileLocation.append("/").append(filename);

        /*
		String[] pathParts = formId.split("/");
		
		for(int i=0; i<pathParts.length; i++) {
			if(!"".equals(pathParts[i])) {
				fileLocation += "/cm:" + pathParts[i];
			}
		} */
		

		// Retreive the xform.xml file
		//NodeRef xformFileNodeRef = _searchService.findNodeFromPath(
		//		CStudioConstants.STORE_REF, fileLocation, filename, true);
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        NodeRef xformFileNodeRef = persistenceManagerService.getNodeRef(sbFileLocation.toString());
		
		org.dom4j.Document document = null;
		
		try { 
			if(xformFileNodeRef == null) {
				throw new ServiceException("no asset '" + filename + "' found at path: '"+ sbFileLocation.toString() +"'");
			}

			document = persistenceManagerService.loadXml(xformFileNodeRef);
		}
		catch(Exception err) {
			throw new ServiceException("error loading XML for '" + filename + "' found at path: '" + sbFileLocation.toString() + "'");
		}
		
		DOMWriter domWriter = new DOMWriter();

		Document dmDocument = null;
		try {
			dmDocument = domWriter.write(document);
		} catch (DocumentException e) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("Error in converting dom4j to w3c document: "
						+ e.getMessage(), e);
				throw new ServiceException(e);
			}
		}

		return dmDocument;
	}
	
	/**
	 * generic method for getting a given form asset as a string
	 * @param formId
	 * @param componentName
	 * @return component as string
	 */
	public String loadComponentAsString(String formId, String componentName) throws ServiceException {
		
		String retComponent = "";

		StringBuilder sbFileLocation = new StringBuilder(getXformPath());
        if (!formId.startsWith("/"))
            sbFileLocation.append("/");
        sbFileLocation.append(formId);
        sbFileLocation.append("/");
        sbFileLocation.append(componentName);

        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        NodeRef fileNodeRef = persistenceManagerService.getNodeRef(sbFileLocation.toString());

		if(fileNodeRef != null) {
			retComponent = persistenceManagerService.getContentAsString(fileNodeRef);
		}

		return retComponent;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.craftercms.cstudio.alfresco.service.api.DmService#loadFormAsString(java
	 * .lang.String)
	 */
	public String loadFormAsString(String formId) throws ServiceException {
		String formString = null;

		Document document = this.loadForm(formId);

		// Convert w3c document to string representation
		try {
			// Create the source
			DOMSource source = new DOMSource(document);

			// Create the destination
			StringWriter stringWriter = new StringWriter();
			StreamResult destination = new StreamResult(stringWriter);

			// Create the transformer
			Transformer transformer = null;
			transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");

			// Transform document to string
			transformer.transform(source, destination);
			formString = stringWriter.toString();
		} catch (TransformerException e) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("Error in converting w3c document to xml string: "
						+ e.getMessage(), e);
				throw new ServiceException(e);
			}
		}

		formString = processCsIncludes(formString);
		
		return formString;
	}

	/**
	 * look for <cs:formInclude path="..." />
	 * @param formString
	 * @return
	 */
	protected String processCsIncludes(String formString) {
		String retExpandedForm = formString;
		Pattern includeTagPattern = Pattern.compile(
				_includeTagPattern, 
				Pattern.CASE_INSENSITIVE | 
				Pattern.DOTALL | 
				Pattern.MULTILINE);
		
		Matcher tagMatcher = includeTagPattern.matcher(formString);
		
		while (tagMatcher.find()) {
			String tag = tagMatcher.group();
			String include = tagMatcher.group(1);
			
			// regex seems to be a greedy outside of the test harness, not sure why yet
			// a perfected regex would remove the need for the following two branches 
			// if they work properly this code becomes inert and can be removed.
			if(tag.indexOf(">") != -1) {
				tag = tag.substring(0, tag.indexOf(">")+1);
			}
			
			if(include.indexOf("\"") != -1) {
				include = include.substring(0, include.indexOf("\""));
			}

			String filename = include.substring(include.lastIndexOf("/")+1);
			String location = include.substring(0, include.lastIndexOf("/"));
			
			try {
			String includeContent = loadComponentAsString(location, filename);
			
			// recurse to handle nested includes
			includeContent = processCsIncludes(includeContent);
			
			retExpandedForm = retExpandedForm.replaceAll(tag, includeContent);
			}
			catch(Exception includeErr) {
				LOGGER.error("error performing xform include on path:"+include, includeErr);
			}
		}

		return retExpandedForm;
	}

	public String getXformPath() {
		return xformPath;
	}

	public void setXformPath(String xformPath) {
		this.xformPath = xformPath;
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

	/**
	 * set regex pattern to match an include tag 
	 * @param pattern
	 */
	public void setIncludeTagPattern(String pattern) {
		this._includeTagPattern = pattern;
	}
		
}
