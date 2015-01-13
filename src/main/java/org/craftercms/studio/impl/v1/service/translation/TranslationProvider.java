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
package org.craftercms.studio.impl.v1.service.translation;

import java.io.InputStream;

/**
 * Interface for external translation providers to implement
 * @author rdanner
 */
public interface TranslationProvider {
	
	/**
	 * translate or submit content for translation
	 * @param sourceSite the site name of the content to be translated
	 * @param sourceLanguage the ISO code for the source content's language
	 * @param targetLanguage the ISO country code for the target translation's language
	 * @param the file name of the content to translate (path)
	 * @param the raw bits of the content to translate
	 */
	void translate(String sourceSite, String sourceLanguage, String targetLanguage, String filename, InputStream content);
	
	/**
	 * retrieve the translated content from the system
	 * @param sourceSite the site name of the content to be translated
	 * @param targetLanguage the ISO country code for the target translation's language
	 * @param filename the path of the content (filename)
	 */
	InputStream getTranslatedContentForItem(String sourceSite, String targetLanguage, String filename);
	
	/**
	 * return a percentage complete from the translation provider
	 * @param sourceSite the site name of the content to be translated
	 * @param targetLanguage the ISO country code for the target translation's language
	 * @param filename the file to get status on
	 */
	int getTranslationStatusForItem(String sourceSite, String targetLanguage, String filename);
}
