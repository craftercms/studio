import scripts.libs.EnvironmentOverrides

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
def result = [:]
def site = params.site
def path = params.path
def version = params.version

def context = ContentServices.createContext(applicationContext, request)
def original = ContentServices.getContent(site, path, false, context)
def revised = ContentServices.getContentVersionAtPath(site, path, version, context)

model.variantA = original
model.variantB = revised
model.diff = HTMLCompareTools.diff(
	IOUtils.toInputStream("<html><body>"+original+"</body></html>"), 
	IOUtils.toInputStream("<html><body>"+revised+"</body></html>"))


model.envConfig = EnvironmentOverrides.getValuesForSite(applicationContext, request, response)  
model.cookieDomain = request.getServerName()     
