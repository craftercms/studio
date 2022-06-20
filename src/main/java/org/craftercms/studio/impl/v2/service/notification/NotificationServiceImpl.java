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

package org.craftercms.studio.impl.v2.service.notification;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;

import com.google.common.cache.Cache;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapperBuilder;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.craftercms.commons.mail.EmailUtils;
import org.craftercms.commons.validation.annotations.param.ValidateParams;
import org.craftercms.commons.validation.annotations.param.ValidateStringParam;
import org.craftercms.studio.api.v1.dal.PublishRequest;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.craftercms.studio.api.v1.to.ContentItemTO;
import org.craftercms.studio.api.v1.to.EmailMessageQueueTo;
import org.craftercms.studio.api.v1.to.EmailMessageTO;
import org.craftercms.studio.api.v1.to.EmailMessageTemplateTO;
import org.craftercms.studio.api.v1.to.MessageTO;
import org.craftercms.studio.api.v1.to.NotificationConfigTO;
import org.craftercms.studio.api.v2.service.config.ConfigurationService;
import org.craftercms.studio.api.v2.service.notification.NotificationMessageType;
import org.craftercms.studio.api.v2.service.notification.NotificationService;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.craftercms.studio.api.v1.constant.SecurityConstants.KEY_EMAIL;
import static org.craftercms.studio.api.v1.constant.StudioConstants.MODULE_STUDIO;
import static org.craftercms.studio.api.v1.constant.StudioConstants.SITE_NAME;
import static org.craftercms.studio.api.v1.constant.StudioXmlConstants.DOCUMENT_ELEMENT_APPROVER_EMAILS;
import static org.craftercms.studio.api.v1.constant.StudioXmlConstants.DOCUMENT_ELEMENT_CANNED_MESSAGES;
import static org.craftercms.studio.api.v1.constant.StudioXmlConstants.DOCUMENT_ELEMENT_COMPLETE_MESSAGES;
import static org.craftercms.studio.api.v1.constant.StudioXmlConstants.DOCUMENT_ELEMENT_DEPLOYMENT_FAILURE_NOTIFICATION;
import static org.craftercms.studio.api.v1.constant.StudioXmlConstants.DOCUMENT_ELEMENT_EMAIL_TEMPLATES;
import static org.craftercms.studio.api.v1.constant.StudioXmlConstants.DOCUMENT_ELEMENT_GENERAL_MESSAGES;
import static org.craftercms.studio.api.v1.constant.StudioXmlConstants.DOCUMENT_ELEMENT_REPOSITORY_MERGE_CONFLICT_NOTIFICATION;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_ENVIRONMENT_ACTIVE;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.NOTIFICATION_CONFIGURATION_FILE;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.NOTIFICATION_TIMEZONE;

