package org.craftercms.studio.impl.v2.upgrade.upgraders;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.configuration2.Configuration;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.api.v2.exception.UpgradeException;
import org.craftercms.studio.api.v2.upgrade.UpgradeContext;
import org.craftercms.studio.api.v2.upgrade.Upgrader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class XsltFileUpgrader implements Upgrader {

    private static final Logger logger = LoggerFactory.getLogger(XsltFileUpgrader.class);

    protected String source;
    protected Resource template;

    @Override
    public void init(final Configuration config) {
        source = config.getString("source");
        template = new ClassPathResource(config.getString("template"));
    }

    @Override
    public void execute(final UpgradeContext context) throws UpgradeException {
        ContentRepository contentRepository = context.getContentRepository();
        try(InputStream templateIs = template.getInputStream()) {
            Transformer transformer =
                TransformerFactory.newInstance("net.sf.saxon.TransformerFactoryImpl", null)
                    .newTransformer(new StreamSource(templateIs));
            for(String site : context.getSites()) {
                logger.info("Applying XSLT template {0} to file {1} for site {2}", template, source, site);
                if(contentRepository.contentExists(site, source)) {
                    try(InputStream sourceIs = contentRepository.getContent(site, source)) {
                        ByteArrayOutputStream os = new ByteArrayOutputStream();
                        transformer.setParameter("site_id", site);
                        transformer.transform(new StreamSource(sourceIs), new StreamResult(os));
                        context.writeToRepo(site, source, new ByteArrayInputStream(os.toByteArray()),
                            "Upgrade to v" + context.getTargetVersion());
                    }
                } else {
                    logger.warn("Source file {0} doesn't exist in site {1}", source, site);
                }
            }


        } catch (Exception e) {
            throw new UpgradeException("Error processing file", e);
        }
    }

}
