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

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.util.List;

import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.translation.TranslationService;
import org.craftercms.studio.impl.v1.service.translation.dal.TranslationContentDAL;

/**
 * Provide support for facilitating translations with an external translation provider
 * @author rdanner
 *
 */
public class TranslationServiceImpl implements TranslationService {

	private static final Logger logger = LoggerFactory.getLogger(TranslationServiceImpl.class);

	protected final String MSG_ERROR_SUBMITTING_ITEM_FOR_TRANSLATION = "err_submit_item_for_translation";
	protected final String MSG_ERR_TRANSLATION_CLOSE_STREAM_ON_SOURCE_CONTENT = "err_close_stream_on_translation_source_content";

	@Override
	public List<String> calculateTargetTranslationSet(String srcSite, List<String> srcPaths, String targetSite) {
		return _translationContentDAL.calculateTargetTranslationSet(srcSite, srcPaths, targetSite);
	}

	@Override
	public void translate(String sourceSite, String sourceLanguage, String targetLanguage, String path) {
		InputStream untranslatedContentStream = null;
		try {
			untranslatedContentStream = _translationContentDAL.getContent(sourceSite, path);
		} catch (ContentNotFoundException e) {
			logger.error("Content not found for {0}:{1}", e, sourceSite, path);
		}

		if(untranslatedContentStream != null) {
			try {
				// read content in to memory and close original stream
				byte[] untranslatedBytes = IOUtils.toByteArray(untranslatedContentStream);
				ByteArrayInputStream detachedContentStream = new ByteArrayInputStream(untranslatedBytes);
				
				_translationProvider.translate(sourceSite, sourceLanguage, targetLanguage, path, detachedContentStream);
			}
			catch (IOException err) {
				logger.error(MSG_ERROR_SUBMITTING_ITEM_FOR_TRANSLATION, err, sourceSite, sourceLanguage, targetLanguage, path);
			}
			finally {
				try {
					untranslatedContentStream.close();
				}
				catch(Exception err) {
					logger.error(MSG_ERR_TRANSLATION_CLOSE_STREAM_ON_SOURCE_CONTENT, err, sourceSite, sourceLanguage, targetLanguage, path);
				}
			}
		}
	}

	@Override
	public int getTranslationStatusForItem(String sourceSite, String targetLanguage, String path) {
		return _translationProvider.getTranslationStatusForItem(sourceSite, targetLanguage, path);
	}

	@Override
	public InputStream getTranslatedContentForItem(String sourceSite, String targetLanguage, String path) {
		return _translationProvider.getTranslatedContentForItem(sourceSite, targetLanguage, path);
	}

	@Override
	public void updateSiteWithTranslatedContent(String targetSite, String path, InputStream content) {
		_translationContentDAL.updateSiteWithTranslatedContent(targetSite, path, content);
	}

	/** getter translation dal */
	public TranslationContentDAL getTranslationContentDAL() { return _translationContentDAL; }
	/** setter translation dal */
	public void setTranslationContentDAL(TranslationContentDAL dal) { _translationContentDAL = dal; }

	/** getter translation provider */
	public TranslationProvider getTranslationProvider() { return _translationProvider; }
	/** setter for translation provider */
	public void setTranslationProvider(TranslationProvider provider) { _translationProvider = provider; }
	
	protected TranslationProvider _translationProvider;
	protected TranslationContentDAL _translationContentDAL;
}