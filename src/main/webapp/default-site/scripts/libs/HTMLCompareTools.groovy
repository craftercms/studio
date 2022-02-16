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

package scripts.libs

import org.apache.commons.io.IOUtils
import org.outerj.daisy.diff.DaisyDiff
import org.springframework.core.io.ClassPathResource

import javax.xml.transform.sax.SAXResult
import javax.xml.transform.Transformer
import javax.xml.transform.stream.StreamSource
import javax.xml.transform.TransformerFactory
import javax.xml.transform.sax.SAXTransformerFactory
import javax.xml.transform.sax.TransformerHandler
import javax.xml.transform.stream.StreamResult
import javax.xml.XMLConstants

import org.xml.sax.InputSource
import org.apache.commons.text.StringEscapeUtils

import static java.nio.charset.StandardCharsets.UTF_8


class HTMLCompareTools {

	static DEFAULT_FACTORY_CLASS = "com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl"

	static CONTENT_XML_TO_HTML_XSL = new ClassPathResource("crafter/studio/utils/xml-to-html.xslt")

	static String xmlAsStringToHtml(String xml) {
		return xmlToHtml(IOUtils.toInputStream(xml, UTF_8))
	}

	static String xmlToHtml(InputStream xml) {
		try (def template = CONTENT_XML_TO_HTML_XSL.getInputStream()) {
			SAXTransformerFactory tf = TransformerFactory.newInstance(DEFAULT_FACTORY_CLASS, null)
			tf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true)
			StreamSource xslSource = new StreamSource(template)
			Transformer transformer = tf.newTransformer(xslSource)
			StreamSource xmlSource = new StreamSource(xml)

			def handler = tf.newTransformerHandler()
			StringWriter resultWriter = new StringWriter()
			handler.setResult(new StreamResult(resultWriter))
			SAXResult result = new SAXResult(handler)

			transformer.transform(xmlSource, result)

			return resultWriter.toString()
		}
		catch (Throwable e) {
			throw new RuntimeException(e)
		}
	}

	static String xmlEscapedFormatted(String xml){
		def formattedXml = ''
		def spacesCount = 0

		for(String s : xml.split("(?=<)|(?<=>)")){
			def spaces = ''
			def i = 0

			for(i; i < spacesCount; i++){
				spaces += '&nbsp;'
			}

			if(s.trim().length() > 0){
				formattedXml += spaces + StringEscapeUtils.escapeXml(s) +  '<br/>'
			}else{
				spacesCount = s.length()
			}
		}

		return formattedXml
	}

	static String diff(String html1, String html2) {
		return diff(IOUtils.toInputStream(html1, UTF_8), IOUtils.toInputStream(html2, UTF_8))
	}

	static String diff(InputStream html1, InputStream html2) {
		try {
			SAXTransformerFactory tf =  TransformerFactory.newInstance(DEFAULT_FACTORY_CLASS, null)
			tf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true)
			TransformerHandler result = tf.newTransformerHandler()
			StringWriter resultWriter = new StringWriter()
			result.setResult(new StreamResult(resultWriter))

			Locale locale = Locale.getDefault()
			String prefix = "diff"

			InputSource oldSource = new InputSource(html1)
			InputSource newSource = new InputSource(html2)

			DaisyDiff.diffHTML(oldSource, newSource, result, prefix, locale)
			return resultWriter.toString()
		}
		catch (Throwable e) {
			throw new RuntimeException(e)
		}
	}
}
