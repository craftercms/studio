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

import org.alfresco.service.ServiceException;
import org.craftercms.cstudio.alfresco.dm.to.DmDependencyTO;
import org.craftercms.cstudio.alfresco.dm.workflow.MultiChannelPublishingContext;
import org.craftercms.cstudio.alfresco.service.exception.ContentNotFoundException;

import java.util.List;

/**
 * @author Dejan Brkic
 *
 * Supports renaming the URL of the page and go live on the URL changes
 *
 */
public interface DmRenameService {

    /**
     * Does the rename operation of moving the node and its contents from the source to destination
     *
     */
    public void rename(String site, String sub, String path, String targetPath,boolean createFolder) throws ServiceException, ContentNotFoundException;

    /**
     *
     * @param site
     * @param cutPath
     * @param pastePath
     * @return
     */
    public boolean isRevertRename(String site,String cutPath, String pastePath);

    /**
     *
     * Given an item determines if has been renamed
     *
     */
    public boolean isItemRenamed(String site, DmDependencyTO item);

    /**
     *
     * Given an item determines if has been renamed
     *
     */
    public boolean isItemRenamed(String site, String uri);

    /**
     *
     * Go live operation on the renamed node
     * @param approver TODO
     *
     */
    public void goLive(final String site, final String sub,List<DmDependencyTO> submittedItem, String approver) throws ServiceException;

    /**
     *
     * Go live operation on the renamed node
     * @param approver TODO
     *
     */
    public void goLive(final String site, final String sub,List<DmDependencyTO> submittedItem, String approver, MultiChannelPublishingContext mcpContext) throws ServiceException;

    /**
     * Operation to be during pre submission
     *
     */
    public void updateWorkflow(String site, String workFlowDescription);

    /**
     * Operation to be done post submission
     *
     */
    public void postSubmission(String site, String workFlowDescription);
}
