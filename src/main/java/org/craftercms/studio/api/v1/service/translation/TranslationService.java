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
package org.craftercms.studio.api.v1.service.translation;

import java.io.InputStream;
import java.util.List;

/**
 * Provide support for facilitating translations with an external translation provider
 * @author rdanner
 *
 */
public interface TranslationService {

	/**
	 * Given a source site, a set of paths and a target site calcualate the files in the srcPaths set that need to be 
	 * translated because they exist in the target site.
	 *
	 * If the target site and the source site are the same (perhaps you are managing translations as branches in the same site)
	 * The intersaction is the same.
	 * @param srcSite the id of source site
	 * @param srcPaths the paths of the content updated in the source site
	 * @param targetSite the id of the target site
	 * @return a list of file paths that represent the set
	 */
	List<String> calculateTargetTranslationSet(String srcSite, List<String> srcPaths, String targetSite);

	/**
	 * Given a site, a source language, a target language and a path submit the item for translation
	 * @param sourceSite the source site where the source content is house
	 * @param sourceLanguage the source language for the content
	 * @param targetLanguage the target language for the translation
	 * @param path the path to the content
	 */
	void translate(String sourceSite, String sourceLanguage, String targetLanguage, String path);

	/**
	 * Get a percent complete status update on in flight translation
	 * @param sourceSite the site name of the content to be translated
	 * @param targetLanguage the target language for the translation
	 * @param path the path to content
	 */
	int getTranslationStatusForItem(String sourceSite, String targetLanguage, String path);


	/**
	 * Return the translated version of the content for a given item
	 * @param sourceSite the source site name of the content to be translated
	 * @param targetLanguage the target language for the translation
	 * @param path the path to content
	 */
	InputStream getTranslatedContentForItem(String sourceSite, String targetLanguage, String path);

	/**
	 * Update site content with the translated content.
	 * Service will help make associations to source content?
	 * @param targetSite the site name of the content to be translated
	 * @param path the path to content
	 * @param content the content stream
	 */
	void updateSiteWithTranslatedContent(String targetSite, String path, InputStream content);
}
