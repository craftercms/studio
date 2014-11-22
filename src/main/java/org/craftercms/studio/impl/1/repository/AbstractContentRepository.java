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
package org.craftercms.cstudio.impl.repository;

import java.io.*;
import org.dom4j.io.SAXReader;
import java.lang.reflect.Method;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import javax.transaction.UserTransaction;

import org.apache.commons.io.IOUtils;

import org.craftercms.cstudio.api.service.transaction.*;
import org.craftercms.cstudio.api.repository.*;

import org.craftercms.cstudio.api.log.*;

/**
 * Abstract repository interface provides common repository operations across implementations
 * @author russdanner
 */
public abstract class AbstractContentRepository implements ContentRepository {

	protected static final String MSG_ERROR_IO_CLOSE_FAILED = "err_io_closed_failed";
	
	private static final Logger logger = LoggerFactory.getLogger(AbstractContentRepository.class);

    /**
     * write content
     * @param site the site project id
     * @param variant variant is a variation of the site (like a translation for example)
     * @param store is an area to write to (live, stage, work-area, ...)
     * @param path is the file path to write
     * @param content is the bits to write
     */
    public void writeContent(String site, String variant, String store, String path, InputStream content) {
    	writeContent("/wem-projects/"+site+"/"+site+"/"+store + path, content);
    }

    /**
     * get content
     * @param site the site project id
     * @param variant variant is a variation of the site (like a translation for example)
     * @param store is an area to write to (live, stage, work-area, ...)
     * @param path is the file path to write
     */
    public InputStream getContent(String site, String variant, String store, String path) {
    	return getContent("/wem-projects/"+site+"/"+site+"/"+store + path);
    }

    public String getContentAsString(String path) throws Exception {
        return IOUtils.toString(this.getContent(path));
    }

    /**
     * get document from wcm content
     *
     * @param path
     * @return document
     * @throws ServiceException
     */
    public Document getContentAsDocument(String path)
    throws DocumentException {
    	Document retDocument = null;
    	InputStream is = this.getContent(path);

    	if(is != null) {
    		try {
	    		SAXReader saxReader = new SAXReader();    		
	    		retDocument = saxReader.read(is);
            } 
    		finally {
    			try {
    				if (is != null) {
    					is.close();
    				}
    			} 
    			catch (IOException err) {
    				logger.error(MSG_ERROR_IO_CLOSE_FAILED, err, path);
    			}
            }   	
    	}

    	return retDocument;
    }
}
