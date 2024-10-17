/*
 * Copyright (C) 2007-2024 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.config;

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.craftercms.commons.rest.RestTemplate;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import java.util.Map;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.DEPLOYER_RESPONSE_TIMEOUT;

/**
 * Configurations for deployer related beans
 */
@Configuration
public class DeployerConfig {

    public static final int DEFAULT_DEPLOYER_RESPONSE_TIMEOUT = 300;

    @Bean
    @SuppressWarnings("unused")
    public RestTemplate deployerRestTemplate(final StudioConfiguration studioConfiguration) {
        int responseTimeoutSeconds = studioConfiguration.getProperty(DEPLOYER_RESPONSE_TIMEOUT,
                Integer.class, DEFAULT_DEPLOYER_RESPONSE_TIMEOUT);
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        CloseableHttpClient client = HttpClientBuilder.create()
                .useSystemProperties()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setResponseTimeout(responseTimeoutSeconds, SECONDS).build())
                .build();
        requestFactory.setHttpClient(client);

        RestTemplate restTemplate = new RestTemplate(Map.class);
        restTemplate.setRequestFactory(requestFactory);
        return restTemplate;
    }
}
