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
package org.craftercms.studio.config;

import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_CORS_ALLOWED_ORIGINS;

/**
 * Spring Websocket Configuration
 *
 * @implNote This configuration class is required because the XML namespace doesn't allow customizations
 *
 * @author joseross
 * @since 4.0.0
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebsocketConfig implements WebSocketMessageBrokerConfigurer {

    protected StudioConfiguration studioConfiguration;

    @Autowired
    public WebsocketConfig(StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry
            // The STOMP controller URL
            .addEndpoint("/events")
            // Use the same allowed origins from the configuration to match the HTTP filter
            .setAllowedOriginPatterns(studioConfiguration.getArray(CONFIGURATION_CORS_ALLOWED_ORIGINS, String.class));
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry
            // The accepted prefixes for sending messages to the app (not used for now)
            .setApplicationDestinationPrefixes("/app")
            // The accepted prefixes for sending messages to the clients
            .enableSimpleBroker("/topic");
    }

}
