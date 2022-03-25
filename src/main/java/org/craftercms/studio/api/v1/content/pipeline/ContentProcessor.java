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
package org.craftercms.studio.api.v1.content.pipeline;

import org.craftercms.studio.api.v1.exception.ContentProcessException;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.to.ResultTO;

/**
 * interface for processing wcm content upon creating or updating the content
 * 
 * @author hyanghee
 *
 */
public interface ContentProcessor {

	/**
	 * process the content at the given path. 
	 * 
	 * @param content
	 * 			content to process
	 * @param result 
	 * 			result to return
	 * @throws ContentProcessException 
	 */
	void process(PipelineContent content, ResultTO result) throws ServiceLayerException, UserNotFoundException;
	
	/**
	 * determines if the content is processable by the given parameters
	 * 
	 * @param content
	 */
	boolean isProcessable(PipelineContent content);

	/**
	 * get the name of this processor
	 * 
	 * @return processor name
	 */
	String getName();
	
}
