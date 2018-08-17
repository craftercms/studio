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
 * &lt;profile&gt;
 *   &lt;id&gt;xxxxx&lt;/id&gt;
 *   &lt;credentials&gt;
 *     &lt;accessKey&gt;XXXXXXXXXXXXXXXXXXXX&lt;/accessKey&gt;
 *     &lt;secretKey&gt;XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX&lt;/secretKey&gt;
 *   &lt;/credentials&gt;
 *   &lt;region&gt;us-east-1&lt;/region&gt;
 *   &lt;bucketName&gt;00000000000000000000&lt;/bucketName&gt;
 * &lt;/profile&gt;
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
