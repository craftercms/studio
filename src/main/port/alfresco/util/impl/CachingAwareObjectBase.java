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
package org.craftercms.cstudio.alfresco.util.impl;

import org.craftercms.cstudio.alfresco.util.api.CachingAwareObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class CachingAwareObjectBase implements CachingAwareObject {

    protected transient String scope;
    protected transient Object key;
    protected transient List<Object> dependencyKeys;
    protected transient Long cachingTime;

    protected CachingAwareObjectBase() {
    }

    protected CachingAwareObjectBase(CachingAwareObject cachingAwareObject, boolean deepCopy) {
        this.scope = cachingAwareObject.getScope();
        this.key = cachingAwareObject.getKey();
        this.cachingTime = cachingAwareObject.getCachingTime();

        if (deepCopy) {
            this.dependencyKeys = cachingAwareObject.getDependencyKeys() != null? new ArrayList<Object>(
                    cachingAwareObject.getDependencyKeys()) : null;
        } else {
            this.dependencyKeys = cachingAwareObject.getDependencyKeys();
        }
    }

    @Override
    public String getScope() {
        return scope;
    }

    @Override
    public void setScope(String scope) {
        this.scope = scope;
    }

    @Override
    public Object getKey() {
        return key;
    }

    @Override
    public void setKey(Object key) {
        this.key = key;
    }

    @Override
    public List<Object> getDependencyKeys() {
        return dependencyKeys;
    }

    @Override
    public void setDependencyKeys(List<Object> dependencyKeys) {
        this.dependencyKeys = dependencyKeys;
    }

    @Override
    public void addDependencyKeys(Collection<Object> dependencyKeys) {
        if (this.dependencyKeys == null) {
            this.dependencyKeys = new ArrayList<Object>();
        }

        this.dependencyKeys.addAll(dependencyKeys);
    }

    @Override
    public void addDependencyKey(Object dependencyKey) {
        if (dependencyKeys == null) {
            dependencyKeys = new ArrayList<Object>();
        }

        dependencyKeys.add(dependencyKey);
    }

    @Override
    public boolean removeDependencyKeys(Collection<Object> dependencyKeys) {
        if (this.dependencyKeys != null) {
            return this.dependencyKeys.removeAll(dependencyKeys);
        } else {
            return false;
        }
    }

    @Override
    public boolean removeDependencyKey(Object dependencyKey) {
        if (dependencyKeys != null) {
            return dependencyKeys.remove(dependencyKey);
        } else {
            return false;
        }
    }

    @Override
    public Long getCachingTime() {
        return cachingTime;
    }

    @Override
    public void setCachingTime(Long cachingTime) {
        this.cachingTime = cachingTime;
    }
}
