package org.craftercms.studio.api.v1.service.aws;

import org.craftercms.studio.api.v1.aws.AwsProfile;
import org.craftercms.studio.api.v1.aws.AwsProfileReader;
import org.craftercms.studio.api.v1.exception.AwsConfigurationException;
import org.springframework.beans.factory.annotation.Required;

/**
 * Provides common profile operations used by all services.
 * @param <T> The type of {@link AwsProfile} that will be used.
 *
 * @author joseross
 */
public abstract class AbstractAwsService<T extends AwsProfile> {

    /**
     * Used to read profiles from the configuration file.
     */
    private AwsProfileManager profileManager;

    /**
     * Used to generate {@link AwsProfile} instances.
     */
    private AwsProfileReader<T> profileReader;

    @Required
    public void setProfileManager(final AwsProfileManager profileManager) {
        this.profileManager = profileManager;
    }

    @Required
    public void setProfileReader(AwsProfileReader<T> profileReader) {
        this.profileReader = profileReader;
    }

    protected T getProfile(String site, String profileId) throws AwsConfigurationException {
        return profileReader.readProfile(profileManager.getProfile(site, profileId));
    }

}
