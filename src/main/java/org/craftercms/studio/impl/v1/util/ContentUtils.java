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
package org.craftercms.studio.impl.v1.util;

import org.craftercms.studio.api.v1.service.activity.CStudioActivityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class ContentUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(ContentUtils.class);

    /**
     * release resources
     *
     * @param in
     *//*
    public static void release(final InputStream in, final OutputStream out) {
        try {
            if (in != null) {
                in.close();
            }
        } catch (IOException e) {
            LOGGER.error("Failed to release a resource.", e);
        }  finally {
            IOUtils.closeQuietly(in);
        }
        try {
            if (out != null) {
                out.close();
            }
        } catch (IOException e) {
            LOGGER.error("Failed to release a resource.", e);
        } finally {
            IOUtils.closeQuietly(out);
        }
    }*/

    /**
     * release resource
     *
     * @param in
     */
	/*
    public static void release(InputStream in) {
        try {
            if (in != null) {
                in.close();
            }
        } catch (IOException e) {
            LOGGER.error("Failed to relase a resource.", e);
        } finally {
            IOUtils.closeQuietly(in);
        }
    }*/

    /**
     * release resource
     *
     * @param out
     *//*
    public static void release(OutputStream out) {
        try {
            if (out != null) {
                out.close();
            }
        } catch (IOException e) {
            LOGGER.error("Failed to relase a resource.", e);
        } finally {
            IOUtils.closeQuietly(out);
        }
    }*/


    /**
     * release a reader
     *
     * @param reader
     *//*
    public static void release(Reader reader) {
        try {
            if (reader != null) {
                reader.close();
            }
        } catch (IOException e) {
            LOGGER.error("Failed to relase a reader.", e);
        } finally {
            IOUtils.closeQuietly(reader);
        }
    }*/

	/**
	 * check if two serializable values are the same
	 * 
	 * @param value1
	 * @param value2
	 * @return true if they are equal
	 *//*
	public static boolean areEqual(Serializable value1, Serializable value2) {
		if (value1 == null && value2 == null) {
			return true;
		} else if (value1 == null || value2 == null) { 
			return false;
		} else {
			return value1.equals(value2);
		}
	}*/


	/**
	 * convert InputStream to string
	 * 
	 * @param is
	 * @return string
	 *//*
	public static String convertStreamToString(InputStream is) {
		BufferedReader bufferedReader = null;
		InputStreamReader inputReader = null;
		StringBuilder sb = new StringBuilder();
		String line = null;
		try {
			inputReader = new InputStreamReader(is);
			bufferedReader = new BufferedReader(inputReader);
			while ((line = bufferedReader.readLine()) != null) {
				sb.append(line + "\n");
			}
			return sb.toString();
		} catch (IOException e) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("Error while coverting stream to string", e);
			}			
			return null;
        } finally {
            ContentUtils.release(bufferedReader);
			ContentUtils.release(inputReader);
			ContentUtils.release(is);
        }
	}*/
	
	/**
	 * convert InputStream to string
	 * 
	 * @param is
	 * @return string
	 *//*
	public static Document convertStreamToXml(InputStream is) throws DocumentException {
        InputStreamReader isReader = null;
		try {
            isReader = new InputStreamReader(is, CStudioConstants.CONTENT_ENCODING);
			SAXReader saxReader = new SAXReader();
			return saxReader.read(isReader);
		} catch (DocumentException e) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("Error while coverting stream to XML", e);
                throw e;
			}
			return null;
		} catch (UnsupportedEncodingException e) {
            LOGGER.error("Error while coverting stream to XML", e);
            return null;
        } finally {
            ContentUtils.release(is);
            ContentUtils.release(isReader);
        }
	}*/
	
	/*
	 * Escape JSON characters
	 *//*
	public static String jsonEscape(String s) {
		if ( (s == null) || (s.length() <=0) ) {
			return s;
		}
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < s.length(); i++) {
			char ch = s.charAt(i);
			switch (ch) {
			case '"' :
				sb.append("\\\"");
				break;
			case '\\' :
				sb.append("\\\\");
				break;
			case '\b' :
				sb.append("\\b");
				break;
			case '\f' :
				sb.append("\\f");
				break;
			case '\n' :
				sb.append("\\n");
				break;
			case '\r' :
				sb.append("\\r");
				break;
			case '\t' :
				sb.append("\\t");
				break;
			case '/' :
				sb.append("\\/");
				break;
			default:
				if (ch >= '\u0000' && ch <= '\u001F') {
					String ss = Integer.toHexString(ch);
					sb.append("\\u");
					for (int k = 0; k < (4 - ss.length()); k++) {
						sb.append("0");
					}
					sb.append(ss.toUpperCase());
				} else {
					sb.append(ch);
				}
			}
		}
		return sb.toString();
	}*/


	/**
	 * create a map from the given string 
	 * 
	 * @param mappingString
	 * 			string to create a map from
	 * @param concatPattern
	 * 			entry separator
	 * @param assignPattern
	 * 			key and value separator
	 * @return a map of keys and values
	 *//*
	public static Map<String, String> createMapping(String mappingString, String concatPattern, String assignPattern) {
		Map<String, String> mapping = new HashMap<String, String>();
		if (!StringUtils.isEmpty(mappingString)) {
			StringTokenizer tokenizer = new StringTokenizer(mappingString, concatPattern);
			if (tokenizer.countTokens() > 0) {
				while (tokenizer.hasMoreTokens()) {
					String token = tokenizer.nextToken();
					String[] pair = token.split(assignPattern);
					if (pair.length == 2) {
						mapping.put(pair[0], pair[1]);
					} 
				}
			}
		} 
		return mapping;
	}*/

	/**
	 * convert a content type to a key that is used to identify content type configuration location
	 * 
	 * @param contentType
	 * @return content type key
	 *//*
	public static String getContentTypeKey(String contentType) {
		contentType = contentType.replaceAll(":", "/");
		String [] levels = contentType.split("/");
		String folderPath = "";
		if (levels != null) {
			for (String level : levels) {
				if (!StringUtils.isEmpty(level)) {
					folderPath += "/" + NamespaceService.CONTENT_MODEL_PREFIX + ":" + level;
				}
			}
		}
		return folderPath;
	}*/

	/**
	 * content the given document to stream
	 * 
	 * @param document
	 * @param encoding
	 * @return XML as stream
	 *//*
	public static InputStream convertDocumentToStream(Document document, String encoding) {
		try {
			return new ByteArrayInputStream(
					(XmlUtils.convertDocumentToString(document)).getBytes(encoding));
		} catch (UnsupportedEncodingException e) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("Failed to convert document to stream with encoding: " + encoding, e);
			}
			return null;
		} catch (IOException e) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("Failed to convert document to stream with encoding: " + encoding, e);
			}
			return null;
		}
	}*/

	/**
	 * generate activity type key
	 * @param activity
	 * @return
	 */
	public static String generateActivityValue(CStudioActivityService.ActivityType activity) {
		return CStudioActivityService.ACTIVITY_TYPE_KEY_PREFIX + activity.toString().toLowerCase();
	}
	

}
