package org.craftercms.studio.api.v1.service.aws;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.craftercms.studio.api.v1.exception.AwsConfigurationException;

public interface AwsProfileManager {

    HierarchicalConfiguration getProfile(String site, String profileId) throws AwsConfigurationException;

}
