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
package org.craftercms.cstudio.alfresco.dm.to;

import org.craftercms.cstudio.alfresco.dm.service.api.DmContentService;

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
public class GoLiveDeleteCandidates {

    // hold the page paths
    protected Set<String> _paths = new HashSet<String>();

    //holds all the dependencies of the page and the child pages
    protected Set<String> _dependencies = new HashSet<String>();

    //holds just the live dependenicies of page and the child pages
    protected Set<String> _liveDependencyItems = new HashSet<String>(); //live items that hass been removed

    protected DmContentService _dmContentService;

    protected String _site;

    //protected String _sub;

    public GoLiveDeleteCandidates(String site, DmContentService dmContentService){
        this._dmContentService = dmContentService;
        this._site = site;
        //this._sub = sub;
    }

    /**
     * Returns all the page paths and dependencies
     *
     * @return
     */
    public Set<String> getAllItems(){
        Set<String> all = new HashSet<String>();
        if(!_paths.isEmpty()){
            all.addAll(_paths);
        }
        if(!_dependencies.isEmpty()){
            all.addAll(_dependencies);
        }
        return all;
    }

    /**
     * Update the dependency collection with the given dependency uri
     *
     * @param uri
     * @return
     */
    public boolean addDependency(String uri){

        return false;
    }

    /**
     * Add the folder path and remove all the child from the live dependency collection
     *
     * @param uri
     */
    public void addDependencyParentFolder(String uri){

    }

    public Set<String> getPaths() {
        return _paths;
    }

    public Set<String> getLiveDependencyItems() {
        return _liveDependencyItems;
    }

    public void setLiveDependencyItems(Set<String> liveItems) {
        this._liveDependencyItems = liveItems;
    }

    public Set<String> getDependencies() {
        return _dependencies;
    }

    public void setDependencies(Set<String> dependencies) {
        this._dependencies = dependencies;
    }
}
