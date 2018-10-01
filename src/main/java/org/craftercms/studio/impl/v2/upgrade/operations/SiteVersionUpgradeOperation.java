package org.craftercms.studio.impl.v2.upgrade.operations;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.configuration2.Configuration;
import org.craftercms.studio.api.v2.exception.UpgradeException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class SiteVersionUpgradeOperation extends XsltFileUpgradeOperation {

    protected Resource defaultFile;

    @Override
    public void init(final String version, final Configuration config) {
        super.init(version, config);
        defaultFile = new ClassPathResource(config.getString("defaultFile"));
    }

    @Override
    public void execute(final String site) throws UpgradeException {
        if(contentRepository.contentExists(site, path)) {
            super.execute(site);
        } else {
            try(InputStream is = defaultFile.getInputStream()) {
                writeToRepo(site, path, is, "Added version file for future upgrades");
            } catch (IOException e) {
                throw new UpgradeException("Error adding version file to site " + site, e);
            }
        }
    }

}
