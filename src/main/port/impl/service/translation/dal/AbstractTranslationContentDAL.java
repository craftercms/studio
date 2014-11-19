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
package org.craftercms.cstudio.impl.service.translation.dal;

import java.io.InputStream;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

import org.craftercms.cstudio.api.repository.*;
import org.craftercms.cstudio.api.service.translation.*;

/**
 * abstract translation content DAL provides implementations of methods common across all stores
 * @author russdanner
 */
public abstract class AbstractTranslationContentDAL implements TranslationContentDAL {

	/**
	 * given a source site, a set of paths and a target site calcualate the files in the srcPaths set that need to be 
	 * translated because they exist in the target site.
     *
	 * If the target site and the source site are the same (perhaps you are managing translations as branches in the same site)
	 * The intersaction is the same.
	 * @param srcSite the id of source site
	 * @param srcPaths the paths of the content updated in the source site
	 * @param targetSite the id of the target site
	 * @return a list of file paths that represent the set
	 */
	public List<String> calculateTargetTranslationSet(String srcSite, List<String> srcPaths, String targetSite) {
		List<String> retTranslationSet = new ArrayList<String>();
		
		for(String srcPath : srcPaths) {
			if((srcSite == targetSite) || isSourcePathInTarget(targetSite, srcPath)) {
				retTranslationSet.add(srcPath);
			}
		}
		
		return retTranslationSet;
	}

	/**
	 * @return true if target has sourcePath in project
	 */
	protected abstract boolean isSourcePathInTarget(String targetSite, String sourcePath);
}