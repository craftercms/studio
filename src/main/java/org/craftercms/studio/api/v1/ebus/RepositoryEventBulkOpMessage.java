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

package org.craftercms.studio.api.v1.ebus;

import java.util.List;

/**
 * Repository Event Message for bulk operations.
 *
 * @author Dejan Brkic
 */
public class RepositoryEventBulkOpMessage {

    private String site;
    private List<String> affectedPaths;

    public String getSite() {
        return site;
    }

    public void setSite(final String site) {
        this.site = site;
    }

    public List<String> getAffectedPaths() {
        return affectedPaths;
    }

    public void setAffectedPaths(final List<String> affectedPaths) {
        this.affectedPaths = affectedPaths;
    }
}
