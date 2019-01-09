/*
 * Copyright (C) 2007-2019 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.api.v1.to;

import java.io.Serializable;

public class PublishingTargetTO implements Serializable {

    private static final long serialVersionUID = 7825280003644274295L;

    public String getRepoBranchName() { return repoBranchName; }
    public void setRepoBranchName(String repoBranchName) { this.repoBranchName = repoBranchName; }

    public String getDisplayLabel() { return displayLabel; }
    public void setDisplayLabel(String displayLabel) { this.displayLabel = displayLabel; }

    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }

    protected String repoBranchName;
    protected String displayLabel;
    protected int order = Integer.MAX_VALUE;
}
