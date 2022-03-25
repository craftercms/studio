/*
 * Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.craftercms.studio.impl.v1.content.pipeline;


import org.craftercms.studio.api.v1.content.pipeline.ContentProcessor;
import org.craftercms.studio.api.v1.content.pipeline.ContentProcessorPipeline;
import org.craftercms.studio.api.v1.content.pipeline.PipelineContent;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.to.ResultTO;

import java.util.List;

/**
 * Implementation of ContentProcessorPipeline that runs the content give through the pipeline
 *
 * @author hyanghee
 *
 */
public class ContentProcessorPipelineImpl implements ContentProcessorPipeline {

	private static final Logger logger = LoggerFactory.getLogger(ContentProcessorPipelineImpl.class);

	/**
	 * a chain of processors to run content through
	 */
	protected List<ContentProcessor> _chain = null;

	public void processContent(PipelineContent content, ResultTO result)
			throws ServiceLayerException, UserNotFoundException {
		if (_chain != null && _chain.size() > 0) {
			for (ContentProcessor processor : _chain) {
				logger.debug("Running " + content.getId() + " through " + processor.getName());
				if (processor.isProcessable(content)) {
					processor.process(content, result);
				} else {
					logger.debug(content.getId() + " was not processed by " + processor.getName());
				}
			}
		} else {
			logger.warn("Processor chain is empty.");
		}
	}

	/**
	 * @param chain the chain to set
	 */
	public void setChain(List<ContentProcessor> chain) {
		this._chain = chain;
	}

}
