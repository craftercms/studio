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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.xml.sax.SAXException;

import java.io.*;
import java.util.List;

import static org.craftercms.studio.api.v1.constant.StudioConstants.FILE_SEPARATOR;


public class ContentUtils {

	private static final Logger logger = LoggerFactory.getLogger(ContentUtils.class);

    /**
     * release resource
     *
     * @param in
     */
    public static void release(InputStream in) {
        try {
            if (in != null) {
                in.close();
            }
        } catch (IOException e) {
            logger.error("Failed to InputStream", e);
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    /**
     * release resource
     *
     * @param out
     */
    public static void release(OutputStream out) {
        try {
            if (out != null) {
                out.close();
            }
        } catch (IOException e) {
			logger.error("Failed to OutputStream", e);
        } finally {
            IOUtils.closeQuietly(out);
        }
    }


    /**
     * release a reader
     *
     * @param reader
     */
    public static void release(Reader reader) {
        try {
            if (reader != null) {
                reader.close();
            }
        } catch (IOException e) {
			logger.error("Failed to Reader", e);
        } finally {
            IOUtils.closeQuietly(reader);
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
			} catch (SAXException e){
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

	public static String getParentUrl(String url) {
		int lastIndex = url.lastIndexOf(FILE_SEPARATOR);
		return url.substring(0, lastIndex);
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
}
