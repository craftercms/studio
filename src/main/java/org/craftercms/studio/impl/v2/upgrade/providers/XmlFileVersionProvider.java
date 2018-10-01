package org.craftercms.studio.impl.v2.upgrade.providers;

import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.api.v2.exception.UpgradeException;
import org.craftercms.studio.api.v2.exception.UpgradeNotSupportedException;
import org.craftercms.studio.api.v2.upgrade.VersionProvider;
import org.springframework.beans.factory.annotation.Required;

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
        String currentSiteVersion = VERSION_3_0_0;
        if(!contentRepository.contentExists(site, "/config/studio")) {
            throw new UpgradeNotSupportedException("Site '"+ site +"' from 2.5 can't be automatically upgraded");
        } else if(contentRepository.contentExists(site, "/config/studio/studio_version.xml")) {
            // read the version from file ...
        }
        return currentSiteVersion;
    }

}
