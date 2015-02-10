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
package org.craftercms.studio.api.v1.service.content;


import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.exception.ServiceException;
import org.craftercms.studio.api.v1.service.workflow.context.MultiChannelPublishingContext;
import org.craftercms.studio.api.v1.to.DmDependencyTO;

import java.util.List;

/**
 * @author Dejan Brkic
 *
 * Supports renaming the URL of the page and go live on the URL changes
 *
 */
public interface DmRenameService {

    /*
    protected NodeRef getIndexNode(final String site, String uri) {
        return getNode(site, getIndexFilePath(uri));
    }


    /**
     * GoLive on the renamed node
     *//*
    @Override
    public void goLive(String site, String sub, List<DmDependencyTO> submittedItems, String approver) throws ServiceException {
                goLive(site, sub, submittedItems, approver, null);
    }

    /**
     * GoLive on the renamed node
     */
    void goLive(String site, List<DmDependencyTO> submittedItems, String approver, MultiChannelPublishingContext mcpContext) throws ServiceException;

    /**
     * Does the rename operation of moving the node and its contents from the source to destination
     *
     */
    public void rename(String site, String path, String targetPath, boolean createFolder) throws ServiceException, ContentNotFoundException;

    /**
     *
     * @param site
     * @param cutPath
     * @param pastePath
     * @return
     */
    //public boolean isRevertRename(String site, String cutPath, String pastePath);

    /**
     *
     * Given an item determines if has been renamed
     *
     */
    boolean isItemRenamed(String site, DmDependencyTO item);

    /**
     *
     * Given an item determines if has been renamed
     *
     */
    boolean isItemRenamed(String site, String uri);

    /**
     *
     * Go live operation on the renamed node
     * @param approver TODO
     *
     */
    //public void goLive(final String site, final String sub, List<DmDependencyTO> submittedItem, String approver) throws ServiceException;

    /**
     * Operation to be during pre submission
     *
     */


    //public void updateWorkflow(String site, String workFlowDescription);

    /**
     * Operation to be done post submission
     *
     */
    //public void postSubmission(String site, String workFlowDescription);
}
