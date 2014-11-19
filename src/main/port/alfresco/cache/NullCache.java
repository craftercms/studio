/*******************************************************************************
 * Crafter Studio Web-content authoring solution
 *     Copyright (C) 2007-2013 Crafter Software Corporation.
 * 
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.craftercms.cstudio.alfresco.cache;

import java.io.Serializable;

/**
 */
public class NullCache implements cstudioCacheManager {

    @Override
    public void put(Scope scope, String key, Serializable item) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void put(Scope scope,String key, String site, Serializable item) {

    }

    @Override
    public Serializable get(Scope scope, String key) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Serializable get(Scope scope,String key,String site) {
        return null;
    }

    @Override
    public void invalidate(Scope scope, String key) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void invalidate(Scope scope) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void invalidate() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String generateKey(Object... params) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void invalidateAndRemoveFromQueue(String fullpath, String site) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
