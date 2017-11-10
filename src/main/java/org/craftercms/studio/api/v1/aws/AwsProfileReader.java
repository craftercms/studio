package org.craftercms.studio.api.v1.aws;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.craftercms.studio.api.v1.exception.AwsConfigurationException;

/**
 * Implementations of this interface should be able to generate a subclass of {@link AwsProfile} from the
 * given configuration.
 *
 * @author avasquez
 */
public interface AwsProfileReader<T extends AwsProfile> {

    T readProfile(HierarchicalConfiguration config) throws AwsConfigurationException;

}
