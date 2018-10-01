package org.craftercms.studio.impl.v2.upgrade.providers;

import java.io.FileInputStream;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.api.v2.exception.UpgradeException;
import org.craftercms.studio.api.v2.exception.UpgradeNotSupportedException;
import org.craftercms.studio.api.v2.upgrade.VersionProvider;
import org.springframework.beans.factory.annotation.Required;
import org.w3c.dom.Document;

import static org.craftercms.studio.api.v2.upgrade.UpgradeConstants.VERSION_3_0_0;

public class XmlFileVersionProvider implements VersionProvider {

    protected String site;
    protected String path;

    protected ContentRepository contentRepository;

    public XmlFileVersionProvider(final String site, final String path) {
        this.site = site;
        this.path = path;
    }

    @Required
    public void setContentRepository(final ContentRepository contentRepository) {
        this.contentRepository = contentRepository;
    }

    @Override
    public String getCurrentVersion() throws UpgradeException {
        String currentVersion = VERSION_3_0_0;
        if(!contentRepository.contentExists(site, "/config/studio")) {
            throw new UpgradeNotSupportedException("Site '"+ site +"' from 2.5 can't be automatically upgraded");
        } else if(contentRepository.contentExists(site, path)) {
            try(InputStream is = contentRepository.getContent(site, path)) {
                DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = builderFactory.newDocumentBuilder();
                Document xmlDocument = builder.parse(is);
                XPath xPath = XPathFactory.newInstance().newXPath();
                String expression = "/*[1]/version/text()";
                String fileVersion = (String) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.STRING);
                if(StringUtils.isNotEmpty(fileVersion)) {
                    currentVersion = fileVersion;
                }
            } catch (Exception e) {
                throw new UpgradeException("Error reading version from file " + path + " in site " + site, e);
            }
        }
        return currentVersion;
    }

}
