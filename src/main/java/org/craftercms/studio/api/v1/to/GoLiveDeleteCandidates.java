/*
 * Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
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
package org.craftercms.studio.api.v1.to;


import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v2.dal.Item;
import org.craftercms.studio.api.v2.dal.ItemState;
import org.craftercms.studio.api.v2.service.item.internal.ItemServiceInternal;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 *
 * Contains all items that are deleted. Tracks live dependencies in a seperate collection since they have to be generally be deleted for staging sandbox
 *
 * @author Shankar Krishnan
 * @author Dejan Brkic
 *
 */
public class GoLiveDeleteCandidates implements Serializable {

    private static final long serialVersionUID = -8844670158916016139L;
    // hold the page paths
    protected Set<String> paths = new HashSet<String>();

    //holds all the dependencies of the page and the child pages
    protected Set<String> dependencies = new HashSet<String>();

    //holds just the live dependenicies of page and the child pages
    protected Set<String> liveDependencyItems = new HashSet<String>(); //live items that hass been removed

    protected ContentService contentService;

    protected String site;

    protected ItemServiceInternal itemServiceInternal;

    public GoLiveDeleteCandidates(String site, ContentService contentService, ItemServiceInternal itemServiceInternal){
        this.contentService = contentService;
        this.itemServiceInternal = itemServiceInternal;
        this.site = site;
    }

    /**
     * Returns all the page paths and dependencies
     *
     * @return return all paths and dependencies
     */
    public Set<String> getAllItems(){
        Set<String> all = new HashSet<String>();
        if(!paths.isEmpty()){
            all.addAll(paths);
        }
        if(!dependencies.isEmpty()){
            all.addAll(dependencies);
        }
        return all;
    }

    /**
     * Update the dependency collection with the given dependency uri
     *
     * @param uri path
     * @return true if dependency added
     */
    public boolean addDependency(String uri){
        if (contentService.contentExists(site, uri)){
            Item item = itemServiceInternal.getItem(site, uri);
            if(!ItemState.isNew(item.getState())){
                liveDependencyItems.add(uri);
            }
            dependencies.add(uri);
            return true;
        }
        return false;
    }

    /**
     * Add the folder path and remove all the child from the live dependency collection
     *
     * @param uri path
     */
    public void addDependencyParentFolder(String uri){
        if (contentService.contentExists(site, uri)){
            Item item = itemServiceInternal.getItem(site, uri);
            if(!ItemState.isNew(item.getState())){
                for (Iterator iterator = liveDependencyItems.iterator(); iterator.hasNext();) {
                    String liveItem = (String) iterator.next();
                    if(liveItem.startsWith(uri)){
                        iterator.remove();
                    }
                }
                liveDependencyItems.add(uri);
            }
            dependencies.add(uri);
        }
    }

    public Set<String> getPaths() {
        return paths;
    }

    public Set<String> getLiveDependencyItems() {
        return liveDependencyItems;
    }

    public void setLiveDependencyItems(Set<String> liveItems) {
        this.liveDependencyItems = liveItems;
    }

    public Set<String> getDependencies() {
        return dependencies;
    }

    public void setDependencies(Set<String> dependencies) {
        this.dependencies = dependencies;
    }
}
