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
package org.craftercms.studio.api.v1.util.filter;

import org.craftercms.studio.api.v1.service.ServicesManager;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v1.to.ContentItemTO;
import org.craftercms.studio.impl.v1.util.ContentUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.craftercms.studio.api.v1.constant.StudioConstants.*;

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

    protected ServicesConfig servicesConfig;

    public ServicesConfig getServicesConfig() { return servicesConfig; }
    public void setServicesConfig(ServicesConfig servicesConfig) { this.servicesConfig = servicesConfig; }

    @Override
    public boolean accept(ContentItemTO item, String filterType) {
        if(item != null) {
            Filter filter = getFilter(filterType);
            return filter.filter(item);
        }
        return false;
    }

    @Override
    public boolean accept(String site, ContentItemTO item, String filterType) {
    	 if(item != null) {
    		 return accept(site, item.uri, filterType);
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
        if (CONTENT_TYPE_COMPONENT.equalsIgnoreCase(filterType)) {
            List<String> toRet = servicesConfig.getComponentPatterns(site);
            List<String> levelConfig = servicesConfig.getLevelDescriptorPatterns(site);
            if(levelConfig!=null)
            	toRet.addAll(levelConfig);
            return toRet;
        } else if (CONTENT_TYPE_ASSET.equalsIgnoreCase(filterType)) {
            return servicesConfig.getAssetPatterns(site);
        }  else if (CONTENT_TYPE_RENDERING_TEMPLATE.equalsIgnoreCase(filterType)) {
            return servicesConfig.getRenderingTemplatePatterns(site);
        } else if (CONTENT_TYPE_DOCUMENT.equalsIgnoreCase(filterType)) {
            return servicesConfig.getDocumentPatterns(site);
        } else if (CONTENT_TYPE_ALL.equalsIgnoreCase(filterType)) {
            return Arrays.asList(".*");
        } else if (CONTENT_TYPE_PAGE.equalsIgnoreCase(filterType)) {
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
				return ContentUtils.matchesPatterns(relativePath, patterns);
			}
		}
		return false;
	}
}
