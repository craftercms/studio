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
package org.craftercms.studio.impl.v1.service.translation.provider.demo;

import java.io.ByteArrayInputStream;
import java.lang.Integer;
import java.io.InputStream;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.impl.v1.service.translation.TranslationProvider;
import org.dom4j.io.SAXReader;
import org.dom4j.Document;
import org.dom4j.Node;

import org.apache.commons.io.IOUtils;

import java.util.regex.Pattern;
import java.util.regex.Matcher;


/**
 * Demo translation provider
 * note this implementation is a toy for testing and holds the content in memory.
 * This is not a model for real providers nor should it be expected to work against large volumes of content
 *
 * Reverses content in tags
 * @author rdanner
 */
public class DemoTranslationProvider implements TranslationProvider {

	// Note: Because this implementatin is a toy log messages are left as literals
	
	private static final Logger logger = LoggerFactory.getLogger(DemoTranslationProvider.class);

	/**
	 * constructor
	 */
	public DemoTranslationProvider() { 
		_progress = new HashMap<String, Integer>();
		_content = new HashMap<String, byte[]>();
	}

	@Override
	public void translate(String siteName, String sourceLanguage, String targetLanguage, String filename, InputStream content) {
		
		try {
			if(content != (InputStream)null) {
				byte[] bytes = IOUtils.toByteArray(content);
				_content.put(filename, bytes);
			}
		}
		catch(Exception err) {
			logger.error("error getting content and storing it in a map as byte array:"+filename, err);
		}
		
		_progress.put(filename, 0);
			
	}

	@Override
	public int getTranslationStatusForItem(String siteName, String targetLanguage, String path) {
		int progress = 0;
		int value = 0;
		
		try { 
			value = _progress.get(path);
		}
		catch(Exception err) {
			// progress not set on item
			value = 0;
		}

		progress = value + 30;
		if(progress > 100) {
			progress = 100;
		}
			
		_progress.put(path, progress);

		return progress;
	}

	@Override
	public InputStream getTranslatedContentForItem(String siteName, String targetLanguage, String path) {
		InputStream retTranslatedContent = null;
		byte[] contentBytes = _content.get(path);
		
		if(contentBytes != null) {
			
			if(path.endsWith(".xml") || path.endsWith(".XML")) {
				try {
					InputStream docInputStream = new ByteArrayInputStream(contentBytes);
					SAXReader saxReader = new SAXReader();    		
		    		Document document = saxReader.read(docInputStream);
		    		
					for(String element : _translateElements) {
						List<Node> valueEls = document.selectNodes("//"+element);
						
						if(valueEls != null && valueEls.size() > 0) {
							// translate each value
							String translation = "";
							Node valueEl = valueEls.get(0);
							String value = valueEl.getText();

							translation = value;
							Pattern contentPattern = Pattern.compile(">(.*?)<");
							Matcher regexMatcher = contentPattern.matcher(value);
							while (regexMatcher.find()) {
							    String content = regexMatcher.group(1);
							    if(!".".equals(content.trim())) {
							    	translation = translation.replaceAll(content, new StringBuffer(content).reverse().toString());
							    }
							} 
							
							// put it back in the xml document (but translated)
							valueEl.setText(translation);
						}
					}
				
					// get the entire translation and return as bytes
					String translatedDocument = document.asXML();
					retTranslatedContent = new ByteArrayInputStream(translatedDocument.getBytes("UTF-8"));
				}
				catch(Exception err) {
					// log error
					logger.error("issue during translation", err);
				}
			}
			else {
				retTranslatedContent = new ByteArrayInputStream(contentBytes);
			}
		}
		
		return retTranslatedContent;
	}

	public List<String> getTranslateElements() { return _translateElements; }
	public void setTranslateElements(List<String> list) { _translateElements = list; }
	
	protected List<String> _translateElements;
	protected Map<String, Integer> _progress;
	protected Map<String, byte[]> _content;
}
