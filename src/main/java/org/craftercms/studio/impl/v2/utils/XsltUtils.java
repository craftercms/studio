/*
 * Copyright (C) 2007-2021 Crafter Software Corporation. All Rights Reserved.
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
package org.craftercms.studio.impl.v2.utils;

import org.apache.commons.collections.MapUtils;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

/**
 * Utility class for XSLT related operations
 *
 * @author joseross
 * @since 4.0
 */
public abstract class XsltUtils {

    public static final String SAXON_CLASS = "net.sf.saxon.TransformerFactoryImpl";

    public static void executeTemplate(InputStream template, Map<String, Object> params, URIResolver uriResolver,
                                       InputStream content, OutputStream output) throws TransformerException {
        // Saxon is used to support XSLT 2.0
        Transformer transformer =
                TransformerFactory.newInstance(SAXON_CLASS, null).newTransformer(new StreamSource(template));
        if (MapUtils.isNotEmpty(params)) {
            params.forEach(transformer::setParameter);
        }
        if (uriResolver != null) {
            transformer.setURIResolver(uriResolver);
        }
        transformer.transform(new StreamSource(content), new StreamResult(output));
    }

}
