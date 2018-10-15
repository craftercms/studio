/*
 * Copyright (C) 2007-2018 Crafter Software Corporation. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

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
 *   &lt;distributionDomain&gt;00000000000000000000&lt;/distributionDomain&gt;
 * &lt;/profile&gt;
 * </pre>
 *
 */
public class XmlS3ProfileReader extends AbstractXmlProfileReader<S3Profile> {

    public static final String DEFAULT_DOMAIN = "https://s3.amazonaws.com";

    public static final String CONFIG_KEY_BUCKET = "bucketName";
    public static final String CONFIG_KEY_DOMAIN = "distributionDomain";

    @Override
    public S3Profile readProfile(final HierarchicalConfiguration config) throws AwsConfigurationException {
        S3Profile profile = new S3Profile();
        readBasicProperties(config, profile);
        profile.setBucketName(getRequiredStringProperty(config,CONFIG_KEY_BUCKET));
        profile.setDistributionDomain(config.getString(CONFIG_KEY_DOMAIN, DEFAULT_DOMAIN));
        return profile;
    }

}
