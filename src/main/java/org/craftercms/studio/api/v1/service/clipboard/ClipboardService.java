/*
 * Crafter Studio Web-content authoring solution
 * Copyright (C) 2007-2015 Crafter Software Corporation.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.craftercms.studio.api.v1.service.clipboard;

import org.craftercms.studio.api.v1.exception.ServiceException;
import org.craftercms.studio.api.v1.to.DmPasteItemTO;

import java.util.List;
import java.util.Map;


public interface ClipboardService {

    /**
     * paste a list of items provided to the specified destination
     *
     * @param site
     * @param pasteItems
     * @param destination
     * 			the root folder of all items' destination
     * @param cut
     * @return a list of copied items
     * @throws org.craftercms.cstudio.alfresco.service.exception.ServiceException
     */
    public List<String> paste(String site, List<Map<String, String>> pasteItems, String destination, boolean cut) throws ServiceException;
/*
    public String duplicateToDraft(String site, String sub, String path) throws ServiceException;

	public String duplicate(String site, String sub, String path, String source) throws ServiceException;*/
}
