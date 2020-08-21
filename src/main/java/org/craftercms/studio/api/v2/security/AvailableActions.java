/*
 * Copyright (C) 2007-2020 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.api.v2.security;

public enum AvailableActions {
    Content: Create
    Content: Update
    Content: Delete
    Content: Cut
    Content: Copy
    Content: Paste
    Content: Move/Rename
    Content: Duplicate
    Content: Translate
    Request Publish
    Approve Publish
    Reject Publish
    Cancel Publish
    Bulk Publish
    Read Publishing Queue
    Read Publishing Status
    Start Publishing
    Stop Publishing
    Publish by Commit ID
    Set Workflow State
    Read Audit Log
    Read Site Log
    Add Remote Repository
    Remove Remote Repositroy
    Pull From Remote Repository
    Push To Remote Repository
    Resolve Conflicts
    System Create
    System Read
    System Update
    System Delete
    Read Studio Log Settings
    Update Studio Log Settings

    public final long value;

    AvailableActions(long exponent) {
        this.value = 2 ^ exponent
    }
}
