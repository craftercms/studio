package org.craftercms.studio.impl.v1.aws.s3;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.craftercms.studio.api.v1.aws.AbstractXmlProfileReader;
import org.craftercms.studio.api.v1.aws.s3.S3Profile;
import org.craftercms.studio.api.v1.exception.AwsConfigurationException;

/**
 * S3 implementation of {@link org.craftercms.studio.api.v1.aws.AwsProfileReader}. It uses Apache Commons Configuration
 * to read an XML S3 profile like the following:
 *
 * <pre>
 * &gt;profile&lt;
 *   &gt;id&lt;xxxxx&gt;/id&lt;
 *   &gt;credentials&lt;
 *     &gt;accessKey&lt;XXXXXXXXXXXXXXXXXXXX&gt;/accessKey&lt;
 *     &gt;secretKey&lt;XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX&gt;/secretKey&lt;
 *   &gt;/credentials&lt;
 *   &gt;region&lt;us-east-1&gt;/region&lt;
 *   &gt;bucketName&lt;00000000000000000000&gt;/bucketName&lt;
 * &gt;/profile&lt;
 * </pre>
 *
 */
public class XmlS3ProfileReader extends AbstractXmlProfileReader<S3Profile> {

    @Override
    public S3Profile readProfile(final HierarchicalConfiguration config) throws AwsConfigurationException {
        S3Profile profile = new S3Profile();
        readBasicProperties(config, profile);
        profile.setBucketName(getRequiredStringProperty(config,"bucketName"));
        return profile;
    }

}
