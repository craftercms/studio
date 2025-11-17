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
package org.craftercms.studio.impl.v1.util;

import org.apache.commons.io.IOUtils;
import org.craftercms.studio.api.v1.constant.StudioConstants;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import java.io.*;
import java.util.List;

import static java.lang.String.format;
import static org.apache.commons.io.FilenameUtils.getFullPathNoEndSeparator;
import static org.apache.commons.lang3.Strings.CS;
import static org.craftercms.studio.api.v1.constant.DmConstants.SLASH_INDEX_FILE;
import static org.craftercms.studio.api.v1.constant.StudioConstants.FILE_SEPARATOR;


public class ContentUtils {

	private static final Logger logger = LoggerFactory.getLogger(ContentUtils.class);

	/**
	 * Release a resource
	 *
	 * @param resource resource to close
	 */
	public static void release(Closeable resource) {
		try {
			if (resource != null) {
				resource.close();
			}
		} catch (IOException e) {
			logger.error("Failed to release resource", e);
		} finally {
			IOUtils.closeQuietly(resource);
		}
	}

	/**
	 * convert InputStream to string
	 *
	 * @param is
	 * @return string
	 */
	public static Document convertStreamToXml(InputStream is) throws DocumentException {
		InputStreamReader isReader = null;
		try {
			isReader = new InputStreamReader(is, StudioConstants.CONTENT_ENCODING);
			SAXReader saxReader = new SAXReader();
			try {
				saxReader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
				saxReader.setFeature("http://xml.org/sax/features/external-general-entities", false);
				saxReader.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
				// TODO: SJ: Investigate the need for the following
				// TODO: SJ: saxReader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
				// TODO: SJ: saxReader.setXIncludeAware(false);
				// TODO: SJ: saxReader.setExpandEntityReferences(false);
				saxReader.setMergeAdjacentText(true);
			} catch (SAXException e) {
				logger.error("Failed to turn off external entity loading. This could be a security risk.", e);
			}
			return saxReader.read(isReader);
		} catch (DocumentException | UnsupportedEncodingException e) {
			logger.error("Failed to parse XML document", e);
			return null;
		} finally {
			ContentUtils.release(is);
			ContentUtils.release(isReader);
		}
	}

	public static boolean matchesPatterns(String uri, List<String> patterns) {
		if (patterns != null) {
			for (String pattern : patterns) {
				if (uri.matches(pattern)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Get the parent url: for folders and components it's just parent, for pages it's the parent of the parent.
	 * e.g.:
	 * /site/website/articles/page1/index.xml -> /site/website/articles
	 * /site/components/posts/january/clickbait.xml -> /site/components/posts/january
	 * /site/components/articles/health/ -> /site/components/articles
	 *
	 * @param path path of the content item
	 * @return path of the parent item
	 */
	public static String getParentUrl(String path) {
		return getFullPathNoEndSeparator(CS.removeEnd(path, SLASH_INDEX_FILE));
	}

	/**
	 * Returns the page name part (e.g.index.xml) of a given URL
	 *
	 * @param url
	 * @return page name
	 */
	public static String getPageName(String url) {
		int lastIndex = url.lastIndexOf(FILE_SEPARATOR);
		return url.substring(lastIndex + 1);
	}

	/**
	 * content the given document to stream
	 *
	 * @param document
	 * @param encoding
	 * @return XML as stream
	 */
	public static InputStream convertDocumentToStream(Document document, String encoding) {
		try {
			return new ByteArrayInputStream(
					(XmlUtils.convertDocumentToString(document)).getBytes(encoding));
		} catch (IOException e) {
			logger.error("Failed to convert XML document to String with encoding '{}'", encoding, e);
			return null;
		}
	}

	/**
	 * Returns the content item id for a given site and path. e.g.: "my-site:/site/website/test1/index.xml"
	 *
	 * @param siteId the site id
	 * @param path   the content item path
	 * @return the content item id
	 */
	public static String getContentItemId(String siteId, String path) {
		return format("%s:%s", siteId, path);
	}

	/**
	 * Helper method to update a single node element with the indicated value
	 *
	 * @param root     root element
	 * @param nodeName name of the node to update
	 * @param value    new text value of the node, if found
	 */
	public static void updateSingleDocumentNode(final Element root, final String nodeName, final String value) {
		Node node = root.selectSingleNode(format("//%s", nodeName));
		if (node != null) {
			node.setText(value);
		}
	}

	/**
	 * Reads the text of a single node in a document.
	 *
	 * @param root     root element of the document
	 * @param nodeName name of the node to read
	 * @return the text of the node, or null if the node is not found
	 */
	public static String readSingleDocumentNodeText(final Element root, final String nodeName) {
		Node node = root.selectSingleNode(format("//%s", nodeName));
		if (node != null) {
			return node.getText();
		}

		return null;
	}
}
