/*
 * Crafter Studio Web-content authoring solution
 * Copyright (C) 2007-2016 Crafter Software Corporation.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.craftercms.studio.impl.v1.util;


import org.dom4j.Document;
import org.dom4j.io.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;

/**
 * This class provides common methods for handling XML
 * 
 * @author hyanghee
 * 
 */
public class XmlUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(XmlUtils.class);

	/**
	 * convert document to string
	 * 
	 * @param document
	 * @return XML as String
	 * @throws java.io.IOException
	 */
	public static String convertDocumentToString(Document document) throws IOException {
		StringWriter sw = new StringWriter();
		XMLWriter writer = new XMLWriter(sw);
		try {
			writer.write(document);
			writer.flush();
			return sw.toString();
		} finally {
			sw.close();
			writer.close();
		}
	}
}
