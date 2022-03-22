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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.security.access.expression.SecurityExpressionHandler;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;

/**
 * Spring Security Websocket Configuration
 *
 * @implNote This configuration class is required because the XML namespace doesn't allow customizations
 *
 * @author joseross
 * @since 4.0.0
 */
@Configuration
public class WebsocketSecurityConfig extends AbstractSecurityWebSocketMessageBrokerConfigurer {

    protected SecurityExpressionHandler<Message<Object>> expressionHandler;

    @Autowired
    public WebsocketSecurityConfig(SecurityExpressionHandler<Message<Object>> expressionHandler) {
        this.expressionHandler = expressionHandler;
    }

    @Override
    protected void configureInbound(MessageSecurityMetadataSourceRegistry messages) {
        messages
            // Add support for Studio's expressions
            .expressionHandler(expressionHandler)

            // Require authentication for CONNECT messages
            .nullDestMatcher().authenticated()
            // Only allow users to subscribe if they are system admins
            .simpSubscribeDestMatchers("/topic/studio").access("isSystemAdmin()")
            // Only allow users to subscribe if they are site members
            .simpSubscribeDestMatchers("/topic/studio/{siteId}").access("isSiteMember(#siteId)")
            // Reject any other incoming message from users
            .anyMessage().denyAll();
    }

}
