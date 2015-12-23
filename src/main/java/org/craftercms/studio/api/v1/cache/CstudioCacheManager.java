/*
 * Crafter Studio Web-content authoring solution
 * Copyright (C) 2007-2014 Crafter Software Corporation.
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
package org.craftercms.studio.api.v1.cache;

import java.io.Serializable;

/**
 */
public interface CstudioCacheManager {

	/**
	 * put an item in cache 
	 * 
	 * @param scope
	 * 			scope where this item is cached
     * @param key
 * 			key to find the target
     * @param item
     */
	public void put(Scope scope, String key, Serializable item);


    public void put(Scope scope, String key, String site, Serializable item);
	
	/**
	 * get a cached item
	 * 
	 * @param scope
	 * 			scope where the target item is cached
	 * @param key
	 * 			key to find the target
	 * @return item in cache
	 */
	public Serializable get(Scope scope, String key);

    public Serializable get(Scope scope, String key, String site);
	/**
	 * invalidate the cached item
	 * 
	 * @param scope
	 * 			scope where the target item is cached
     * @param key
     */
	public void invalidate(Scope scope, String key);
	
	/**
	 * invalidate all items within the given scope
	 * 
	 * @param scope
     */
	public void invalidate(Scope scope);
	
	/**
	 * invalidate all items
	 */
	public void invalidate();
	
	/**
	 * generate one single key from given params
	 *  	 
	 */
	public String generateKey(Object... params);

    public void invalidateAndRemoveFromQueue(String fullpath, String site);
	
}
