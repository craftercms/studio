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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.craftercms.cstudio.alfresco.content.pipeline.api.ContentProcessor;
import org.craftercms.cstudio.alfresco.content.pipeline.api.ContentProcessorPipeline;
import org.craftercms.cstudio.alfresco.content.pipeline.api.PipelineContent;
import org.craftercms.cstudio.alfresco.service.exception.ContentProcessException;
import org.craftercms.cstudio.alfresco.to.ResultTO;

/**
 * Implementation of ContentProcessorPipeline that runs the content give through the pipeline
 * 
 * @author hyanghee
 *
 */
public class ContentProcessorPipelineImpl implements ContentProcessorPipeline {

	private static final Logger LOGGER = LoggerFactory.getLogger(ContentProcessorPipelineImpl.class);
	
	/**
	 * a chain of processors to run content through
	 */
	protected List<ContentProcessor> _chain = null;
	
	/*
	 * (non-Javadoc)
	 * @see org.craftercms.cstudio.alfresco.content.pipeline.api.ContentProcessorPipeline#processContent(org.craftercms.cstudio.alfresco.content.pipeline.api.PipelineContent, org.craftercms.cstudio.alfresco.to.ResultTO)
	 */
	public void processContent(PipelineContent content, ResultTO result) throws ContentProcessException {
		if (_chain != null && _chain.size() > 0) {
			for (ContentProcessor processor : _chain) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Running " + content.getId() + " through " + processor.getName());
				}
				if (processor.isProcessable(content)) {
					processor.process(content, result);
				} else {
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug(content.getId() + " was not processed by " + processor.getName());
					}
				}
			}
		} else {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("Processor chain is empty.");
			}
		}
	}

	/**
	 * @param chain the chain to set
	 */
	public void setChain(List<ContentProcessor> chain) {
		this._chain = chain;
	}

}
