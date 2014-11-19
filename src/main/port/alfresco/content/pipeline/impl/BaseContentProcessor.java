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
package org.craftercms.cstudio.alfresco.content.pipeline.impl;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.craftercms.cstudio.alfresco.content.pipeline.api.ContentProcessor;
import org.craftercms.cstudio.alfresco.content.pipeline.api.PipelineContent;
import org.craftercms.cstudio.alfresco.service.exception.ContentProcessException;
import org.craftercms.cstudio.alfresco.to.ResultTO;

/**
 * A base class of content processor 
 * 
 * @author hyanghee
 *
 */
public class BaseContentProcessor implements ContentProcessor {

	private static final Logger LOGGER = LoggerFactory.getLogger(BaseContentProcessor.class);
	
	public static final String NAME = "BaseContentProcessor";

	/** the default process name if not set **/
	protected String _name;

	/**
	 * default constructor
	 */
	public BaseContentProcessor() {
		this._name = NAME;
	}
	
	/**
	 * constructor that sets the process name
	 * 
	 * @param name
	 */
	public BaseContentProcessor(String name) {
		this._name = name;
	}
	
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this._name = name;
	}

	/*
	 * (non-Javadoc)
	 * @see org.craftercms.cstudio.alfresco.wcm.util.ContentProcessor#getName()
	 */
	public String getName() {
		return _name;
	}

	/*
	 * (non-Javadoc)
	 * @see org.craftercms.cstudio.alfresco.content.pipeline.api.ContentProcessor#isProcessable(org.craftercms.cstudio.alfresco.content.pipeline.api.PipelineContent)
	 */
	public boolean isProcessable(PipelineContent content) {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see org.craftercms.cstudio.alfresco.content.pipeline.api.ContentProcessor#process(org.craftercms.cstudio.alfresco.content.pipeline.api.PipelineContent, org.craftercms.cstudio.alfresco.to.ResultTO)
	 */
	public void process(PipelineContent content, ResultTO result) throws ContentProcessException {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Processing " + content.getId() + " through " + _name);
		}
	}

	/**
	 * check if the mandatory value provided is not empty
	 * 
	 * @param key
	 * @param value
	 * @throws ContentProcessorException
	 */
	public void checkForMandatoryParam(String key, String value) throws ContentProcessException {
		if (StringUtils.isEmpty(value)) {
			throw new ContentProcessException(key + " is a mandatory parameter."); 
		}
	}
}