public class NotificationServiceImpl implements NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private static final String NOTIFICATION_KEY_DEPLOYMENT_ERROR = "deploymentError";
    private static final String NOTIFICATION_KEY_CONTENT_APPROVED = "contentApproved";
    private static final String NOTIFICATION_KEY_SUBMITTED_FOR_REVIEW = "submittedForReview";
    private static final String NOTIFICATION_KEY_CONTENT_REJECTED = "contentRejected";
    private static final String NOTIFICATION_KEY_REPOSITORY_MERGE_CONFLICT = "repositoryMergeConflict";

    private static final String TEMPLATE_MODEL_DEPLOYMENT_ERROR = "deploymentError";
    private static final String TEMPLATE_MODEL_FILES = "files";
    private static final String TEMPLATE_MODEL_SUBMITTER_USER = "submitterUser";
    private static final String TEMPLATE_MODEL_APPROVER = "approver";
    private static final String TEMPLATE_MODEL_SCHEDULED_DATE = "scheduleDate";
    private static final String TEMPLATE_MODEL_SUBMITTER = "submitter";
    private static final String TEMPLATE_MODEL_IS_DELETED = "isDeleted";
    private static final String TEMPLATE_MODEL_SUBMISSION_COMMENTS = "submissionComments";
    private static final String TEMPLATE_MODEL_SITE_NAME = "siteName";
    private static final String TEMPLATE_MODEL_LIVE_URL = "liveUrl";
    private static final String TEMPLATE_MODEL_AUTHORING_URL = "authoringUrl";
    private static final String TEMPLATE_MODEL_REJECTION_REASON = "rejectionReason";
    private static final String TEMPLATE_MODEL_USER_THAT_REJECTS = "userThatRejects";

    private static final String MESSAGE_ELEMENT_SUBJECT = "subject";
    private static final String MESSAGE_ELEMENT_BODY = "body";
    private static final String MESSAGE_ATTRIBUTE_KEY = "key";
    private static final String MESSAGE_ATTRIBUTE_TITLE = "title";

    protected ContentService contentService;
    protected EmailMessageQueueTo emailMessages;
    protected ServicesConfig servicesConfig;
    protected SecurityService securityService;
    private Configuration configuration;
    protected StudioConfiguration studioConfiguration;
    protected ConfigurationService configurationService;

    protected Cache<String, NotificationConfigTO> cache;

    public void init() {
        configuration = new Configuration(Configuration.VERSION_2_3_23);
        configuration.setTimeZone(TimeZone.getTimeZone(getTemplateTimezone()));
        configuration.setObjectWrapper(new DefaultObjectWrapperBuilder(Configuration.VERSION_2_3_23).build());
    }

    @Override
    @ValidateParams
    public void notifyDeploymentError(@ValidateStringParam(name = "site") final String site, final Throwable throwable,
                                      final List<PublishRequest> filesUnableToPublish) {
        try {
            final NotificationConfigTO notificationConfig = getNotificationConfig(site);
            final Map<String, Object> templateModel = new HashMap<>();
            templateModel.put(TEMPLATE_MODEL_DEPLOYMENT_ERROR, ExceptionUtils.getStackTrace(throwable));
            templateModel.put(TEMPLATE_MODEL_FILES, filesUnableToPublish);
            notify(site, notificationConfig.getDeploymentFailureNotifications(), NOTIFICATION_KEY_DEPLOYMENT_ERROR,
                    templateModel);
        } catch (Throwable ex) {
            logger.error("Unable to Notify Error", ex);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    @ValidateParams
    public void notifyDeploymentError(@ValidateStringParam(name = "name") final String name, final Throwable throwable) {
        notifyDeploymentError(name, throwable, Collections.EMPTY_LIST);
    }

    @Override
    @ValidateParams
    public void notifyContentApproval(@ValidateStringParam(name = "site") final String site,
                                      @ValidateStringParam(name = "submitter") final String submitter,
                                      final List<String> itemsSubmitted,
                                      @ValidateStringParam(name = "approver") final String approver,
                                      final ZonedDateTime scheduleDate) {
        try {
            final Map<String, Object> submitterUser = securityService.getUserProfile(submitter);
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put(TEMPLATE_MODEL_FILES, convertPathsToContent(site, itemsSubmitted));
            templateModel.put(TEMPLATE_MODEL_SUBMITTER_USER, submitter);
            templateModel.put(TEMPLATE_MODEL_APPROVER, securityService.getUserProfile(approver));
            templateModel.put(TEMPLATE_MODEL_SCHEDULED_DATE, (scheduleDate == null) ?
                    null : Date.from(scheduleDate.toInstant()));
            notify(site, singletonList(submitterUser.get(KEY_EMAIL).toString()), NOTIFICATION_KEY_CONTENT_APPROVED,
                    templateModel);
        } catch (Throwable ex) {
            logger.error("Unable to Notify Content Approval", ex);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    @ValidateParams
    public String getNotificationMessage(@ValidateStringParam(name = "site") final String site,
                                         final NotificationMessageType type,
                                         @ValidateStringParam(name = "key") final String key,
                                         final Pair<String, Object>... params) {
        try {
            final NotificationConfigTO notificationConfig = getNotificationConfig(site);
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
                        "key: {2}.", site, type.toString(), key);
                    break;
            }
            if (message != null) {
                Map<String, Object> model = new HashMap<>();
                for (Pair<String, Object> param : params) {
                    model.put(param.getKey(), param.getValue());
                }
                model.put(SITE_NAME, site);
                return processMessage(key, message, model);
            }
        } catch (Exception ex) {
            logger.error("Unable to get notification message from notification configuration for site: {0} type: {1}"
                + " key: {2}.", ex, site, type, key);
            return EMPTY;
        }
        return EMPTY;
    }

    private String getCannedMessage(final Map<String, List<MessageTO>> cannedMessages, final String key) {
        if (cannedMessages.containsKey(key)) {
            final List<MessageTO> messages = cannedMessages.get(key);
            if (!messages.isEmpty()) {
                return messages.get(0).getBody();
            }
        }
        return EMPTY;
    }

    @Override
    @ValidateParams
    public void notifyApprovesContentSubmission(@ValidateStringParam(name = "name") final String site,
                                                final List<String> usersToNotify, final List<String> itemsSubmitted,
                                                @ValidateStringParam(name = "submitter") final String submitter,
                                                final ZonedDateTime scheduleDate, final boolean isADelete,
                                                final @ValidateStringParam(name = "submissionComments")
                                                            String submissionComments) {
        try {
            final NotificationConfigTO notificationConfig = getNotificationConfig(site);
            final Map<String, Object> submitterUser = securityService.getUserProfile(submitter);
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put(TEMPLATE_MODEL_FILES, convertPathsToContent(site, itemsSubmitted));
            templateModel.put(TEMPLATE_MODEL_SUBMITTER, submitterUser);
            templateModel.put(TEMPLATE_MODEL_SCHEDULED_DATE, (scheduleDate == null) ? null : Date.from(scheduleDate.toInstant()));
            templateModel.put(TEMPLATE_MODEL_IS_DELETED, isADelete);
            templateModel.put(TEMPLATE_MODEL_SUBMISSION_COMMENTS, submissionComments);
            if (usersToNotify == null) {
                notify(site, notificationConfig.getApproverEmails(), NOTIFICATION_KEY_SUBMITTED_FOR_REVIEW,
                        templateModel);
            } else {
                notify(site, usersToNotify, NOTIFICATION_KEY_SUBMITTED_FOR_REVIEW, templateModel);
            }
        } catch (Throwable ex) {
            logger.error("Unable to notify content submission", ex);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    @ValidateParams
    public void notify(@ValidateStringParam(name = "site") final String site, final List<String> toUsers,
                       @ValidateStringParam(name = "key") final String key,
                       final Pair<String, Object>... params) {
        try {
            final NotificationConfigTO notificationConfig = getNotificationConfig(site);
            final EmailMessageTemplateTO emailTemplate = notificationConfig.getEmailMessageTemplates().get(key);
            if (emailTemplate != null) {
                Map<String, Object> templateModel = new HashMap<>();
                templateModel.put(TEMPLATE_MODEL_SITE_NAME, site);
                templateModel.put(TEMPLATE_MODEL_LIVE_URL, servicesConfig.getLiveUrl(site));
                templateModel.put(TEMPLATE_MODEL_AUTHORING_URL, servicesConfig.getAuthoringUrl(site));
                for (Pair<String, Object> param : params) {
                    templateModel.put(param.getKey(), param.getValue());
                }
                final String messageBody = processMessage(key, emailTemplate.getMessage(), templateModel);
                final String subject = processMessage(key, emailTemplate.getSubject(), templateModel);
                sendEmail(messageBody, subject, toUsers);
            } else {
                logger.error("Unable to find " + key );
            }
        } catch (Throwable ex) {
            logger.error("Unable to notify ", ex);
        }
    }

    @SuppressWarnings("unchecked")
    protected void notify(final String site, final List<String> toUsers, final String key,
                          final Map<String, Object> params) {
        try {
            List<Pair<String, Object>> namedParams = new ArrayList<>();
            for (String paramKey : params.keySet()) {
                namedParams.add(new ImmutablePair<>(paramKey, params.get(paramKey)));
            }
            notify(site, toUsers, key, namedParams.toArray(new Pair[params.size()]));
        } catch (Throwable ex) {
            logger.error("Unable to notify", ex);
        }
    }

    @Override
    @ValidateParams
    public void notifyContentRejection(@ValidateStringParam(name = "site") final String site,
                                       final List<String> submittedByList,
                                       final List<String> rejectedItems,
                                       @ValidateStringParam(name = "rejectionReason") final String rejectionReason,
                                       @ValidateStringParam(name = "userThatRejects") final String userThatRejects) {
        try {
            var submitterUsers = new ArrayList<Map<String, Object>>();
            submittedByList.forEach(submittedBy -> {
                Map<String, Object> userProfile = null;
                try {
                    userProfile = securityService.getUserProfile(submittedBy);

                } catch (ServiceLayerException | UserNotFoundException e) {
                    logger.debug("User not found by username " + submittedBy);
                    try {
                        userProfile = securityService.getUserProfileByGitName(submittedBy);
                    } catch (ServiceLayerException | UserNotFoundException ex) {
                        logger.debug("Didn't find user " + submittedBy + ". Notification will not be sent " +
                                "to that user.", ex);
                    }
                }
                if (Objects.nonNull(userProfile)) {
                    submitterUsers.add(userProfile);
                }
            });
            if (!submitterUsers.isEmpty()) {
                Map<String, Object> templateModel = new HashMap<>();
                templateModel.put(TEMPLATE_MODEL_FILES, convertPathsToContent(site, rejectedItems));
                templateModel.put(TEMPLATE_MODEL_REJECTION_REASON, rejectionReason);
                templateModel.put(TEMPLATE_MODEL_USER_THAT_REJECTS, securityService.getUserProfile(userThatRejects));
                List<String> emails = submitterUsers.stream().map(u -> u.get(KEY_EMAIL).toString())
                        .collect(Collectors.toList());
                notify(site, emails, NOTIFICATION_KEY_CONTENT_REJECTED, templateModel);
            } else {
                logger.info("Unable to notify content rejection. User(s) " +
                        StringUtils.join(submittedByList, ", ") + " not found.");
            }
        } catch (Exception ex) {
            logger.error("Unable to notify content rejection", ex);
        }
    }

    protected NotificationConfigTO loadConfig(final String site) {
        var environment = studioConfiguration.getProperty(CONFIGURATION_ENVIRONMENT_ACTIVE);
        var configPath = getConfigPath();
        var cacheKey = configurationService.getCacheKey(site, MODULE_STUDIO, configPath, environment, "object");

        try {
            NotificationConfigTO config = cache.getIfPresent(cacheKey);
            if (Objects.isNull(config)) {
                logger.debug("Cache miss {0}", cacheKey);

                config = new NotificationConfigTO();
                Document document =
                        configurationService.getConfigurationAsDocument(site, MODULE_STUDIO, configPath, environment);
                Element root = document.getRootElement();

                if (root.getNodeType() == Node.ELEMENT_NODE) {
                    loadGenericMessage((Element)root.selectSingleNode(DOCUMENT_ELEMENT_GENERAL_MESSAGES),
                            config.getMessages());
                    loadGenericMessage((Element)root.selectSingleNode(DOCUMENT_ELEMENT_COMPLETE_MESSAGES),
                            config.getCompleteMessages());
                    loadEmailTemplates((Element)root.selectSingleNode(DOCUMENT_ELEMENT_EMAIL_TEMPLATES),
                            config.getEmailMessageTemplates());
                    loadCannedMessages((Element)root.selectSingleNode(DOCUMENT_ELEMENT_CANNED_MESSAGES),
                            config.getCannedMessages());
                    loadEmailList(site, (Element)root.selectSingleNode(DOCUMENT_ELEMENT_DEPLOYMENT_FAILURE_NOTIFICATION),
                            config.getDeploymentFailureNotifications());
                    loadEmailList(site, (Element)root.selectSingleNode(DOCUMENT_ELEMENT_APPROVER_EMAILS),
                            config.getApproverEmails());
                    loadEmailList(site, (Element)root.selectSingleNode(DOCUMENT_ELEMENT_REPOSITORY_MERGE_CONFLICT_NOTIFICATION),
                            config.getRepositoryMergeConflictNotifications());
                } else {
                    logger.info("Unable to execute against a non-XML-element: " + root.getUniquePath());
                }

                cache.put(cacheKey, config);
            }
            return config;
        } catch (Exception e) {
            logger.error("Unable to read or load notification '" + getConfigPath() + "' configuration for " + site, e);
            return null;
        }
    }

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
                deploymentFailureNotifications.add(servicesConfig.getAdminEmailAddress(site));
            }
        } else {
            logger.error("Unable to read completed Messages (they don't exist)");
        }
    }

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

    protected void loadEmailTemplates(final Element emailTemplates,
                                      final Map<String, EmailMessageTemplateTO>  messageContainer) {
        if (emailTemplates != null) {
            List<Element> messages = emailTemplates.elements();
            if (!messages.isEmpty()) {
                for (Element message : messages) {
                    final Node subjectNode = message.element(MESSAGE_ELEMENT_SUBJECT);
                    final Node bodyNode = message.element(MESSAGE_ELEMENT_BODY);
                    final String messageKey = message.attributeValue(MESSAGE_ATTRIBUTE_KEY);
                    if (subjectNode != null && bodyNode != null) {
                        EmailMessageTemplateTO emailMessageTemplateTO =
                                new EmailMessageTemplateTO(subjectNode.getText(), bodyNode.getText());
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

    protected void loadCannedMessages(final Element completedMessages, final Map<String, List<MessageTO>>
        messageContainer) {
        if (completedMessages != null) {
            List<Element> messages = completedMessages.elements();
            if (!messages.isEmpty()) {
                for (Element message : messages) {
                    final String messageKey = message.attributeValue(MESSAGE_ATTRIBUTE_KEY);
                    final String messageContent = message.getText();
                    final String messageTitle = message.attributeValue(MESSAGE_ATTRIBUTE_TITLE);
                    if (!messageContainer.containsKey(messageKey)) {
                        messageContainer.put(messageKey, new ArrayList<>());
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

    protected NotificationConfigTO getNotificationConfig(final String site) {
        return loadConfig(site);
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

    @Override
    @ValidateParams
    public void notifyRepositoryMergeConflict(@ValidateStringParam(name = "site") final String site,
                                              final List<String> filesUnableToMerge) {
        try {
            final NotificationConfigTO notificationConfig = getNotificationConfig(site);
            final Map<String, Object> templateModel = new HashMap<>();
            templateModel.put(TEMPLATE_MODEL_FILES, filesUnableToMerge);
            notify(site, notificationConfig.getRepositoryMergeConflictNotifications(),
                    NOTIFICATION_KEY_REPOSITORY_MERGE_CONFLICT, templateModel);
        } catch (Throwable ex) {
            logger.error("Unable to Notify Error", ex);
        }
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

    public void setSecurityService(final SecurityService securityService) {
        this.securityService = securityService;
    }

    public void setStudioConfiguration(StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    public void setCache(Cache<String, NotificationConfigTO> cache) {
        this.cache = cache;
    }
}

