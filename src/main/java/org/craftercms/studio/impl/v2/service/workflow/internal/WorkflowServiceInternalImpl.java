/*
 * Copyright (C) 2007-2021 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.impl.v2.service.workflow.internal;

import org.apache.ibatis.annotations.Param;
import org.craftercms.studio.api.v2.dal.Workflow;
import org.craftercms.studio.api.v2.dal.WorkflowDAO;
import org.craftercms.studio.api.v2.dal.WorkflowItem;
import org.craftercms.studio.api.v2.service.workflow.internal.WorkflowServiceInternal;

import java.util.List;

import static org.craftercms.studio.api.v2.dal.QueryParameterNames.SITE_ID;
import static org.craftercms.studio.api.v2.dal.Workflow.STATE_OPENED;

public class WorkflowServiceInternalImpl implements WorkflowServiceInternal {

    private WorkflowDAO workflowDao;

    @Override
    public WorkflowItem getWorkflowEntry(String siteId, String path) {
        return workflowDao.getWorkflowEntryOpened(siteId, path, STATE_OPENED);
    }

    @Override
    public Workflow getWorkflowEntry(String siteId, String path, String publishingPackageId) {
        return workflowDao.getWorkflowEntry(siteId, path, publishingPackageId);
    }

    @Override
    public void insertWorkflow(Workflow workflow) {
        workflowDao.insertWorkflowEntry(workflow);
    }

    @Override
    public List<WorkflowItem> getSubmittedItems(String site) {
        return workflowDao.getSubmittedItems(site, STATE_OPENED);
    }

    @Override
    public void deleteWorkflowEntries(String siteId, List<String> paths) {
        workflowDao.deleteWorkflowEntries(siteId, paths);
    }

    @Override
    public void deleteWorkflowEntry(String siteId, String path) {
        workflowDao.deleteWorkflowEntry(siteId, path);
    }

    @Override
    public void deleteWorkflowEntriesForSite(long siteId) {
        workflowDao.deleteWorkflowEntriesForSite(siteId);
    }

    public WorkflowDAO getWorkflowDao() {
        return workflowDao;
    }

    public void setWorkflowDao(WorkflowDAO workflowDao) {
        this.workflowDao = workflowDao;
    }
}
