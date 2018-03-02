package org.craftercms.studio.impl.v1.service.aws;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileHandler;
import org.craftercms.studio.api.v1.exception.AwsConfigurationException;
import org.craftercms.studio.api.v1.service.aws.AwsProfileManager;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.impl.v1.util.ConfigUtils;
import org.springframework.beans.factory.annotation.Required;

/**
 * Default implementation of {@link AwsProfileManager} that reads a single XML file from the repository.\
 *
 * @author joseross
 */
public class AwsProfileManagerImpl implements AwsProfileManager {

    /**
     * The path where the XML file is stored.
     */
    protected String basePath;

    /**
     * The name of the XML file.
     */
    protected String fileName;

    /**
     * Crafter Studio content service.
     */
    protected ContentService contentService;

    @Required
    public void setBasePath(final String basePath) {
        this.basePath = basePath;
    }

    @Required
    public void setFileName(final String fileName) {
        this.fileName = fileName;
    }

    @Required
    public void setContentService(final ContentService contentService) {
        this.contentService = contentService;
    }

    protected HierarchicalConfiguration getConfiguration(InputStream input) throws Exception {
        try {
            return ConfigUtils.readXmlConfiguration(input);
        } catch (ConfigurationException e) {
            throw new Exception("Unable to read the AWS configuration", e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public HierarchicalConfiguration getProfile(String site, String profileId) throws AwsConfigurationException {
        try {
            InputStream content = contentService.getContent(site, basePath + "/" + fileName);
            HierarchicalConfiguration config = getConfiguration(content);
            List<HierarchicalConfiguration> profiles = config.configurationsAt("profile");
            Optional<HierarchicalConfiguration> profile =
                profiles.stream()
                            .filter(profileItem -> profileId.equals(profileItem.getString("id")))
                            .findFirst();
            return profile.orElseThrow(() -> new AwsConfigurationException("Profile not found: " + profileId));
        } catch (Exception e) {
            throw new AwsConfigurationException("Unable to retrieve profile", e);
        }
    }

}
