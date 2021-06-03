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

package org.craftercms.studio.impl.v2.service.workflow;

import org.craftercms.studio.api.v2.service.item.internal.ItemServiceInternal;
import org.craftercms.studio.api.v2.service.workflow.WorkflowService;
import org.craftercms.studio.model.rest.content.SandboxItem;

import java.util.List;
import java.util.stream.Collectors;

public class WorkflowServiceImpl implements WorkflowService {

    private ItemServiceInternal itemServiceInternal;

    @Override
    public int getItemStatesTotal(String siteId, String path, Long states) {
        return itemServiceInternal.getItemStatesTotal(siteId, path, states);
    }

    @Override
    public List<SandboxItem> getItemStates(String siteId, String path, Long states, int offset, int limit) {
        return itemServiceInternal.getItemStates(siteId, path, states, offset, limit)
                .stream().map(item -> SandboxItem.getInstance(item)).collect(Collectors.toList());
    }

    @Override
    public void updateItemStates(String siteId, List<String> paths, boolean clearSystemProcessing,
                                 boolean clearUserLocked, boolean live, boolean staged) {
        itemServiceInternal.updateItemStates(siteId, paths, clearSystemProcessing, clearUserLocked, live, staged);
    }

    @Override
    public void updateItemStatesByQuery(String siteId, String path, Long states, boolean clearSystemProcessing,
                                        boolean clearUserLocked, Boolean live, Boolean staged) {
        itemServiceInternal.updateItemStatesByQuery(siteId, path, states, clearSystemProcessing, clearUserLocked,
                live, staged);
    }

    public ItemServiceInternal getItemServiceInternal() {
        return itemServiceInternal;
    }

    public void setItemServiceInternal(ItemServiceInternal itemServiceInternal) {
        this.itemServiceInternal = itemServiceInternal;
    }
}
