/*
 * Crafter Studio Web-content authoring solution
 * Copyright (C) 2007-2016 Crafter Software Corporation.
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

    /**
     * GoLive on the renamed node
     */
    void goLive(String site, List<DmDependencyTO> submittedItems, String approver, MultiChannelPublishingContext mcpContext) throws ServiceException;

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

}
