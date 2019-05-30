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

package org.craftercms.studio.impl.v2.service.notification;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapperBuilder;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.craftercms.commons.mail.EmailUtils;
import org.craftercms.commons.validation.annotations.param.ValidateParams;
import org.craftercms.commons.validation.annotations.param.ValidateStringParam;
import org.craftercms.engine.exception.ConfigurationException;
import org.craftercms.studio.api.v1.constant.StudioConstants;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.GeneralLockService;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v1.to.ContentItemTO;
import org.craftercms.studio.api.v1.to.EmailMessageQueueTo;
import org.craftercms.studio.api.v1.to.EmailMessageTO;
import org.craftercms.studio.api.v1.to.EmailMessageTemplateTO;
import org.craftercms.studio.api.v1.to.MessageTO;
import org.craftercms.studio.api.v1.to.NotificationConfigTO;
import org.craftercms.studio.api.v1.util.StudioConfiguration;
import org.craftercms.studio.api.v2.service.config.ConfigurationService;
import org.craftercms.studio.api.v2.service.notification.NotificationMessageType;
import org.craftercms.studio.api.v2.service.notification.NotificationService;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import static org.craftercms.studio.api.v1.constant.SecurityConstants.KEY_EMAIL;
import static org.craftercms.studio.api.v1.constant.StudioConstants.FILE_SEPARATOR;
import static org.craftercms.studio.api.v1.constant.StudioConstants.PATTERN_ENVIRONMENT;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.*;

