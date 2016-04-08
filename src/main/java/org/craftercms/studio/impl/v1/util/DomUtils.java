/*******************************************************************************
 * Crafter Studio Web-content authoring solution
 * Copyright (C) 2007-2016 Crafter Software Corporation.
 * <p/>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.craftercms.studio.impl.v1.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

//import com.sun.org.apache.xml.internal.serialize.OutputFormat;
//import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

/**
 * Utilities for handling XML DOMs
 *
 * @author rdanner
 */
public class DomUtils {

    /**
     * Given a string create a DOM object
     * <p/>
     * does NOT check for null strings
     *
     * @param xmlAsString
     *            dom object to convert
     * @return a dom object or null on error
     */
    public static Document createXmlDocument(String xmlAsString) {

        return createXmlDocument(xmlAsString, "UTF-8");
    }

    /**
     * Given a string create a DOM object
     * <p/>
     * does NOT check for null strings
     *
     * @param xmlAsString
     *            dom object to convert
     * @return a dom object or null on error
     */
    public static Document createXmlDocument(String xmlAsString, String encoding) {

        Document document = null;

        try {

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

            dbf.setValidating(false);
            dbf.setIgnoringComments(false);
            dbf.setIgnoringElementContentWhitespace(true);
            dbf.setNamespaceAware(true);

            DocumentBuilder builder = dbf.newDocumentBuilder();
            document = builder.parse(new InputSource(new StringReader(xmlAsString)));
        } catch (Exception err) {
            err.printStackTrace();
        }

        return document;
    }

    /**
     * read input stream and produce a Document Object
     *
     * @param is
     * @return
     * @throws SAXException
     * @throws IOException
     * @throws ParserConfigurationException
     */
    public static Document createXmlDocument(
        InputStream is) throws SAXException, IOException, ParserConfigurationException {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        dbf.setValidating(false);
        dbf.setIgnoringComments(false);
        dbf.setIgnoringElementContentWhitespace(true);
        dbf.setNamespaceAware(true);

        DocumentBuilder db = null;
        db = dbf.newDocumentBuilder();
        db.setEntityResolver(new NullResolver());

        return db.parse(is);
    }

    /**
     * convert an xml document to a string
     *
     * @param node
     *            document node
     * @return a string representation of the xml document
     */
    public static String xmlToString(Node node) {

        return xmlToString(node, "UTF-8");
    }

    /**
     * convert an xml document to a string
     *
     * @param node
     *            document node
     * @return a string representation of the xml document
     */
    public static String xmlToString(Node node, String encoding) {

        String retXmlAsString = "";

        try {
            Source source = new DOMSource(node);
            StringWriter stringWriter = new StringWriter();
            Result result = new StreamResult(stringWriter);
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, encoding);

            transformer.transform(source, result);
            retXmlAsString = stringWriter.toString();

            // for some reason encoding is not handling entity references - need
            // to look in to the further
            retXmlAsString = retXmlAsString.replace("&nbsp;", "&#160;");

        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }

        return retXmlAsString;
    }

    /**
     * write string to console but ensure encoding is UTF-8
     *
     * @param value
     *            to write
     */
    public static void dumpStringToConsole(String value) {
        try {
            java.io.PrintStream outx = new java.io.PrintStream(System.out, true, "UTF-8");
            outx.println(value);
        } catch (Exception e) {
            System.out.println("error dumping string to console" + e);
        }
    }

    private static class NullResolver implements EntityResolver {

        public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {

            return new InputSource(new StringReader(""));
        }
    }

}
