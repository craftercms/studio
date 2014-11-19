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
package org.craftercms.cstudio.alfresco.dm.filter;

import org.craftercms.cstudio.alfresco.dm.constant.DmConstants;
import org.craftercms.cstudio.alfresco.dm.to.DmContentItemTO;
import org.craftercms.cstudio.alfresco.dm.util.DmUtils;
import org.craftercms.cstudio.alfresco.service.ServicesManager;
import org.craftercms.cstudio.alfresco.service.api.ServicesConfig;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class DmFilterWrapperImpl implements DmFilterWrapper {

    protected Map<String, Filter> _filterMap;
    public Map<String, Filter> getFilterMap() {
        return _filterMap;
    }
    public void setFilterMap(Map<String, Filter> filterMap) {
        this._filterMap = filterMap;
    }
    protected Filter _defaultFilter;
    public Filter getDefaultFilter() {
        return _defaultFilter;
    }
    public void setDefaultFilter(Filter defaultFilter) {
        this._defaultFilter = defaultFilter;
    }

    protected ServicesManager servicesManager;
    public void setServicesManager(ServicesManager servicesManager) {
        this.servicesManager = servicesManager;
    }

    @Override
    public boolean accept(DmContentItemTO item, String filterType) {
        if(item != null) {
            Filter filter = getFilter(filterType);
            return filter.filter(item);
        }
        return false;
    }

    @Override
    public boolean accept(String site, DmContentItemTO item, String filterType) {
    	 if(item != null) {
    		 return accept(site, item.getUri(), filterType);
    	 }
    	 return false;
    }

    protected Filter getFilter(String filterType) {
        Filter filter = _defaultFilter;
        if(filterType != null && _filterMap.get(filterType.toLowerCase()) != null) {
            filter = _filterMap.get(filterType.toLowerCase());
        }
        return filter;
    }

    protected List<String> getFilterPatterns(String site, String filterType) {
        ServicesConfig servicesConfig = servicesManager.getService(ServicesConfig.class);
        if (DmConstants.CONTENT_TYPE_COMPONENT.equalsIgnoreCase(filterType)) {
            List<String> toRet = servicesConfig.getComponentPatterns(site);
            List<String> levelConfig = servicesConfig.getLevelDescriptorPatterns(site);
            if(levelConfig!=null)
            	toRet.addAll(levelConfig);
            return toRet;
        } else if (DmConstants.CONTENT_TYPE_ASSET.equalsIgnoreCase(filterType)) {
            return servicesConfig.getAssetPatterns(site);
        }  else if (DmConstants.CONTENT_TYPE_RENDERING_TEMPLATE.equalsIgnoreCase(filterType)) {
            return servicesConfig.getRenderingTemplatePatterns(site);
        } else if (DmConstants.CONTENT_TYPE_DOCUMENT.equalsIgnoreCase(filterType)) {
            return servicesConfig.getDocumentPatterns(site);
        } else if (DmConstants.CONTENT_TYPE_ALL.equalsIgnoreCase(filterType)) {
            return Arrays.asList(".*");
        } else if (DmConstants.CONTENT_TYPE_PAGE.equalsIgnoreCase(filterType)) {
            return servicesConfig.getPagePatterns(site);
        } else {
            return null;
        }
    }

	@Override
	public boolean accept(String site, String relativePath, String filterType) {
		if (relativePath != null) {
			List<String> patterns = getFilterPatterns(site, filterType);
			if (patterns != null) {
				return DmUtils.matchesPattern(relativePath, patterns);
			}
		}
		return false;
	}
}