public class NotificationServiceImpl implements NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private static final String NOTIFICATION_KEY_DEPLOYMENT_ERROR = "deploymentError";
    private static final String NOTIFICATION_KEY_CONTENT_APPROVED = "contentApproved";
    private static final String NOTIFICATION_KEY_SUBMITTED_FOR_REVIEW = "submittedForReview";
    private static final String NOTIFICATION_KEY_CONTENT_REJECTED = "contentRejected";

    protected Map<String, Map<String, NotificationConfigTO>> notificationConfiguration;
    protected ContentService contentService;
    protected EmailMessageQueueTo emailMessages;
    protected ServicesConfig servicesConfig;
    protected SiteService siteService;
    protected SecurityService securityService;
    private Configuration configuration;
    protected StudioConfiguration studioConfiguration;
    protected ConfigurationService configurationService;

    public NotificationServiceImpl() {
        notificationConfiguration = new HashMap<String, Map<String, NotificationConfigTO>>();
    }

    public void init() {
        configuration = new Configuration(Configuration.VERSION_2_3_23);
        configuration.setTimeZone(TimeZone.getTimeZone(getTemplateTimezone()));
        configuration.setObjectWrapper(new DefaultObjectWrapperBuilder(Configuration.VERSION_2_3_23).build());
    }

    @Override
    @ValidateParams
    public void notifyDeploymentError(@ValidateStringParam(name = "site") final String site, final Throwable throwable, final List<String>
        filesUnableToPublish, final Locale locale) {
        try {
            final NotificationConfigTO notificationConfig = getNotificationConfig(site, locale);
            final Map<String, Object> templateModel = new HashMap<>();
            templateModel.put("deploymentError", throwable);
            templateModel.put("files", convertPathsToContent(site, filesUnableToPublish));
            notify(site, notificationConfig.getDeploymentFailureNotifications(), NOTIFICATION_KEY_DEPLOYMENT_ERROR,
                locale, templateModel);
        } catch (Throwable ex) {
            logger.error("Unable to Notify Error", ex);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    @ValidateParams
    public void notifyDeploymentError(@ValidateStringParam(name = "name") final String name, final Throwable throwable) {
        notifyDeploymentError(name, throwable, Collections.EMPTY_LIST, Locale.ENGLISH);
    }

    @Override
    @ValidateParams
    public void notifyContentApproval(@ValidateStringParam(name = "site") final String site, @ValidateStringParam(name = "submitter") final String submitter, final List<String> itemsSubmitted,
                                      @ValidateStringParam(name = "approver") final String approver, final ZonedDateTime scheduleDate, final Locale locale) {
        try {
            final Map<String, Object> submitterUser = securityService.getUserProfile(submitter);
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put("files", convertPathsToContent(site, itemsSubmitted));
            templateModel.put("submitterUser", submitter);
            templateModel.put("approver", securityService.getUserProfile(approver));
            templateModel.put("scheduleDate", Date.from(scheduleDate.toInstant()));
            notify(site, Arrays.asList(submitterUser.get(KEY_EMAIL).toString()), NOTIFICATION_KEY_CONTENT_APPROVED,
                locale, templateModel);
        } catch (Throwable ex) {
            logger.error("Unable to Notify Content Approval", ex);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    @ValidateParams
    public String getNotificationMessage(@ValidateStringParam(name = "site") final String site, final NotificationMessageType type, @ValidateStringParam(name = "key") final String key,
                                         final Locale locale, final Pair<String, Object>... params) {
        try {
            final NotificationConfigTO notificationConfig = getNotificationConfig(site, locale);
            String message = null;
            switch (type) {
                case GeneralMessages:
                    message = notificationConfig.getMessages().get(key);
                    break;
                case EmailMessage:
                    message = notificationConfig.getEmailMessageTemplates().get(key).getMessage();
                    break;
                case CompleteMessages:
                    message = notificationConfig.getCompleteMessages().get(key);
                    break;
                case CannedMessages:
                    message = getCannedMessage(notificationConfig.getCannedMessages(), key);
                    break;
                default:
                    logger.error("Requested notification message bundle not recognized: site: {0}, type: {1}," +
                        "key: {2}, locale {3}.", site, type.toString(), key, locale);
                    break;
            }
            if (message != null) {
                Map<String, Object> model = new HashMap<>();
                for (Pair<String, Object> param : params) {
                    model.put(param.getKey(), param.getValue());
                }
                model.put(StudioConstants.SITE_NAME, site);
                return processMessage(key, message, model);
            }
        } catch (Throwable ex) {
            logger.error("Unable to get notification message from notification configuration for site: {0} type: {1}"
                + " key: {2}, locale {3}.", (Exception)ex, site, type, key, locale);
            return StringUtils.EMPTY;
        }
        return StringUtils.EMPTY;
    }

    private String getCannedMessage(final Map<String, List<MessageTO>> cannedMessages, final String key) {
        if (cannedMessages.containsKey(key)) {
            final List<MessageTO> messages = cannedMessages.get(key);
            if (!messages.isEmpty()) {
                return messages.get(0).getBody();
            }
        }
        return "";
    }

    @Override
    @ValidateParams
    public void notifyApprovesContentSubmission(@ValidateStringParam(name = "name") final String site, final List<String> usersToNotify, final
            List<String> itemsSubmitted, @ValidateStringParam(name = "submitter") final String submitter, final ZonedDateTime scheduleDate, final boolean isADelete, final
            @ValidateStringParam(name = "submissionComments") String submissionComments, final Locale locale) {
        try {
            final NotificationConfigTO notificationConfig = getNotificationConfig(site, locale);
            final Map<String, Object> submitterUser = securityService.getUserProfile(submitter);
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put("files", convertPathsToContent(site, itemsSubmitted));
            templateModel.put("submitter", submitterUser);
            templateModel.put("scheduleDate", Date.from(scheduleDate.toInstant()));
            templateModel.put("isDeleted", isADelete);
            templateModel.put("submissionComments", submissionComments);
            if (usersToNotify == null) {
                notify(site, notificationConfig.getApproverEmails(), NOTIFICATION_KEY_SUBMITTED_FOR_REVIEW, locale,
                    templateModel);
            } else {
                notify(site, usersToNotify, NOTIFICATION_KEY_SUBMITTED_FOR_REVIEW, locale, templateModel);
            }
        } catch (Throwable ex) {
            logger.error("Unable to notify content submission", ex);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    @ValidateParams
    public void notify(@ValidateStringParam(name = "site") final String site, final List<String> toUsers, @ValidateStringParam(name = "key") final String key, final Locale locale, final
    Pair<String, Object>... params) {
        try {
            final NotificationConfigTO notificationConfig = getNotificationConfig(site, locale);
            final EmailMessageTemplateTO emailTemplate = notificationConfig.getEmailMessageTemplates().get(key);
            if (emailTemplate != null) {
                Map<String, Object> templateModel = new HashMap<>();
                templateModel.put("siteName", site);
                templateModel.put("liveUrl", siteService.getLiveServerUrl(site));
                templateModel.put("previewUrl", siteService.getPreviewServerUrl(site));
                templateModel.put("authoringUrl", siteService.getAuthoringServerUrl(site));
                for (Pair<String, Object> param : params) {
                    templateModel.put(param.getKey(), param.getValue());
                }
                final String messageBody = processMessage(key, emailTemplate.getMessage(), templateModel);
                final String subject = processMessage(key, emailTemplate.getSubject(), templateModel);
                sendEmail(messageBody, subject, toUsers);
            } else {
                logger.error("Unable to find " + key + " for language " + locale.getLanguage());
            }
        } catch (Throwable ex) {
            logger.error("Unable to notify ", ex);
        }
    }

    @SuppressWarnings("unchecked")
    protected void notify(final String site, final List<String> toUsers, final String key, final Locale locale, final
    Map<String, Object> params) {
        try {
            List<Pair<String, Object>> namedParams = new ArrayList<>();
            for (String paramKey : params.keySet()) {
                namedParams.add(new ImmutablePair<String, Object>(paramKey, params.get(paramKey)));
            }
            notify(site, toUsers, key, locale, namedParams.toArray(new Pair[params.size()]));
        } catch (Throwable ex) {
            logger.error("Unable to notify", ex);
        }
    }

    @Override
    @ValidateParams
    public void notifyContentRejection(@ValidateStringParam(name = "site") final String site, @ValidateStringParam(name = "submittedBy") final String submittedBy, final List<String> rejectedItems,
                                       @ValidateStringParam(name = "rejectionReason") final String rejectionReason, @ValidateStringParam(name = "userThatRejects") final String userThatRejects, final Locale
                                               locale) {
        try {
            final Map<String, Object> submitterUser = securityService.getUserProfile(submittedBy);
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put("files", convertPathsToContent(site, rejectedItems));
            templateModel.put("submitter", submitterUser);
            templateModel.put("rejectionReason", rejectionReason);
            templateModel.put("userThatRejects", securityService.getUserProfile(userThatRejects));
            notify(site, Arrays.asList(submitterUser.get(KEY_EMAIL).toString()), NOTIFICATION_KEY_CONTENT_REJECTED,
                locale, templateModel);
        } catch (Throwable ex) {
            logger.error("Unable to notify content rejection", ex);
        }
    }

    @SuppressWarnings("unchecked")
    protected void loadConfig(final String site) {
        if (notificationConfiguration == null) {
            notificationConfiguration = new HashMap<String, Map<String, NotificationConfigTO>>();
        }
        Map<String, NotificationConfigTO> siteNotificationConfig = new HashMap<String, NotificationConfigTO>();

        try {
            Document document = contentService.getContentAsDocument(site, getConfigPath());
            if (document != null) {
                Element root = document.getRootElement();
                final List<Element> languages = root.selectNodes("//lang");
                if (languages.isEmpty()) {
                    throw new ConfigurationException("Notification Configuration is a invalid xml file, missing " +
                        "at " + "least one lang");

                }
                for (Element language : languages) {
                    String messagesLang = language.attributeValue("name");
                    if (StringUtils.isNotBlank(messagesLang)) {
                        if (!siteNotificationConfig.containsKey(messagesLang)) {
                            siteNotificationConfig.put(messagesLang, new NotificationConfigTO(site));
                        }
                        NotificationConfigTO configForLang = siteNotificationConfig.get(messagesLang);
                        loadGenericMessage((Element)language.selectSingleNode("//generalMessages"), configForLang
                            .getMessages());
                        loadGenericMessage((Element)language.selectSingleNode("//completeMessages"), configForLang
                            .getCompleteMessages());
                        loadEmailTemplates((Element)language.selectSingleNode("//emailTemplates"), configForLang
                            .getEmailMessageTemplates());
                        loadCannedMessages((Element)language.selectSingleNode("//cannedMessages"), configForLang
                            .getCannedMessages());
                        loadEmailList(site, (Element)language.selectSingleNode("//deploymentFailureNotification"),
                            configForLang.getDeploymentFailureNotifications());
                        loadEmailList(site, (Element)language.selectSingleNode("//approverEmails"), configForLang
                            .getApproverEmails());
                    } else {
                        logger.error("A lang section does not have the 'name' attribute, ignoring");
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("Unable to read or load notification '" + getConfigPath() + "' configuration for " + site, ex);
        }
        notificationConfiguration.put(site, siteNotificationConfig);
    }

    @SuppressWarnings("unchecked")
    private void loadEmailList(final String site, final Element emailList, final List<String>
        deploymentFailureNotifications) {
        if (emailList != null) {
            List<Element> emails = emailList.elements();
            if (!emails.isEmpty()) {
                for (Element emailNode : emails) {
                    final String email = emailNode.getText();
                    if (EmailUtils.validateEmail(email)) {
                        deploymentFailureNotifications.add(email);
                    }
                }
            } else {
                deploymentFailureNotifications.add(siteService.getAdminEmailAddress(site));
            }
        } else {
            logger.error("Unable to read completed Messages (they don't exist)");
        }
    }

    @SuppressWarnings("unchecked")
    protected void loadGenericMessage(final Element emailTemplates, final Map<String, String> messageContainer) {
        if (emailTemplates != null) {
            List<Element> messages = emailTemplates.elements();
            if (!messages.isEmpty()) {
                for (Element message : messages) {
                    final String messageKey = message.attributeValue("key");
                    final String messageText = message.getText();
                    messageContainer.put(messageKey, messageText);
                }
            } else {
                logger.error("completed Messages is empty");
            }
        } else {
            logger.error("Unable to read completed Messages (they don't exist)");
        }
    }

    @SuppressWarnings("unchecked")
    protected void loadEmailTemplates(final Element emailTemplates, final Map<String, EmailMessageTemplateTO>
        messageContainer) {
        if (emailTemplates != null) {
            List<Element> messages = emailTemplates.elements();
            if (!messages.isEmpty()) {
                for (Element message : messages) {
                    final Node subjectNode = message.element("subject");
                    final Node bodyNode = message.element("body");
                    final String messageKey = message.attributeValue("key");
                    if (subjectNode != null && bodyNode != null) {
                        EmailMessageTemplateTO emailMessageTemplateTO = new EmailMessageTemplateTO(subjectNode
                            .getText(), bodyNode.getText());
                        messageContainer.put(messageKey, emailMessageTemplateTO);
                    } else {
                        logger.error("Email message malformed");
                    }
                }
            } else {
                logger.error("completed Messages is empty");
            }
        } else {
            logger.error("Unable to read completed Messages (they don't exist)");
        }
    }

    @SuppressWarnings("unchecked")
    protected void loadCannedMessages(final Element completedMessages, final Map<String, List<MessageTO>>
        messageContainer) {
        if (completedMessages != null) {
            List<Element> messages = completedMessages.elements();
            if (!messages.isEmpty()) {
                for (Element message : messages) {
                    final String messageKey = message.attributeValue("key");
                    final String messageContent = message.getText();
                    final String messageTitle = message.attributeValue("title");
                    if (!messageContainer.containsKey(messageKey)) {
                        messageContainer.put(messageKey, new ArrayList<MessageTO>());
                    }
                    List<MessageTO> messageTOs = messageContainer.get(messageKey);
                    messageTOs.add(new MessageTO(messageTitle, messageContent, messageKey));
                }
            } else {
                logger.error("completed Messages is empty");
            }
        } else {
            logger.error("Unable to read completed Messages (they don't exist)");
        }
    }

    @Override
    @ValidateParams
    public void reloadConfiguration(@ValidateStringParam(name = "site") final String site) {
        loadConfig(site);
    }

    protected NotificationConfigTO getNotificationConfig(final String site, final Locale locale) {
        Map<String, NotificationConfigTO> siteNotificationConfig = notificationConfiguration.get(site);
        if (siteNotificationConfig == null || siteNotificationConfig.isEmpty()) {
            loadConfig(site);
            siteNotificationConfig = notificationConfiguration.get(site);
        }
        Locale realLocale = locale;
        if (locale == null) {
            realLocale = Locale.ENGLISH;
        }
        return siteNotificationConfig.get(realLocale.getLanguage());
    }


    protected void sendEmail(final String message, final String subject, final List<String> sendTo) {
        EmailMessageTO emailMessage = new EmailMessageTO(subject, message, StringUtils.join(sendTo, ','));
        emailMessages.addEmailMessage(emailMessage);
    }

    protected String processMessage(final String templateName, final String message, final Map<String, Object>
        templateModel) {
        StringWriter out = new StringWriter();
        try {
            Template t = new Template(templateName, new StringReader(message), configuration);
            t.process(templateModel, out);
            return out.toString();
        } catch (TemplateException | IOException ex) {
            logger.error("Unable to process notification message " + templateName, ex);
        }
        return null;
    }

    protected Set<ContentItemTO> convertPathsToContent(final String site, final List<String> listOfPaths) {
        Set<ContentItemTO> files = new HashSet<>(listOfPaths.size());
        for (String path : listOfPaths) {
            files.add(contentService.getContentItem(site, path));
        }
        return files;
    }

    public String getConfigPath() {
        return studioConfiguration.getProperty(NOTIFICATION_CONFIGURATION_FILE);
    }

    public String getTemplateTimezone() {
        return studioConfiguration.getProperty(NOTIFICATION_TIMEZONE);
    }

    public void setContentService(final ContentService contentService) {
        this.contentService = contentService;
    }

    public void setEmailMessages(final EmailMessageQueueTo emailMessages) {
        this.emailMessages = emailMessages;
    }

    public void setServicesConfig(final ServicesConfig servicesConfig) {
        this.servicesConfig = servicesConfig;
    }

    public void setSiteService(final SiteService siteService) {
        this.siteService = siteService;
    }

    public void setSecurityService(final SecurityService securityService) {
        this.securityService = securityService;
    }

    public StudioConfiguration getStudioConfiguration() {
        return studioConfiguration;
    }

    public void setStudioConfiguration(StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }

    public ConfigurationService getConfigurationService() {
        return configurationService;
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }
}

