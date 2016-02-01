/*******************************************************************************
 * Crafter Studio Web-content authoring solution
 *     Copyright (C) 2007-2016 Crafter Software Corporation.
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
package org.craftercms.studio.impl.v1.repository.cached;

import java.util.HashMap;

public class ResponseCache {
    
    private static ThreadContainer mThreadContainer = new ThreadContainer();

    public static void setResponseCacheForThread(ResponseCache pContainer) {
        if (mThreadContainer != null) {
            mThreadContainer.set(pContainer);
        }
    }
    
    public static ResponseCache currentResponseCache() {

        ResponseCache vRetContainer = null;
        
        if (mThreadContainer != null) {
            vRetContainer = (ResponseCache) mThreadContainer.get();
        }
        
        return vRetContainer;
    }
    
    protected static class ThreadContainer extends ThreadLocal<ResponseCache> {
        
        public ResponseCache initialValue() {

            return null;
        }
    }

	 private HashMap<String, Object> cache;

	 public ResponseCache() {
	 	cache = new HashMap<String, Object>();
	 }

     private static ThreadLocal<ResponseCache> responseMap =
         new ThreadLocal<ResponseCache>() {

             @Override protected ResponseCache initialValue() {
                 return new ResponseCache();
         }
     };

     public HashMap<String, Object> getCacheMap() {
         return cache;
     }	

     public static ResponseCache currentCacheContainer() {
         return responseMap.get();
     }	
}
