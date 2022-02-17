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
package org.craftercms.studio.api.v1.content.pipeline;

import java.io.InputStream;
import java.util.Map;

import org.craftercms.studio.api.v1.exception.ContentProcessException;
import org.dom4j.Document;


/**
 * wrapper to content that is being processed through the content processor pipeline
 * 
 * @author hyanghee
 *
 */
public interface PipelineContent {

	public static final String KEY_CONTENT_TYPE = "contentType";
	public final static String KEY_FILE_NAME = "fileName";
	public final static String KEY_SITE = "site";
	public final static String KEY_PATH = "path";
	
	/**
	 * get the id of this content
	 * 
	 * @return id
	 */
	public String getId();
	
	/**
	 * set the id of this content 
	 * 
	 * @param id
	 */
	public void setId(String id);
	
	/**
	 * get this content as input stream
	 * 
	 * @return input stream
	 * @throws ContentProcessException 
	 */
	public InputStream getContentStream() throws ContentProcessException;

	/**
	 * set content input stream.this will reset document.
	 * 
	 * @param contentStream
	 */
	public void setContentStream(InputStream contentStream);
	
	/**
	 * get this content as XML Document
	 * 
	 * @return document
	 * @throws ContentProcessException 
	 */
	public Document getDocument() throws ContentProcessException;
	
	/**
	 * set the content document. this will reset content stream
	 * 
	 * @param document
	 */
	public void setDocument(Document document);
	
	/**
	 * get the encoding of this content
	 * 
	 * @return encoding
	 */
	public String getEncoding();
	
	/**
	 * set the encoding of this content
	 * 
	 * @param encoding
	 */
	public void setEncoding(String encoding);
	
	/**
	 * get properties of this content
	 * 
	 * @return properties
	 */
	public Map<String, String> getProperties();

	/**
	 * set content properties
	 * 
	 * @param properties
	 */
	public void setProperties(Map<String, String> properties);
	
	/**
	 * add a content property
	 * 
	 * @param key
	 * @param value
	 */
	public void addProperty(String key, String value);
	
	/**
	 * get a property value given the key
	 * 
	 * @param key
	 * @return property value
	 */
	public String getProperty(String key);
	
	/**
	 * close this content 
	 */
	public void closeContentStream();
}
