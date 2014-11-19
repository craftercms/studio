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
package org.craftercms.cstudio.alfresco.pagenavigationordersequence;

public class PageNavigationOrderSequenceDAO {

    private String folderId;
    public String getFolder_id() {
        return folderId;
    }
    public void setFolder_id(String folderId) {
        this.folderId = folderId;
    }

    private String site;
    public String getSite() {
        return site;
    }
    public void setSite(String site) {
        this.site = site;
    }

    private String path;
    public String getPath() {
        return path;
    }
    public void setPath(String path) {
        this.path = path;
    }

    private float maxCount;
    public float getMax_count() {
        return maxCount;
    }
    public void setMax_count(float maxCount) {
        this.maxCount = maxCount;
    }

    public PageNavigationOrderSequenceDAO() { }

    public PageNavigationOrderSequenceDAO(String folderId, String site, String path, float maxCount) {
        this.folderId = folderId;
        this.site = site;
        this.path = path;
        this.maxCount = maxCount;
    }
}
