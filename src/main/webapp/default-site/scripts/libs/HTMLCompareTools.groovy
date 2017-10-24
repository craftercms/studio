package scripts.libs
import java.util.*
import org.apache.commons.io.IOUtils
import java.io.InputStream
import java.io.StringWriter
import java.net.URISyntaxException
import java.util.Locale

import javax.xml.transform.sax.SAXResult
import javax.xml.transform.Transformer
import javax.xml.transform.stream.StreamSource
import javax.xml.transform.TransformerFactory
import javax.xml.transform.sax.SAXTransformerFactory
import javax.xml.transform.sax.TransformerHandler
import javax.xml.transform.stream.StreamResult

import org.outerj.daisy.diff.DaisyDiff
import org.outerj.daisy.diff.HtmlCleaner
import org.outerj.daisy.diff.html.HTMLDiffer
import org.outerj.daisy.diff.html.HtmlSaxDiffOutput
import org.outerj.daisy.diff.html.TextNodeComparator
import org.outerj.daisy.diff.html.dom.DomTreeBuilder
import org.xml.sax.InputSource
import org.xml.sax.helpers.AttributesImpl
import javax.xml.XMLConstants;

class HTMLCompareTools {
		static CONTENT_XML_TO_HTML_XSL = 
			"<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" xmlns:fo=\"http://www.w3.org/1999/XSL/Format\">"+
  				"<xsl:template match=\"//\">" +
  				    "<html><body><table>"+
	      				"<xsl:apply-templates/>" +
	      			"</table></body></html>" +
  				"</xsl:template>" +
  				"<xsl:template match='*'>" +
					"<xsl:for-each select='./*'>" +
			     	"<tr>" +
			    		"<td style='font-weight:bold;'>" +
			    			"<xsl:attribute name='data-var'><xsl:value-of select='local-name()'/></xsl:attribute>"+
				      		"<xsl:value-of select='local-name()'/>" +
			      		"</td>" +				        
				        "<td><xsl:value-of select='.'></xsl:value-of></td>" +
				       "</tr>" +
				  "</xsl:for-each>" +
  				"</xsl:template>" +
			"</xsl:stylesheet>"

	static String xmlAsStringToHtml(String xml) {
		return xmlToHtml(IOUtils.toInputStream(xml))
	}

	static String xmlToHtml(InputStream xml) {
		try {
			SAXTransformerFactory tf = TransformerFactory.newInstance()
			tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
			tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
			StreamSource xslSource = new StreamSource(IOUtils.toInputStream(HTMLCompareTools.CONTENT_XML_TO_HTML_XSL))
			Transformer transformer = tf.newTransformer(xslSource)
			StreamSource xmlSource = new StreamSource(xml)

			def handler = tf.newTransformerHandler()
			StringWriter resultWriter = new StringWriter()
			handler.setResult(new StreamResult(resultWriter))
			SAXResult result = new SAXResult(handler)

			transformer.transform(xmlSource, result)

			return resultWriter.toString();
		} 
		catch (Throwable e) {
			throw new RuntimeException(e)
		}
	}

	static String diff(String html1, String html2) {
		return diff(IOUtils.toInputStream(html1), IOUtils.toInputStream(html2))
	}

	static String diff(InputStream html1, InputStream html2) {
		try {
			SAXTransformerFactory tf = (SAXTransformerFactory) TransformerFactory.newInstance();
			tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
			tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
			TransformerHandler result = tf.newTransformerHandler();
			StringWriter resultWriter = new StringWriter();
			result.setResult(new StreamResult(resultWriter));

			Locale locale = Locale.getDefault();
			String prefix = "diff";

			HtmlCleaner cleaner = new HtmlCleaner();

			InputSource oldSource = new InputSource(html1);
			InputSource newSource = new InputSource(html2);

			DomTreeBuilder oldHandler = new DomTreeBuilder();
			cleaner.cleanAndParse(oldSource, oldHandler);
			TextNodeComparator leftComparator = new TextNodeComparator(oldHandler, locale);

			DomTreeBuilder newHandler = new DomTreeBuilder();
			cleaner.cleanAndParse(newSource, newHandler);
			TextNodeComparator rightComparator = new TextNodeComparator(newHandler, locale);

			HtmlSaxDiffOutput output = new HtmlSaxDiffOutput(result, prefix);

			HTMLDiffer differ = new HTMLDiffer(output);
			differ.diff(leftComparator, rightComparator);
			return resultWriter.toString();
		} 
		catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}
}
