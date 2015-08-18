/*
 * Crafter Studio Web-content authoring solution
 * Copyright (C) 2007-2015 Crafter Software Corporation.
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

import scripts.api.ContentServices;
import java.util.*
import org.apache.commons.io.IOUtils;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.Locale;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.outerj.daisy.diff.DaisyDiff;
import org.outerj.daisy.diff.HtmlCleaner;
import org.outerj.daisy.diff.html.HTMLDiffer;
import org.outerj.daisy.diff.html.HtmlSaxDiffOutput;
import org.outerj.daisy.diff.html.TextNodeComparator;
import org.outerj.daisy.diff.html.dom.DomTreeBuilder;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.AttributesImpl;

def result = [:]
def site = params.site
def path = params.path
def version = params.version

def context = ContentServices.createContext(applicationContext, request)
def original = ContentServices.getContent(site, path, false, context)
def revised = ContentServices.getContentVersionAtPath(site, path, version, context)

class HTMLCompareTools {
	static String diff(InputStream html1, InputStream html2) {
		try {
			SAXTransformerFactory tf = (SAXTransformerFactory) TransformerFactory.newInstance();

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

			result.startDocument();
			result.startElement("", "html", "html", new AttributesImpl());
			result.startElement("", "head", "head", new AttributesImpl());

			AttributesImpl cssLinkAttrs = new AttributesImpl();
			cssLinkAttrs.addAttribute("", "href", "href", "", "css/diff.css");
			cssLinkAttrs.addAttribute("", "type", "type", "", "text/css");
			cssLinkAttrs.addAttribute("", "rel", "rel", "", "stylesheet");
			result.startElement("", "link", "link", cssLinkAttrs);

			result.endElement("", "head", "head");
			result.startElement("", "body", "body", new AttributesImpl());
			HtmlSaxDiffOutput output = new HtmlSaxDiffOutput(result, prefix);

			HTMLDiffer differ = new HTMLDiffer(output);
			differ.diff(leftComparator, rightComparator);

			result.endElement("", "body", "body");
			result.endElement("", "html", "html");
			result.endDocument();
			return resultWriter.toString();
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}
}
def patch = [:]
//patch.original = original
//patch.revised = revised
patch.result = HTMLCompareTools.diff(IOUtils.toInputStream(original), IOUtils.toInputStream(revised))

return patch