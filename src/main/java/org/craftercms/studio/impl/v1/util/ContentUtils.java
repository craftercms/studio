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
package org.craftercms.studio.impl.v1.util;

import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v1.constant.StudioConstants;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.activity.ActivityService;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.Date;
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
            logger.error("Failed to release a resource.", e);
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
            logger.error("Failed to relase a resource.", e);
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
            logger.error("Failed to release a reader.", e);
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
			return saxReader.read(isReader);
		} catch (DocumentException e) {
				logger.error("Error while coverting stream to XML", e);
			return null;
		} catch (UnsupportedEncodingException e) {
            logger.error("Error while coverting stream to XML", e);
            return null;
        } finally {
            ContentUtils.release(is);
            ContentUtils.release(isReader);
        }
	}

	public static Date getEditedDate(String str) {
		if (!StringUtils.isEmpty(str)) {
			try {
				return (new ISO8601DateFormat()).parse(str);
			} catch (ParseException e) {
				logger.error("No activity post date provided.");
				return null;
			}
		} else {
			logger.error("No activity post date provided.");
			return null;
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

	public static String getMd5ForFile(InputStream input) {
		String result = null;
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("MD5");

			md.reset();
			byte[] bytes = new byte[1024];
			int numBytes;
			input.mark(Integer.MAX_VALUE);
			while ((numBytes = input.read(bytes)) != -1) {
				md.update(bytes, 0, numBytes);
			}
			byte[] digest = md.digest();
			result = new String(Hex.encodeHex(digest));
			input.reset();
		} catch (NoSuchAlgorithmException e) {
			logger.error("Error while creating MD5 digest", e);
		} catch (IOException e) {
			logger.error("Error while reading input stream", e);
		} finally {

		}
		return result;
	}

    public static String getMd5ForFile(String data) {
        InputStream is = null;
        String fileName = null;

        try {
            is = new ByteArrayInputStream(data.getBytes("UTF-8"));

            fileName = getMd5ForFile(is);
        } catch(UnsupportedEncodingException e) {
            logger.error("Error while creating MD5 digest", e);
        }
        return fileName;
    }

	public static String getParentUrl(String url) {
		int lastIndex = url.lastIndexOf(FILE_SEPARATOR);
		return url.substring(0, lastIndex);
	}

	/**
	 * Returns the page name part (eg.index.xml) of a given URL
	 *
	 * @param url
	 * @return
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
		} catch (UnsupportedEncodingException e) {
			logger.error("Failed to convert document to stream with encoding: " + encoding, e);
			return null;
		} catch (IOException e) {
			logger.error("Failed to convert document to stream with encoding: " + encoding, e);
			return null;
		}
	}
}
