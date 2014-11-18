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
package org.craftercms.cstudio.alfresco.dm.service.api;

import java.util.List;

import org.craftercms.cstudio.alfresco.dm.to.DmPasteItemTO;
import org.craftercms.cstudio.alfresco.service.exception.ServiceException;

public interface DmClipboardService {

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
    public List<String> paste(String site, List<DmPasteItemTO> pasteItems, String destination, boolean cut) throws ServiceException;

    public String duplicateToDraft(String site, String sub, String path) throws ServiceException;

	public String duplicate(String site, String sub, String path, String source) throws ServiceException;
}
