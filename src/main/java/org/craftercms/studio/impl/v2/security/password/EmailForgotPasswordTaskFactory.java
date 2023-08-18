/*
 * Copyright (C) 2007-2023 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.impl.v2.security.password;

import freemarker.template.Template;
import org.craftercms.commons.http.RequestContext;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v2.dal.User;
import org.craftercms.studio.api.v2.service.security.UserService;
import org.craftercms.studio.api.v2.service.security.internal.UserServiceInternal;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.lang.NonNull;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;

import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import java.beans.ConstructorProperties;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.craftercms.studio.api.v2.utils.StudioConfiguration.*;

/**
 * {@link ForgotPasswordTaskFactory} implementation that creates a task to send
 * the user a password reset link
 *
 * @since 4.1.2
 */
public class EmailForgotPasswordTaskFactory implements ForgotPasswordTaskFactory, InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(EmailForgotPasswordTaskFactory.class);

    private static final String AUTHORING_URL_MODEL_KEY = "authoringUrl";
    private static final String SERVICE_URL_MODEL_KEY = "serviceUrl";
    private static final String TOKEN_MODEL_KEY = "token";
    private final UserServiceInternal userServiceInternal;
    private final StudioConfiguration studioConfiguration;
    private final JavaMailSender emailService;
    private final JavaMailSender emailServiceNoAuth;
    private final UserService userService;
    private final ObjectFactory<FreeMarkerConfig> freeMarkerConfig;
    private Template template;
    private String authoringUrl;

    @ConstructorProperties({"userService", "userServiceInternal",
            "studioConfiguration", "freeMarkerConfig",
            "emailService", "emailServiceNoAuth"})
    public EmailForgotPasswordTaskFactory(final UserService userService, final UserServiceInternal userServiceInternal,
                                          final StudioConfiguration studioConfiguration, final ObjectFactory<FreeMarkerConfig> freeMarkerConfig,
                                          final JavaMailSender emailService, final JavaMailSender emailServiceNoAuth) {
        this.userService = userService;
        this.userServiceInternal = userServiceInternal;
        this.studioConfiguration = studioConfiguration;
        this.emailService = emailService;
        this.emailServiceNoAuth = emailServiceNoAuth;
        this.freeMarkerConfig = freeMarkerConfig;
    }

    @Override
    public void afterPropertiesSet() throws IOException {
        // Retrieving the template here allows the template loader to have access to the right context, so it can find the template
        this.template = freeMarkerConfig.getObject().getConfiguration().getTemplate(studioConfiguration.getProperty(SECURITY_FORGOT_PASSWORD_EMAIL_TEMPLATE));
        RequestContext context = RequestContext.getCurrent();
        HttpServletRequest request = context.getRequest();
        // TODO: JM: This needs to be fixed so the authoring URL does not come from the user request
        authoringUrl = request.getRequestURL().toString().replace(request.getPathInfo(), "");
    }

    @NonNull
    @Override
    public Runnable prepareTask(@NonNull final String username) {
        return () -> forgotPassword(username);
    }

    private void forgotPassword(String username) {
        logger.debug("Get the user profile for username '{}'", username);
        User user;
        try {
            user = userServiceInternal.getUserByIdOrUsername(-1, username);
        } catch (UserNotFoundException e) {
            logger.error("Unable to send forgot password email because user '{}' does not exist.", username, e);
            return;
        } catch (ServiceLayerException e) {
            logger.error("Unable to send forgot password email. Failed to load user with username '{}'.", username, e);
            return;
        }
        if (user.isExternallyManaged()) {
            logger.error("Unable to send forgot password email because user '{}' is externally managed", username);
            return;
        }
        if (user.getEmail() == null) {
            logger.info("Failed to send forgot password email because user '{}' does not have an email address in the system", username);
            return;
        }

        String email = user.getEmail();

        logger.debug("Create a forgot password security token for username '{}'", username);
        String encryptedToken = userService.getForgotPasswordToken(username);
        logger.debug("Send username '{}' the forgot password email to '{}'", username, email);
        sendEmail(email, encryptedToken);
    }

    private void sendEmail(String emailAddress, String token) {
        try {
            Map<String, Object> model = new HashMap<>();
            String serviceUrl = studioConfiguration.getProperty(SECURITY_RESET_PASSWORD_SERVICE_URL);
            model.put(AUTHORING_URL_MODEL_KEY, authoringUrl);
            model.put(SERVICE_URL_MODEL_KEY, serviceUrl);
            model.put(TOKEN_MODEL_KEY, token);
            String messageBody = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);

            MimeMessage mimeMessage = emailService.createMimeMessage();
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);

            messageHelper.setFrom(studioConfiguration.getProperty(MAIL_FROM_DEFAULT));
            messageHelper.setTo(emailAddress);
            messageHelper.setSubject(studioConfiguration.getProperty(SECURITY_FORGOT_PASSWORD_MESSAGE_SUBJECT));
            messageHelper.setText(messageBody, true);
            logger.info("Send the password recovery email to '{}'", emailAddress);
            if (isAuthenticatedSMTP()) {
                emailService.send(mimeMessage);
            } else {
                emailServiceNoAuth.send(mimeMessage);
            }
            logger.info("Successfully sent the password recovery email to '{}'", emailAddress);
        } catch (Exception e) {
            logger.error("Failed to send the password recovery email to '{}'", emailAddress, e);
        }
    }

    private boolean isAuthenticatedSMTP() {
        return Boolean.parseBoolean(studioConfiguration.getProperty(MAIL_SMTP_AUTH));
    }

}
