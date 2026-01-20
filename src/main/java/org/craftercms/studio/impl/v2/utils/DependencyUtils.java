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

package org.craftercms.studio.impl.v2.utils;

import org.apache.commons.collections4.MapUtils;
import org.apache.ibatis.session.SqlSession;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.service.dependency.DependencyResolver.ResolvedDependency;
import org.craftercms.studio.api.v2.dal.Dependency;
import org.craftercms.studio.api.v2.dal.DependencyDAO;
import org.craftercms.studio.api.v2.service.dependency.DependencyService;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * Utility class for Dependency related helper methods
 */
public class DependencyUtils {

	/* A dependency path is invalid if contains line feeds or exceeds 4000 characters*/
	public static final String INVALID_DEPENDENCY_PATH_REGEX = "\\n";
	public static final Pattern INVALID_DEPENDENCY_PATH_PATTERN = Pattern.compile(INVALID_DEPENDENCY_PATH_REGEX);
	public static final Integer MAX_DEPENDENCY_PATH_LENGTH = 4000;

	/**
	 * Update the dependencies for the given path
	 *
	 * @param siteId            the site id
	 * @param path              the content item path
	 * @param oldPath           the content item old path
	 * @param dependencyService the dependency service
	 * @param dependencyDao     the dependency mapper
	 * @param sqlSession        the sql session
	 */
	public static void updateDependencies(String siteId, String path, String oldPath,
										  DependencyService dependencyService, DependencyDAO dependencyDao, SqlSession sqlSession) throws SiteNotFoundException {
		updateDependencies(siteId, path, oldPath, dependencyService, dependencyDao, sqlSession, true, true);
	}

	/**
	 * Update the dependencies for the given path
	 *
	 * @param siteId            the site id
	 * @param path              the content item path
	 * @param oldPath           the content item old path
	 * @param dependencyService the dependency service
	 * @param dependencyDao     the dependency mapper
	 * @param sqlSession        the sql session
	 * @param cleanExisting     if true, the existing dependencies for the path will be deleted
	 * @param revalidate        if true, the existing dependencies pointing to the path will be set to valid=true
	 */
	public static void updateDependencies(String siteId, String path, String oldPath,
										  DependencyService dependencyService, DependencyDAO dependencyDao,
										  SqlSession sqlSession,  boolean cleanExisting, boolean revalidate) throws SiteNotFoundException {
		if (cleanExisting) {
			if (isEmpty(oldPath)) {
				dependencyDao.deleteItemDependencies(siteId, path);
			} else {
				dependencyDao.deleteItemDependencies(siteId, oldPath);
				// Invalidate existing dependencies pointing to the old item path
				dependencyDao.invalidateDependencies(siteId, oldPath);
			}
		}
		if (revalidate) {
			// Validate existing broken dependencies pointing to the item path
			dependencyDao.validateDependencies(siteId, path);
		}

		if (!dependencyService.isValidDependencySource(siteId, path)) {
			// Path is not a valid dependency source. e.g.: an image or a txt
			return;
		}
		Map<String, Set<ResolvedDependency>> dependencies = dependencyService.resolveDependencies(siteId, path);
		if (MapUtils.isEmpty(dependencies)) {
			return;
		}

		List<Dependency> newDependencies = dependencies.entrySet().stream()
			.flatMap(entry -> entry.getValue().stream()
				.filter(dependency -> isValidDependencyPath(dependency.path()))
				.map(dependency -> {
					Dependency newDependency = new Dependency();
					newDependency.setSite(siteId);
					newDependency.setSourcePath(path);
					newDependency.setTargetPath(dependency.path());
					newDependency.setType(entry.getKey());
					newDependency.setValid(dependency.valid());
					return newDependency;
				})
			)
			.toList();

		dependencyDao.insertItemDependencies(newDependencies);
		sqlSession.flushStatements();
	}

	/**
	 * A dependency path is valid if the length is less than 4000 characters and does not contain line feeds
	 *
	 * @param path the dependency target path
	 * @return true if the path is valid, false otherwise
	 */
	public static boolean isValidDependencyPath(final String path) {
		return path.length() <= MAX_DEPENDENCY_PATH_LENGTH &&
			!INVALID_DEPENDENCY_PATH_PATTERN.matcher(path).matches();
	}
}
