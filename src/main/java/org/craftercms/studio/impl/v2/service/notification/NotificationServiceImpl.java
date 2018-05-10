package org.craftercms.studio.impl.v2.service.notification;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
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
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.craftercms.commons.lang.Callback;
import org.craftercms.commons.mail.EmailUtils;
import org.craftercms.commons.validation.annotations.param.ValidateParams;
import org.craftercms.commons.validation.annotations.param.ValidateStringParam;
import org.craftercms.core.service.CacheService;
import org.craftercms.core.util.cache.CacheTemplate;
import org.craftercms.engine.exception.ConfigurationException;
import org.craftercms.studio.api.v1.constant.CStudioConstants;
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
import org.craftercms.studio.api.v2.service.notification.NotificationMessageType;
import org.craftercms.studio.api.v2.service.notification.NotificationService;
import org.craftercms.studio.impl.v1.service.StudioCacheContext;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

public class NotificationServiceImpl implements NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private static final String DEPLOYMENT_ERROR_MSG_KEY = "deploymentError";
    private static final String NOTIFY_CONTENT_APPROVAL = "contentApproval";
    private static final String NOTIFY_CONTENT_SUBMIT_FOR_APPROVAL = "submitToApproval";
    private static final String NOTIFY_CONTENT_REJECTED = "contentRejected";

    protected Map<String, NotificationConfigTO> notificationConfiguration;
    protected CacheTemplate cacheTemplate;
    protected String configPath;
    protected String configFileName;
    protected ContentService contentService;
    protected EmailMessageQueueTo emailMessages;
    protected ServicesConfig servicesConfig;
    protected SiteService siteService;
    protected SecurityService securityService;
    protected boolean enable;
    private String templateTimezone;
    private Configuration configuration;

    public NotificationServiceImpl() {
        notificationConfiguration = new HashMap<>();
    }


    public void init() {
        configuration = new Configuration(Configuration.VERSION_2_3_23);
        configuration.setTimeZone(TimeZone.getTimeZone(templateTimezone));
        configuration.setObjectWrapper(new DefaultObjectWrapperBuilder(Configuration.VERSION_2_3_23).build());
    }

    @Override
    @ValidateParams
    public void notifyDeploymentError(@ValidateStringParam(name = "site") final String site, final Throwable throwable, final List<String>
        filesUnableToPublish, final Locale locale) {
        try {
            if (enable) {
                final NotificationConfigTO notificationConfig = getNotificationConfig(site, locale);
                final Map<String, Object> templateModel = new HashMap<>();
                templateModel.put("deploymentError", throwable);
                templateModel.put("files", convertPathsToContent(site, filesUnableToPublish));
                notify(site, notificationConfig.getDeploymentFailureNotifications(), DEPLOYMENT_ERROR_MSG_KEY,
                    locale, templateModel);

            }
        } catch (Throwable ex) {
            logger.error("Unable to Notify Error", ex);
        }
    }


    @Override
    @ValidateParams
    public void notifyDeploymentError(@ValidateStringParam(name = "name") final String name, final Throwable throwable) {
        notifyDeploymentError(name, throwable, Collections.EMPTY_LIST, Locale.ENGLISH);
    }

    @Override
    @ValidateParams
    public void notifyDeploymentError(@ValidateStringParam(name = "name") final String name) {
        notifyDeploymentError(name, null);
    }

    @Override
    @ValidateParams
    public void notifyContentApproval(@ValidateStringParam(name = "site") final String site, @ValidateStringParam(name = "submitter") final String submitter, final List<String> itemsSubmitted,
                                      @ValidateStringParam(name = "approver") final String approver, final Date scheduleDate, final Locale locale) {
        try {
            if (enable) {
                final Map<String, String> submitterUser = securityService.getUserProfile(submitter);
                Map<String, Object> templateModel = new HashMap<>();
                templateModel.put("files", convertPathsToContent(site, itemsSubmitted));
                templateModel.put("submitterUser", submitter);
                templateModel.put("approver", securityService.getUserProfile(approver));
                templateModel.put("scheduleDate", scheduleDate);
                notify(site, Arrays.asList(submitterUser.get("email")), NOTIFY_CONTENT_APPROVAL, locale, templateModel);
            }
        }
        catch(Throwable ex) {
            logger.error("Unable to Notify Content Approval", ex);
        }
    }

    @Override
    @ValidateParams
    public String getNotificationMessage(@ValidateStringParam(name = "site") final String site, final NotificationMessageType type, @ValidateStringParam(name = "key") final String key,
                                         final Locale locale, final Pair<String, Object>... params) {
        try {
            if (enable) {
                final NotificationConfigTO notificationConfig = getNotificationConfig(site, locale);
                String message = null;
                switch (type) {
                    default:
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
                        message = getCannedMessage(notificationConfig.getCannedMessages(),key);
                        break;
                }
                if (message != null) {
                    Map<String, Object> model = new HashMap<>();
                    for (Pair<String, Object> param : params) {
                        model.put(param.getKey(), param.getValue());
                    }
                    model.put("siteName", site);
                    return processMessage(key, message, model);
                }
            }

        } catch (Throwable ex) {
            logger.error("Unable to Get Message", ex);
            return "";
        }
        return "";
    }

    private String getCannedMessage(final Map<String, List<MessageTO>> cannedMessages, final String key) {
        if(cannedMessages.containsKey(key)){
            final List<MessageTO> messages = cannedMessages.get(key);
            if(!messages.isEmpty()){
             return messages.get(0).getBody();
            }
        }
        return "";
    }

    @Override
    @ValidateParams
    public void notifyApprovesContentSubmission(@ValidateStringParam(name = "site") final String site, final List<String> usersToNotify, final
    List<String> itemsSubmitted, @ValidateStringParam(name = "submitter") final String submitter, final Date scheduleDate, final boolean isADelete, @ValidateStringParam(name = "submissionComments") final
    String submissionComments, final Locale locale) {
        try {
            if (enable) {
                final NotificationConfigTO notificationConfig = getNotificationConfig(site, locale);
                final Map<String, String> submitterUser = securityService.getUserProfile(submitter);
                Map<String, Object> templateModel = new HashMap<>();
                templateModel.put("files", convertPathsToContent(site, itemsSubmitted));
                templateModel.put("submitter", submitterUser);
                templateModel.put("scheduleDate", scheduleDate);
                templateModel.put("isDeleted", isADelete);
                templateModel.put("submissionComments", submissionComments);
                if (usersToNotify == null) {
                    notify(site, notificationConfig.getApproverEmails(), NOTIFY_CONTENT_SUBMIT_FOR_APPROVAL, locale,
                        templateModel);
                } else {
                    notify(site, usersToNotify, NOTIFY_CONTENT_SUBMIT_FOR_APPROVAL, locale, templateModel);
                }
            }

        } catch (Throwable ex) {
            logger.error("Unable to notify content submission", ex);
        }
    }

    @Override
    @ValidateParams
    public void notify(@ValidateStringParam(name = "site") final String site, final List<String> toUsers, @ValidateStringParam(name = "key") final String key, final Locale locale, final
    Pair<String, Object>... params) {
        try {
            if (enable) {
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
            }
        } catch (Throwable ex) {
            logger.error("Unable to notify ", ex);
        }
    }

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
            if (enable) {
                final Map<String, String> submitterUser = securityService.getUserProfile(submittedBy);
                Map<String, Object> templateModel = new HashMap<>();
                templateModel.put("files", convertPathsToContent(site, rejectedItems));
                templateModel.put("submitter", submitterUser);
                templateModel.put("rejectionReason", rejectionReason);
                templateModel.put("userThatRejects", securityService.getUserProfile(userThatRejects));
                notify(site, Arrays.asList(submitterUser.get("email")), NOTIFY_CONTENT_REJECTED, locale, templateModel);
            }
        } catch (Throwable ex) {
            logger.error("Unable to notify content rejection", ex);
        }
    }

    protected Map<String, NotificationConfigTO> loadConfig(final String site) {
        notificationConfiguration = new HashMap<>();
        if (enable) {
            String configFullPath = configPath.replaceFirst(CStudioConstants.PATTERN_SITE, site);
            configFullPath = configFullPath + "/" + configFileName;
            try {
                Document document = contentService.getContentAsDocument(configFullPath);
                if (document != null) {
                    Element root = document.getRootElement();
                    final List<Element> languages = root.selectNodes("//lang");
                    if (languages.isEmpty()) {
                        throw new ConfigurationException("Notification Configuration is a invalid xml file, missing "
                            + "at " + "least one lang");

                    }
                    for (Element language : languages) {
                        String messagesLang = language.attributeValue("name");
                        if (StringUtils.isNotBlank(messagesLang)) {
                            if (!notificationConfiguration.containsKey(messagesLang)) {
                                notificationConfiguration.put(messagesLang, new NotificationConfigTO(site));
                            }
                            NotificationConfigTO configForLang = notificationConfiguration.get(messagesLang);
                            loadGenericMessage((Element)language.selectSingleNode("//generalMessages"), configForLang
                                .getMessages());
                            loadGenericMessage((Element)language.selectSingleNode("//completeMessages"),
                                configForLang.getCompleteMessages());
                            loadEmailTemplates((Element)language.selectSingleNode("//emailTemplates"), configForLang
                                .getEmailMessageTemplates());
                            loadCannedMessages((Element)language.selectSingleNode("//cannedMessages"), configForLang
                                .getCannedMessages());
                            loadEmailList(site, (Element)language.selectSingleNode("//deploymentFailureNotification")
                                , configForLang.getDeploymentFailureNotifications());
                            loadEmailList(site, (Element)language.selectSingleNode("//approverEmails"), configForLang
                                .getApproverEmails());
                        } else {
                            logger.error("A lang section does not have the 'name' attribute, ignoring");
                        }
                    }
                }
            } catch (Exception ex) {
                logger.error("Unable to read or load notification '" + configFullPath + "' configuration for " +
                    site, ex);
            }
        }
        return notificationConfiguration;
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
                deploymentFailureNotifications.add(siteService.getAdminEmailAddress(site));
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
                    messageTOs.add(new MessageTO(messageTitle, messageContent,messageKey));
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
        if (enable) {
            CacheService cacheService = cacheTemplate.getCacheService();
            StudioCacheContext cacheContext = new StudioCacheContext(site, true);
            Object cacheKey = cacheTemplate.getKey(site, configPath.replaceFirst(CStudioConstants.PATTERN_SITE, site)
                , configFileName);

            cacheService.remove(cacheContext, cacheKey);
            Map<String, NotificationConfigTO> config = loadConfig(site);
            cacheService.put(cacheContext, cacheKey, config);
        }
    }

    @Override
    public boolean isEnable() {
        return enable;
    }

    protected NotificationConfigTO getNotificationConfig(final String site, final Locale locale) {
        CacheService cacheService = cacheTemplate.getCacheService();
        StudioCacheContext cacheContext = new StudioCacheContext(site, true);
        Map<String, NotificationConfigTO> config = cacheTemplate.getObject(cacheContext, new Callback<Map<String,
            NotificationConfigTO>>() {
            @Override
            public Map<String, NotificationConfigTO> execute() {
                return loadConfig(site);
            }
        }, site, configPath.replaceFirst(CStudioConstants.PATTERN_SITE, site), configFileName);

        Locale realLocale = locale;
        if (locale == null) {
            realLocale = Locale.ENGLISH;
        }
        return config.get(realLocale.getLanguage());
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

    public void setCacheTemplate(final CacheTemplate cacheTemplate) {
        this.cacheTemplate = cacheTemplate;
    }

    public void setConfigPath(final String configPath) {
        this.configPath = configPath;
    }

    public void setConfigFileName(final String configFileName) {
        this.configFileName = configFileName;
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

    public void setEnable(final boolean enable) {
        this.enable = enable;
    }

    public void setTemplateTimezone(final String templateTimezone) {
        this.templateTimezone = templateTimezone;
    }

    public GeneralLockService getGeneralLockService() { return generalLockService; }
    public void setGeneralLockService(GeneralLockService generalLockService) { this.generalLockService = generalLockService; }

    protected GeneralLockService generalLockService;
}

