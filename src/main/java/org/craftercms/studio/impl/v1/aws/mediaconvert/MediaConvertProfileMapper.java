/*
 * Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.craftercms.studio.impl.v1.aws.mediaconvert;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.craftercms.commons.config.ConfigurationException;
import org.craftercms.commons.config.ConfigurationResolver;
import org.craftercms.commons.config.profiles.aws.AbstractAwsProfile;
import org.craftercms.commons.config.profiles.aws.AbstractAwsProfileMapper;
import org.craftercms.studio.api.v1.aws.mediaconvert.MediaConvertProfile;

import java.beans.ConstructorProperties;

import static org.craftercms.commons.config.ConfigUtils.*;

/**
 * MediaConvert implementation of {@link org.craftercms.commons.config.ConfigurationMapper}. It uses Apache Commons
 * Configuration to read an XML profile like the following properties:
 *
 * <pre>
 * &lt;profile&gt;
 *   &lt;id&gt;xxxxx&lt;/id&gt;
 *   &lt;credentials&gt;
 *     &lt;accessKey&gt;XXXXXXXXXXXXXXXXXXXX&lt;/accessKey&gt;
 *     &lt;secretKey&gt;XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX&lt;/secretKey&gt;
 *   &lt;/credentials&gt;
 *   &lt;region&gt;us-east-1&lt;/region&gt;
 *   &lt;endpoint&gt;https://XXXXXXXX.mediaconvert.us-east-1.amazonaws.com&lt;/endpoint&gt;
 *   &lt;role&gt;arn:aws:iam::XXXXXXXXXXXX:role/...&lt;/role&gt;
 *   &lt;queue&gt;arn:aws:mediaconvert:us-east-1:XXXXXXXXXXXX:queues/Default&lt;/queue&gt;
 *   &lt;inputPath&gt;example-bucket/folder/videos/...&lt;/inputPath&gt;
 *   &lt;template&gt;Example Template&lt;/template&gt;
 * &lt;/profile&gt;
 * </pre>
 *
 * @author joseross
 */
public class MediaConvertProfileMapper extends AbstractAwsProfileMapper<MediaConvertProfile> {

    public static final String CONFIG_KEY_MEDIACONVERT = "mediaConvert";
    public static final String CONFIG_KEY_ENDPOINT = "endpoint";
    public static final String CONFIG_KEY_ROLE = "role";
    public static final String CONFIG_KEY_QUEUE = "queue";
    public static final String CONFIG_KEY_TEMPLATE = "template";
    public static final String CONFIG_KEY_INPUT_PATH = "inputPath";

    @ConstructorProperties({"resolver"})
    public MediaConvertProfileMapper(final ConfigurationResolver resolver) {
        super(CONFIG_KEY_MEDIACONVERT, resolver);
    }

    @Override
    protected MediaConvertProfile mapProfile(HierarchicalConfiguration<ImmutableNode> profileConfig)
            throws ConfigurationException {
        MediaConvertProfile profile = super.mapProfile(profileConfig);

        // For MediaConvert the endpoint is required
        profile.setEndpoint(getRequiredStringProperty(profileConfig, CONFIG_KEY_ENDPOINT));

        profile.setRole(getRequiredStringProperty(profileConfig, CONFIG_KEY_ROLE));
        profile.setQueue(getRequiredStringProperty(profileConfig, CONFIG_KEY_QUEUE));
        profile.setTemplate(getRequiredStringProperty(profileConfig, CONFIG_KEY_TEMPLATE));
        profile.setInputPath(getRequiredStringProperty(profileConfig, CONFIG_KEY_INPUT_PATH));

        return profile;
    }

    @Override
    protected AbstractAwsProfile createProfile() {
        return new MediaConvertProfile();
    }

}
