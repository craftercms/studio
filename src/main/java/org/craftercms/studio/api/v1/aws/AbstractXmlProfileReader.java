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

package org.craftercms.studio.api.v1.aws;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v1.exception.AwsConfigurationException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;

/**
 * Class with common methods for XML {@link AwsProfileReader} implementations.
 *
 * @param <T> The type of {@link AwsProfile} that will be parsed.
 *
 * @author joseross
 */
public abstract class AbstractXmlProfileReader<T extends AwsProfile> implements AwsProfileReader<T> {

    public static final String CONFIG_KEY_REGION = "region";
    public static final String CONFIG_KEY_ACCESS_KEY = "credentials.accessKey";
    public static final String CONFIG_KEY_SECRET_KEY = "credentials.secretKey";

    @SuppressWarnings("unchecked")
    protected List<HierarchicalConfiguration> getRequiredConfigurationsAt(HierarchicalConfiguration config,
                                                                          String key) throws AwsConfigurationException {
        List<HierarchicalConfiguration> configs = config.configurationsAt(key);
        if (CollectionUtils.isEmpty(configs)) {
            throw new AwsConfigurationException("Missing required property '" + key + "'");
        } else {
            return configs;
        }
    }

    protected String getRequiredStringProperty(Configuration config, String key) throws AwsConfigurationException {
        String property = config.getString(key);
        if (StringUtils.isEmpty(property)) {
            throw new AwsConfigurationException("Missing required property '" + key + "'");
        } else {
            return property;
        }
    }

    protected void readBasicProperties(Configuration config, T profile) throws AwsConfigurationException {
        if(config.containsKey(CONFIG_KEY_ACCESS_KEY) && config.containsKey(CONFIG_KEY_SECRET_KEY)) {
            String accessKey = getRequiredStringProperty(config, CONFIG_KEY_ACCESS_KEY);
            String secretKey = getRequiredStringProperty(config, CONFIG_KEY_SECRET_KEY);
            profile.setCredentialsProvider(
                new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey)));
        } else {
            profile.setCredentialsProvider(new DefaultAWSCredentialsProviderChain());
        }
        if(config.containsKey(CONFIG_KEY_REGION)) {
            profile.setRegion(getRequiredStringProperty(config, CONFIG_KEY_REGION));
        }
    }

}
