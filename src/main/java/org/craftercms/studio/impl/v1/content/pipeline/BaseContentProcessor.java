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

import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v1.content.pipeline.ContentProcessor;
import org.craftercms.studio.api.v1.content.pipeline.PipelineContent;
import org.craftercms.studio.api.v1.exception.ContentProcessException;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.to.ResultTO;

/**
 * A base class of content processor
 *
 * @author hyanghee
 *
 */
public class BaseContentProcessor implements ContentProcessor {

	private static final Logger logger = LoggerFactory.getLogger(BaseContentProcessor.class);

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

	public String getName() {
		return _name;
	}

	public boolean isProcessable(PipelineContent content) {
		return true;
	}

	public void process(PipelineContent content, ResultTO result) throws ServiceLayerException, UserNotFoundException {
		logger.debug("Processing " + content.getId() + " through " + _name);
	}

	/**
	 * check if the mandatory value provided is not empty
	 *
	 * @param key key
	 * @param value value
	 * @throws ContentProcessException mandatory value is empty
	 */
	public void checkForMandatoryParam(String key, String value) throws ContentProcessException {
		if (StringUtils.isEmpty(value)) {
			throw new ContentProcessException(key + " is a mandatory parameter.");
		}
	}
}
