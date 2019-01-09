/*
 * Copyright (C) 2007-2019 Crafter Software Corporation. All Rights Reserved.
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
