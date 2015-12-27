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
package org.craftercms.studio.impl.v1.repository;


import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import java.io.InputStream;

/**
 * Abstract repository interface provides common repository operations across implementations
 * @author russdanner
 */
public abstract class AbstractContentRepository implements ContentRepository {
	
	private static final Logger logger = LoggerFactory.getLogger(AbstractContentRepository.class);

    /**
     * return a specific version of the content
     * @param path path of the content
     * @param version version to return
     * @return input stream
     */
    public InputStream getContentVersion(String path, String version) 
	throws ContentNotFoundException {    
    	throw new java.lang.RuntimeException("operation not supported");
    }
	
}
