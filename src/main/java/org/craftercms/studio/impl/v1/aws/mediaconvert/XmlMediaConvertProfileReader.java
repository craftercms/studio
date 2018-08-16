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

package org.craftercms.studio.impl.v1.aws.mediaconvert;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.craftercms.studio.api.v1.aws.AbstractXmlProfileReader;
import org.craftercms.studio.api.v1.aws.mediaconvert.MediaConvertProfile;
import org.craftercms.studio.api.v1.exception.AwsConfigurationException;

/**
 * MediaConvert implementation of {@link org.craftercms.studio.api.v1.aws.AwsProfileReader}. It uses Apache Commons
 * Configuration to read an XML profile like the following properties:
 *
 * <pre>
 * &gt;profile&lt;
 *   &gt;id&lt;xxxxx&gt;/id&lt;
 *   &gt;credentials&lt;
 *     &gt;accessKey&lt;XXXXXXXXXXXXXXXXXXXX&gt;/accessKey&lt;
 *     &gt;secretKey&lt;XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX&gt;/secretKey&lt;
 *   &gt;/credentials&lt;
 *   &gt;region&lt;us-east-1&gt;/region&lt;
 *   &gt;endpoint&lt;https://XXXXXXXX.mediaconvert.us-east-1.amazonaws.com&gt;/endpoint&lt;
 *   &gt;role&lt;arn:aws:iam::XXXXXXXXXXXX:role/...&gt;/role&lt;
 *   &gt;queue&lt;arn:aws:mediaconvert:us-east-1:XXXXXXXXXXXX:queues/Default&gt;/queue&lt;
 *   &gt;inputPath&lt;example-bucket/folder/videos/...&gt;/inputPath&lt;
 *   &gt;template&lt;Example Template&gt;/template&lt;
 * &gt;/profile&lt;
 * </pre>
 *
 * @author joseross
 */
@SuppressWarnings("unchecked")
public class XmlMediaConvertProfileReader extends AbstractXmlProfileReader<MediaConvertProfile> {

    public static final String CONFIG_KEY_ENDPOINT = "endpoint";
    public static final String CONFIG_KEY_ROLE = "role";
    public static final String CONFIG_KEY_QUEUE = "queue";
    public static final String CONFIG_KEY_TEMPLATE = "template";
    public static final String CONFIG_KEY_INPUT_PATH = "inputPath";

    /**
     * {@inheritDoc}
     */
    @Override
    public MediaConvertProfile readProfile(final HierarchicalConfiguration config) throws AwsConfigurationException {
        MediaConvertProfile profile = new MediaConvertProfile();
        readBasicProperties(config, profile);

        profile.setEndpoint(config.getString(CONFIG_KEY_ENDPOINT));
        profile.setRole(config.getString(CONFIG_KEY_ROLE));
        profile.setQueue(config.getString(CONFIG_KEY_QUEUE));
        profile.setTemplate(config.getString(CONFIG_KEY_TEMPLATE));

        profile.setInputPath(config.getString(CONFIG_KEY_INPUT_PATH));
        return profile;
    }

}
