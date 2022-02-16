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
package org.craftercms.studio.impl.v1.content.pipeline;

import org.craftercms.studio.api.v1.content.pipeline.PipelineContent;
import org.craftercms.studio.api.v1.exception.ContentProcessException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.impl.v1.util.ContentUtils;
import org.craftercms.studio.impl.v1.util.XmlUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class PipelineContentImpl implements PipelineContent {

    /** content id **/
    protected String _id = "";
    /** content as document **/
    protected Document _document = null;
    /** content as input stream **/
    protected InputStream _contentStream = null;
    /** content encoding **/
    protected String _encoding = null;
    /** content properties **/
    protected Map<String, String> _properties = null;
    /** is this XML content? **/
    protected boolean _xml = false;

    public static final Logger LOGGER = LoggerFactory.getLogger(PipelineContentImpl.class);

    /**
     * constructor
     */
    public PipelineContentImpl() {}

    /**
     * constructor
     *
     * @param id
     * @param contentStream
     * @param isXml
     * @param document
     * @param encoding
     * @param properties
     */
    public PipelineContentImpl(String id, InputStream contentStream, boolean isXml, Document document, String encoding, Map<String, String> properties) {
        this._id = id;
        this._contentStream = contentStream;
        this._document = document;
        this._encoding = encoding;
        this._xml = isXml;
        this._properties = properties;
    }

    @Override
    public String getId() {
        return _id;
    }

    @Override
    public void setId(String id) {
        this._id = id;
    }

    @Override
    public InputStream getContentStream() throws ContentProcessException {
        // make sure we don't use up the input stream before getting document
        if (_xml && _document == null) {
            getDocument();
        }
        if (_contentStream == null) {
            if (_document != null) {
                try {
                    _contentStream = new ByteArrayInputStream(
                            (XmlUtils.convertDocumentToString(_document)).getBytes(_encoding));
                } catch (UnsupportedEncodingException e) {
                    throw new ContentProcessException("Error while converting " + _id + " into docuemnt.", e);
                } catch (IOException e) {
                    throw new ContentProcessException("Error while converting " + _id + " into docuemnt.", e);
                }
            } else {
                throw new ContentProcessException("Error while converting " + _id
                        + " into docuemnt. Both document and content stream cannot be null.");

            }
        }
        return _contentStream;
    }

    @Override
    public void setContentStream(InputStream contentStream) {
        this._contentStream = contentStream;
        this._document = null;
    }

    @Override
    public Document getDocument() throws ContentProcessException {
        if (_xml && _document == null) {
            if (_contentStream != null) {
                try {
                    SAXReader saxReader = new SAXReader();
                    try {
                        saxReader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
                        saxReader.setFeature("http://xml.org/sax/features/external-general-entities", false);
                        saxReader.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
                    }catch (SAXException ex){
                        LOGGER.error("Unable to turn off external entity loading, This could be a security risk.", ex);
                    }
                    saxReader.setEncoding(_encoding);
                    _document = saxReader.read(_contentStream);
                    _contentStream = null;
                } catch (DocumentException e) {
                    throw new ContentProcessException("Error while converting " + _id + " into document.", e);
                } finally {
                    ContentUtils.release(_contentStream);
                    _contentStream = null;
                }
            } else {
                throw new ContentProcessException("Error while converting " + _id
                        + " into document. Both document and content stream cannot be null.");
            }
        }
        return _document;
    }

    @Override
    public void setDocument(Document document) {
        this._document = document;
        ContentUtils.release(_contentStream);
        this._contentStream = null;
    }

    @Override
    public String getEncoding() {
        return _encoding;
    }

    @Override
    public void setEncoding(String encoding) {
        this._encoding = encoding;
    }

    @Override
    public Map<String, String> getProperties() {
        return _properties;
    }

    @Override
    public void setProperties(Map<String, String> properties) {
        this._properties = properties;
    }

    @Override
    public void addProperty(String key, String value) {
        if (_properties == null) {
            _properties = new HashMap<>();
        }
        _properties.put(key, value);
    }

    @Override
    public String getProperty(String key) {
        if (_properties != null) {
            return _properties.get(key);
        }
        return null;
    }

    @Override
    public void closeContentStream() {
        ContentUtils.release(_contentStream);
    }

}
