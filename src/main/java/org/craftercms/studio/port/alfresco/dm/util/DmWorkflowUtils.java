/*
 * Copyright (C) 2007-2013 Crafter Software Corporation.
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
package org.craftercms.cstudio.alfresco.dm.util;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.workflow.WorkflowTask;

import java.util.List;
import java.util.LinkedList;

public class DmWorkflowUtils {

    public static List<WorkflowTask> getAssociatedTasksForNode(NodeRef node, List<WorkflowTask> tasks) {
        List<WorkflowTask> result = new LinkedList<WorkflowTask>();

        for (WorkflowTask task : tasks)
        {
            final NodeRef ref = task.path.instance.workflowPackage;
            if (ref.getId().equals(node.getId())) {
                result.add(task);
            }
            /*
            final String path = WCMUtil.getCorrespondingPath(node.getPath(), ref.getStoreRef().getIdentifier());

            if (logger.isDebugEnabled())
            {
                logger.debug("checking store " + ref.getStoreRef().getIdentifier() +
                        " for " + node.getPath() + " (" + path + ")");
            }

            try
            {
                final LayeringDescriptor ld = avmService.getLayeringInfo(-1, path);
                if (!ld.isBackground())
                {
                    if (logger.isDebugEnabled())
                    {
                        logger.debug(path + " is in the foreground.  workflow active");
                    }

                    result.add(task);
                }
            }
            catch (final AVMNotFoundException avmnfe)
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug(path + " not found");
                }
            }   */
        }

        return result;
    }
}
