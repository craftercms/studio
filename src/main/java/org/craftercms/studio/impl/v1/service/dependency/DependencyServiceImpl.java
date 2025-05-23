/*
 * Copyright (C) 2007-2025 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.impl.v1.service.dependency;

import org.apache.commons.collections4.CollectionUtils;
import org.craftercms.studio.api.v1.dal.DependencyMapper;
import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.service.dependency.DependencyService;
import org.craftercms.studio.api.v2.annotation.ContentPath;
import org.craftercms.studio.api.v2.annotation.RequireContentExists;
import org.craftercms.studio.api.v2.annotation.RequireSiteExists;
import org.craftercms.studio.api.v2.annotation.SiteId;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.craftercms.studio.api.v1.dal.DependencyMapper.*;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_DEPENDENCY_ITEM_SPECIFIC_PATTERNS;

public class DependencyServiceImpl implements DependencyService {

	private static final Logger logger = LoggerFactory.getLogger(DependencyServiceImpl.class);

	protected DependencyMapper dependencyMapper;
	protected StudioConfiguration studioConfiguration;
	protected ContentService contentService;

	@Override
	@RequireSiteExists
	public Set<String> getItemSpecificDependencies(@SiteId String site, String path, int depth)
		throws ServiceLayerException {
		// Check if content exists
		if (!contentService.contentExists(site, path)) {
			throw new ContentNotFoundException();
		}

		Set<String> toRet = new HashSet<>();
		Set<String> paths = new HashSet<>();
		boolean exitCondition = false;
		paths.add(path);
		if (depth < 0) {
			do {
				List<String> deps = getItemSpecificDependenciesFromDB(site, paths);
				exitCondition = !toRet.addAll(deps);
				paths.clear();
				paths.addAll(deps);
			} while (!exitCondition);
		} else {
			int d = depth;
			while (d-- > 0) {
				List<String> deps = getItemSpecificDependenciesFromDB(site, paths);
				exitCondition = !toRet.addAll(deps);
				paths.clear();
				paths.addAll(deps);
				if (exitCondition) break;
			}
		}
		return toRet;
	}

	private List<String> getItemSpecificDependenciesFromDB(String site, Set<String> paths) {
		if (CollectionUtils.isEmpty(paths)) {
			return new ArrayList<>();
		}
		Map<String, Object> params = new HashMap<>();
		params.put(SITE_PARAM, site);
		params.put(PATHS_PARAM, paths);
		params.put(REGEX_PARAM, getItemSpecificDependenciesPatterns());
		return dependencyMapper.getItemSpecificDependenciesForList(params);
	}

	@Override
	@RequireContentExists
	public Set<String> getItemDependencies(@SiteId String site, @ContentPath String path, int depth)
		throws ServiceLayerException {
		logger.debug("Get item dependencies for site '{}' path '{}'", site, path);

		Set<String> toRet = new HashSet<>();
		Set<String> paths = new HashSet<>();
		paths.add(path);
		boolean exitCondition;
		if (depth < 0) {
			do {
				List<String> deps = getItemDependenciesFromDB(site, paths);
				exitCondition = !toRet.addAll(deps);
				paths.clear();
				paths.addAll(deps);
			} while (!exitCondition);
		} else {
			int d = depth;
			while (d-- > 0) {
				List<String> deps = getItemDependenciesFromDB(site, paths);
				exitCondition = !toRet.addAll(deps);
				if (exitCondition) break;
			}
		}
		return toRet;
	}

	private List<String> getItemDependenciesFromDB(String site, Set<String> paths) {
		if (CollectionUtils.isEmpty(paths)) {
			return new ArrayList<>();
		}
		Map<String, Object> params = new HashMap<>();
		params.put(SITE_PARAM, site);
		params.put(PATHS_PARAM, paths);
		return dependencyMapper.getDependenciesForList(params);
	}

	@Override
	@RequireContentExists
	public Set<String> getItemsDependingOn(@SiteId String site, @ContentPath String path, int depth)
		throws ServiceLayerException {
		logger.debug("Get items depending on item site '{}' path '{}'", site, path);
		Set<String> toRet = new HashSet<>();
		Set<String> paths = new HashSet<>();
		paths.add(path);
		if (depth < 0) {
			do {
				List<String> deps = getItemsDependingOnFromDB(site, paths);
				toRet.addAll(deps);
				paths.clear();
				paths.addAll(deps);
			} while (!CollectionUtils.isNotEmpty(paths));
		} else {
			int d = depth;
			while (d-- > 0) {
				List<String> deps = getItemsDependingOnFromDB(site, paths);
				toRet.addAll(deps);
				paths.clear();
				paths.addAll(deps);
			}
		}

		return toRet;
	}

	private List<String> getItemsDependingOnFromDB(String site, Set<String> paths) {
		if (CollectionUtils.isEmpty(paths)) {
			return new ArrayList<>();
		}
		Map<String, Object> params = new HashMap<>();
		params.put(SITE_PARAM, site);
		params.put(PATHS_PARAM, paths);
		return dependencyMapper.getItemsDependingOn(params);
	}

	protected List<String> getItemSpecificDependenciesPatterns() {
		StringTokenizer st = new StringTokenizer(
			studioConfiguration.getProperty(CONFIGURATION_DEPENDENCY_ITEM_SPECIFIC_PATTERNS), ",");
		List<String> itemSpecificDependenciesPatterns = new ArrayList<>(st.countTokens());
		while (st.hasMoreTokens()) {
			itemSpecificDependenciesPatterns.add(st.nextToken().trim());
		}
		return itemSpecificDependenciesPatterns;
	}

	public void setStudioConfiguration(StudioConfiguration studioConfiguration) {
		this.studioConfiguration = studioConfiguration;
	}

	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}

	public void setDependencyMapper(DependencyMapper dependencyMapper) {
		this.dependencyMapper = dependencyMapper;
	}
}
