/*
 * Copyright (C) 2007-2026 Crafter Software Corporation. All Rights Reserved.
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
package org.craftercms.studio.api.v1.constant;

/**
 * General constants for WCM
 *
 * @author hyanghee
 */
public class DmConstants {

	public final static String INDEX_FILE = "index.xml";
	public final static String SLASH_INDEX_FILE = "/index.xml";
	public final static String XML_PATTERN = ".xml";
	public final static String CSS_PATTERN = ".css";
	public final static String JS_PATTERN = ".js";

	public final static String KEY_CONTENT_TYPE = "contentType";
	public final static String KEY_PATH = "path";
	public final static String KEY_SITE = "site";
	public final static String KEY_USER = "user";
	public static final String KEY_LIFECYCLE_CONTENT = "lifecycleContent";
	public static final String KEY_LOGGER = "logger";

	/**
	 * rename keys
	 **/
	public final static String KEY_SOURCE_PATH = "sourcePath";

	/* script object names */
	public final static String KEY_CONTENT_LOADER = "contentLoader";

	/* TODO: move this to configuration */
	public static final String ROOT_PATTERN_PAGES = "/site/website";
	public static final String ROOT_PATTERN_ASSETS = "/static-assets";
	public static final String ROOT_PATTERN_DOCUMENTS = "/site/documents";

	public static final String CONTENT_LIFECYCLE_OPERATION = "contentLifecycleOperation";

	public static final String KEY_APPLICATION_CONTEXT = "applicationContext";
}
