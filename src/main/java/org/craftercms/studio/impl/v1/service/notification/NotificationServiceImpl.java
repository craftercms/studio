/*******************************************************************************
 * Crafter Studio Web-content authoring solution
 *     Copyright (C) 2007-2013 Crafter Software Corporation.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.craftercms.studio.impl.v1.service.notification;

import org.apache.commons.lang.StringUtils;
import org.craftercms.commons.lang.Callback;
import org.craftercms.core.service.CacheService;
import org.craftercms.core.util.cache.CacheTemplate;
import org.craftercms.studio.api.v1.constant.CStudioConstants;
import org.craftercms.studio.api.v1.constant.DmConstants;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.ConfigurableServiceBase;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v1.to.*;
import org.craftercms.studio.api.v1.service.notification.NotificationService;
import org.craftercms.studio.impl.v1.service.StudioCacheContext;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import javax.swing.text.html.parser.ContentModel;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 *
 * @author hyanghee
 *
 */
public class NotificationServiceImpl implements NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);

    /** message type key to match configuration **/
    protected static final String MESSAGE_REJECTION = "rejection";
    protected static final String MESSAGE_REJECTION_NON_PREVIEWABLE = "rejectionNonPreviewable";
    protected static final String MESSAGE_APPROVAL = "approval";
    protected static final String MESSAGE_APPROVAL_NONPREVIEWABLE = "approvalNonPreviewable";
    protected static final String MESSAGE_DELETE_APPROVAL = "deleteApproval";
    protected static final String MESSAGE_CONTENT_SUBMISSION = "contentSubmission";
    protected static final String MESSAGE_CONTENT_NOPREVIEWABLE_SUBMISSION = "contentNoPreviewableSubmission";
    protected static final String MESSAGE_CONTENT_SUBMISSION_FOR_DELETE = "contentSubmissionForDelete";
    protected static final String MESSAGE_CONTENT_NOPREVIEWABLE_SUBMISSION_FOR_DELETE = "contentNoPreviewableSubmissionForDelete";
    protected static final String MESSAGE_CONTENT_DATE_FORMAT = "EEE, d MMM yyyy HH:mm:ss z";
    protected static final String DOCUMENT_EXTERNAL_URL_PROPERTY = "cstudio-core:documentExternalUrl";
    protected EmailMessageQueueTo emailMessages;
    protected String previewBaseUrl=null;
    protected String liveBaseUrl=null;
    protected ServicesConfig servicesConfig;
    protected SiteService siteService;
    protected SecurityService securityService;
    protected ContentService contentService;
    protected String configPath;
    protected String configFileName;
    protected CacheTemplate cacheTemplate;

    public String getPreviewBaseUrl() {
        return previewBaseUrl;
    }

    public void setPreviewBaseUrl(String previewBaseUrl) {
        this.previewBaseUrl = previewBaseUrl;
    }

    public String getLiveBaseUrl() {
        return liveBaseUrl;
    }

    public void setLiveBaseUrl(String liveBaseUrl) {
        this.liveBaseUrl = liveBaseUrl;
    }

    public ServicesConfig getServicesConfig() { return servicesConfig; }
    public void setServicesConfig(ServicesConfig servicesConfig) { this.servicesConfig = servicesConfig; }

    public SiteService getSiteService() { return siteService; }
    public void setSiteService(SiteService siteService) { this.siteService = siteService; }

    public SecurityService getSecurityService() { return securityService; }
    public void setSecurityService(SecurityService securityService) { this.securityService = securityService; }

    public ContentService getContentService() { return contentService; }
    public void setContentService(ContentService contentService) { this.contentService = contentService; }

    public String getConfigPath() { return configPath; }
    public void setConfigPath(String configPath) { this.configPath = configPath; }

    public String getConfigFileName() { return configFileName; }
    public void setConfigFileName(String configFileName) { this.configFileName = configFileName; }

    public CacheTemplate getCacheTemplate() { return cacheTemplate; }
    public void setCacheTemplate(CacheTemplate cacheTemplate) { this.cacheTemplate = cacheTemplate; }

    @Override
    public boolean sendNotice(String site, String action) {
        //checkForUpdate(site);
        NotificationConfigTO config = getNotificationConfig(site);
        if (config != null) {
            Boolean sendNotice = config.getSendNoticeMapping().get(action);
            if (sendNotice != null) {
                return sendNotice.booleanValue();
            }
        }
        // // default to true
        return true;
    }

    protected NotificationConfigTO getNotificationConfig(final String site) {
        CacheService cacheService = cacheTemplate.getCacheService();
        StudioCacheContext cacheContext = new StudioCacheContext(site, true);
        Object cacheKey = cacheTemplate.getKey(site, configPath.replaceFirst(CStudioConstants.PATTERN_SITE, site), configFileName);
        if (!cacheService.hasScope(cacheContext)) {
            cacheService.addScope(cacheContext);
        }
        NotificationConfigTO config = cacheTemplate.getObject(cacheContext, new Callback<NotificationConfigTO>() {
            @Override
            public NotificationConfigTO execute() {
                return loadConfiguration(site);
            }
        }, site, configPath.replaceFirst(CStudioConstants.PATTERN_SITE, site), configFileName);
        return config;
    }

    @Override
    public String getGeneralMessage(String site, String key) {
        //checkForUpdate(site);
        NotificationConfigTO config = getNotificationConfig(site);
        if (config != null) {
            Map<String, String> messages = config.getMessages();
            if (messages != null) {
                return messages.get(key);
            }
        }
        return "";
    }

    @Override
    public List<MessageTO> getCannedRejectionReasons(final String site) {
        //checkForUpdate(site);
        NotificationConfigTO config = getNotificationConfig(site);
        if (config != null) {
            Map<String, List<MessageTO>> messages = config.getCannedMessages();
            if (messages != null) {
                return messages.get(MESSAGE_REJECTION);
            }
        }
        return null;
    }

    public EmailMessageTemplateTO getRejectionEmailMessageTemplate(final String site) {
        //checkForUpdate(site);
        NotificationConfigTO config = getNotificationConfig(site);
        if (config != null) {
            Map<String, EmailMessageTemplateTO> messages = config.getEmailMessageTemplates();
            if (messages != null) {
                return messages.get(MESSAGE_REJECTION);
            }
        }
        return null;
    }

    public EmailMessageTemplateTO getRejectionNonPreviewableEmailMessageTemplate(final String site) {
        //checkForUpdate(site);
        NotificationConfigTO config = getNotificationConfig(site);
        if (config != null) {
            Map<String, EmailMessageTemplateTO> messages = config.getEmailMessageTemplates();
            if (messages != null) {
                return messages.get(MESSAGE_REJECTION_NON_PREVIEWABLE);
            }
        }
        return null;
    }

    public EmailMessageTemplateTO getApprovalEmailMessageTemplate(final String site) {
        //checkForUpdate(site);
        NotificationConfigTO config = getNotificationConfig(site);
        if (config != null) {
            Map<String, EmailMessageTemplateTO> messages = config.getEmailMessageTemplates();
            if (messages != null) {
                return messages.get(MESSAGE_APPROVAL);
            }
        }
        return null;
    }

    public EmailMessageTemplateTO getApprovalNonPreviewableEmailMessageTemplate(final String site) {
        //checkForUpdate(site);
        NotificationConfigTO config = getNotificationConfig(site);
        if (config != null) {
            Map<String, EmailMessageTemplateTO> messages = config.getEmailMessageTemplates();
            if (messages != null) {
                return messages.get(MESSAGE_APPROVAL_NONPREVIEWABLE);
            }
        }
        return null;
    }

    public EmailMessageTemplateTO getDeleteApprovalEmailMessageTemplate(final String site) {
        //checkForUpdate(site);
        NotificationConfigTO config = getNotificationConfig(site);
        if (config != null) {
            Map<String, EmailMessageTemplateTO> messages = config.getEmailMessageTemplates();
            if (messages != null) {
                return messages.get(MESSAGE_DELETE_APPROVAL);
            }
        }
        return null;
    }

    public EmailMessageTemplateTO getContentSubmissionEmailMessageTemplate(final String site) {
        //checkForUpdate(site);
        NotificationConfigTO config = getNotificationConfig(site);
        if (config != null) {
            Map<String, EmailMessageTemplateTO> messages = config.getEmailMessageTemplates();
            if (messages != null) {
                return messages.get(MESSAGE_CONTENT_SUBMISSION);
            }
        }
        return null;
    }

    public EmailMessageTemplateTO getContentSubmissionNoPreviewableEmailMessageTemplate(final String site) {
        //checkForUpdate(site);
        NotificationConfigTO config = getNotificationConfig(site);
        if (config != null) {
            Map<String, EmailMessageTemplateTO> messages = config.getEmailMessageTemplates();
            if (messages != null) {
                return messages.get(MESSAGE_CONTENT_NOPREVIEWABLE_SUBMISSION);
            }
        }
        return null;
    }

    public EmailMessageTemplateTO getContentSubmissionForDeleteEmailMessageTemplate(final String site) {
        //checkForUpdate(site);
        NotificationConfigTO config = getNotificationConfig(site);
        if (config != null) {
            Map<String, EmailMessageTemplateTO> messages = config.getEmailMessageTemplates();
            if (messages != null) {
                return messages.get(MESSAGE_CONTENT_SUBMISSION_FOR_DELETE);
            }
        }
        return null;
    }

    public EmailMessageTemplateTO getContentSubmissionForDeleteNoPreviewableEmailMessageTemplate(final String site) {
        //checkForUpdate(site);
        NotificationConfigTO config = getNotificationConfig(site);
        if (config != null) {
            Map<String, EmailMessageTemplateTO> messages = config.getEmailMessageTemplates();
            if (messages != null) {
                return messages.get(MESSAGE_CONTENT_NOPREVIEWABLE_SUBMISSION_FOR_DELETE);
            }
        }
        return null;
    }

    @Override
    public String getCompleteMessage(final String site, final String key) {
        //checkForUpdate(site);
        NotificationConfigTO config = getNotificationConfig(site);
        if (config != null) {
            Map<String, String> messages = config.getCompleteMessages();
            if (messages != null) {
                return messages.get(key);
            }
        }
        return "";
    }

    @Override
    public String getErrorMessage(String site, String key, Map<String, String> params) {
        //checkForUpdate(site);
        NotificationConfigTO config = getNotificationConfig(site);
        if (config != null) {
            Map<String, String> messages = config.getErrorMessages();
            if (messages != null) {
                String s = messages.get(key);
                for (Map.Entry<String, String> param : params.entrySet()) {
                    s = s.replaceAll("\\$"+param.getKey(),param.getValue());
                }
                return s;
            }
        }
        return "";
    }

    @Override
    public void sendContentSubmissionNotification(String site,String to,String browserUrl,String from,Date scheduledDate,boolean isPreviewable,boolean isDelete) {
        try {
            EmailMessageTemplateTO template = null;
            if (isDelete) {
                if (isPreviewable) {
                    template = getContentSubmissionForDeleteEmailMessageTemplate(site);
                } else {
                    template = getContentSubmissionForDeleteNoPreviewableEmailMessageTemplate(site);
                }
            } else {
                if (isPreviewable) {
                    template = getContentSubmissionEmailMessageTemplate(site);
                } else {
                    template = getContentSubmissionNoPreviewableEmailMessageTemplate(site);
                }
            }

            String subject = "Contributer submitted a content for approval.";
            String message = "Contributer submitted a content for approval.\n";
            if (isDelete) {
                subject = "Contributer submitted a content for delete.";
                message = "Contributer submitted a content for delete.\n";
            }

            if (template != null) {
                subject = template.getSubject();
                message = template.getMessage();
            }
            if(scheduledDate != null) {
                subject += ", At requested [";
                subject += getDateInSpecificTimezone(scheduledDate, site);
                subject += (isDelete? "] (Scheduled for Delete)" : "] (Scheduled Go Live)");
            } else {
                subject += ", As soon as possible ";
            }
            notifyUser(site, to, message, subject, from, browserUrl, "");
        } catch (Exception e) {
            logger.error("Could not queue the content submission notification:",e);
        }
    }

    @Override
    public void sendContentSubmissionNotificationToApprovers(String site, String to, String browserUrl, String from, Date scheduledDate, boolean isPreviewable, boolean isDelete) {
        try {

            NotificationConfigTO config = getNotificationConfig(site);
            if (config != null && config.getSubmitNotificationsMapping() != null) {

                Map<String, String> rules = config.getSubmitNotificationsMapping();
                String toAddress = StringUtils.EMPTY;
                for (String rule : rules.keySet()) {
                    Pattern p = Pattern.compile(rule);
                    Matcher m = p.matcher(browserUrl);
                    if (m.matches()) {
                        toAddress = rules.get(rule);
                        break;
                    }
                }

                if (StringUtils.isNotEmpty(toAddress)) {
                    EmailMessageTemplateTO template = null;
                    if (isDelete) {
                        if (isPreviewable) {
                            template = getContentSubmissionForDeleteEmailMessageTemplate(site);
                        } else {
                            template = getContentSubmissionForDeleteNoPreviewableEmailMessageTemplate(site);
                        }
                    } else {
                        if (isPreviewable) {
                            template = getContentSubmissionEmailMessageTemplate(site);
                        } else {
                            template = getContentSubmissionNoPreviewableEmailMessageTemplate(site);
                        }
                    }

                    String subject = "Contributer submitted a content for approval.";
                    String message = "Contributer submitted a content for approval.\n";
                    if (isDelete) {
                        subject = "Contributer submitted a content for delete.";
                        message = "Contributer submitted a content for delete.\n";
                    }

                    if (template != null) {
                        subject = template.getSubject();
                        message = template.getMessage();
                    }
                    if (scheduledDate != null) {
                        subject += ", At requested [";
                        subject += getDateInSpecificTimezone(scheduledDate, site);
                        subject += (isDelete ? "] (Scheduled for Delete)" : "] (Scheduled Go Live)");
                    } else {
                        subject += ", As soon as possible ";
                    }

                    logger.debug("Notifying user:" + toAddress);

                    if (previewBaseUrl == null) {
                        previewBaseUrl = siteService.getPreviewServerUrl(site);
                    }
                    if (liveBaseUrl == null) {
                        liveBaseUrl = siteService.getLiveServerUrl(site);
                    }


                    Map<String, String> fromProfile = securityService.getUserProfile(from);
                    final String userFirstName = fromProfile.get("firstName");
                    final String userLastName = fromProfile.get("lastName");
                    final String replyTo = fromProfile.get("email");
                    String fromPersonalName = "";
                    if (userFirstName != null) {
                        fromPersonalName = userFirstName + " ";
                    }
                    if (userLastName != null) {
                        fromPersonalName += userLastName;
                    }
                    EmailMessageTO emailMessage= new EmailMessageTO(subject,message,toAddress);
                    emailMessage.setPreviewBaseUrl(previewBaseUrl);
                    emailMessage.setLiveBaseUrl(liveBaseUrl);

                    // reading item internal-name for email title
                    String itemName = "";
                    boolean isDocument = false;
                    boolean isExternalDocument = false;
                    String documentUrl = "";
                    String browserUri = "";
                    ContentItemTO contentItem = contentService.getContentItem(site, browserUrl, 0);
                    if (contentItem != null) {
                        itemName = contentItem.getInternalName();
                        browserUri = contentItem.getBrowserUri();

                        if (contentItem.isPreviewable() && contentItem.isDocument()) {
                            isDocument = true;
                        }
                    }

                    String absolutePath = contentService.expandRelativeSitePath(site, browserUrl);
                    DmPathTO path = new DmPathTO(absolutePath);
                    String name = path.getName();

                    String folderPath = (name.equals(DmConstants.INDEX_FILE)) ? browserUrl.replace("/" + name, "") : browserUrl;
                    String internalName = folderPath;
                    int index = folderPath.lastIndexOf('/');
                    if(index != -1) {
                        internalName = folderPath.substring(index + 1);
                    }
                    internalName = StringUtils.isEmpty(itemName) ? internalName : itemName;
                    emailMessage.setPersonalFromName(fromPersonalName);
                    emailMessage.setTitle(internalName);
                    //emailMessage.setAdminEmail(adminEmailAddress);
                    // set browser URL
                    if (isDocument) {
                        if (isExternalDocument) {
                            emailMessage.setBrowserUrlForExternalDocument(documentUrl);
                        } else {
                            emailMessage.setBrowserUrl(documentUrl);
                        }
                    } else {
                        emailMessage.setBrowserUrl(browserUri);
                    }

                    if(replyTo != null)
                        emailMessage.setReplyTo(replyTo);

                    logger.debug("Queuing notification email request for user:" + toAddress);
                    emailMessages.addEmailMessage(emailMessage);
                } else {
                    sendContentSubmissionNotification(site, to, browserUrl, from, scheduledDate, isPreviewable, isDelete);
                }


            } else {
                sendContentSubmissionNotification(site, to, browserUrl, from, scheduledDate, isPreviewable, isDelete);
            }
        } catch (Exception e) {
            logger.error("Could not queue the content submission notification:",e);
        }
    }

    @Override
    public void sendDeleteApprovalNotification(String site, String to, String browserUrl, String from)
    {
        try {
            EmailMessageTemplateTO template = getDeleteApprovalEmailMessageTemplate(site);
            String subject = "Your Content deletion got Approved.";
            String message = "Admin has approved your request to delete content.\n";
            if (template != null) {
                subject = template.getSubject();
                message = template.getMessage();
            }
            notifyUser(site, to, message, subject, from, browserUrl, "");
        } catch(Exception e) {
            logger.error("Could not queue the content delete approval notification:", e);
        }
    }
    
    public void setEmailMessages(EmailMessageQueueTo emailMessages) {
        this.emailMessages=emailMessages;
    }

    @Override
    public void sendGenericNotification(String site, String path, String to, String from, String key, Map<String,String> params) {
        try {
            EmailMessageTemplateTO template = null;
            NotificationConfigTO config = getNotificationConfig(site);
            if (config != null) {
                Map<String, EmailMessageTemplateTO> messages = config.getEmailMessageTemplates();
                if (messages != null)
                    template = messages.get(key);
            }
            if (template == null) {
                throw new RuntimeException("No email-message-template: " + key);
            } else {
                String subject = template.getSubject();
                String message = template.getMessage();
                if (params != null) {
                    for (String name : params.keySet()) {
                        String value = params.get(name);
                        message = message.replaceAll("\\$" + name, value);
                    }
                }
                notifyUser(site, to, message, subject, from, path, null);
            }
        } catch (Exception e) {
            logger.error("Could not queue the notification:", e);
        }
    }


    protected NotificationConfigTO loadConfiguration(final String site) {
        String configFullPath = configPath.replaceFirst(CStudioConstants.PATTERN_SITE, site);
        configFullPath = configFullPath + "/" + configFileName;
        NotificationConfigTO config = null;
        try {
            Document document = contentService.getContentAsDocument(configFullPath);
            if (document != null) {
                Element root = document.getRootElement();
                config = new NotificationConfigTO();
                Node configNode = root.selectSingleNode("/notification-config");
                if (configNode != null) {
                    loadCannedMessages(config, configNode.selectNodes("canned-messages/messages"));
                    loadEmailMessageTemplates(config, configNode.selectNodes("email-message-templates/email-message-template"));
                    Map<String, String> completeMessages = loadMessages(configNode.selectNodes("complete-messages/message"));
                    Map<String, String> errorMessages = loadMessages(configNode.selectNodes("error-messages/message"));
                    config.setCompleteMessages(completeMessages);
                    config.setErrorMessages(errorMessages);
                    Map<String, String> generalMessages = loadMessages(configNode.selectNodes("general-messages/message"));
                    config.setMessages(generalMessages);
                    Map<String, Boolean> noticeMapping = loadSendNoticeMapping(configNode.selectSingleNode("send-notifications"));
                    config.setSendNoticeMapping(noticeMapping);
                    Map<String, String> submitNotificationRules = loadSubmitNotificationRules(configNode.selectSingleNode("submit-notifications"));
                    config.setSubmitNotificationsMapping(submitNotificationRules);
                    config.setSite(site);
                    config.setLastUpdated(new Date());
                } else {
                    logger.error("Notification config is not found for " + site);
                }
            }
        } catch (Exception ex) {
            logger.error("Notification config is not found for " + site, ex);
        }
        return config;
    }

    protected Map<String,String> loadSubmitNotificationRules(Node node) {
        Map<String, String> submitNotificationMapping = new HashMap<String, String>();
        if (node != null) {
            Element element = (Element) node;
            List<Element> childElements = element.elements();

            if (childElements != null && childElements.size() > 0) {
                for (Element childElement : childElements) {
                    String regex = childElement.attributeValue("regex");
                    Node addressNode = childElement.selectSingleNode("email");
                    String value = addressNode.getText();
                    // default to true
                    //Boolean sendNotice = (!StringUtils.isEmpty(value) && value.equals("false")) ? false : true;
                    if (!StringUtils.isEmpty(regex) && !StringUtils.isEmpty(value)) {
                        submitNotificationMapping.put(regex, value);
                    }
                }
            }
        }
        return submitNotificationMapping;
    }


    /**
     * load send notice mapping
     *
     * @param node
     * @return
     */
    protected Map<String, Boolean> loadSendNoticeMapping(Node node) {
        Map<String, Boolean> noticeMapping = new HashMap<String, Boolean>();
        if (node != null) {
            Element element = (Element) node;
            List<Element> childElements = element.elements();
            if (childElements != null && childElements.size() > 0) {
                for (Element childElement : childElements) {
                    String action = childElement.getName();
                    String value = childElement.getText();
                    // default to true
                    Boolean sendNotice = (!StringUtils.isEmpty(value) && value.equals("false")) ? false : true;
                    if (!StringUtils.isEmpty(action)) {
                        noticeMapping.put(action, sendNotice);
                    }
                }
            }
        }
        return noticeMapping;
    }

    /**
     * load messages from the given nodes
     *
     * 			notification config to store messages
     * @param nodes
     * 			message nodes
     */
    protected Map<String, String> loadMessages(final List<Node> nodes) {
        if (nodes != null) {
            Map<String, String> messagesMap = new HashMap<String, String>();
            for (Node node : nodes) {
                String name = node.valueOf("@name");
                if (!StringUtils.isEmpty(name)) {
                    String message = node.getText();
                    messagesMap.put(name, message);
                }
            }
            return messagesMap;
        }
        return null;
    }


    /**
     * load canned messages from the configuration file
     *
     * @param config
     * 			notification config to store messages
     * @param nodes
     * 			canned messages nodes
     * @return a list of canned messages
     */
    @SuppressWarnings("unchecked")
    protected void loadCannedMessages(final NotificationConfigTO config, final List<Node> nodes) {
        if (nodes != null) {
            Map<String, List<MessageTO>> messageMap = new HashMap<String, List<MessageTO>>();
            for (Node listNode : nodes) {
                String name = listNode.valueOf("@name");
                if (!StringUtils.isEmpty(name)) {
                    List<Node> messageNodes = listNode.selectNodes("message");
                    if (messageNodes != null) {
                        List<MessageTO> messages = new ArrayList<MessageTO>(messageNodes.size());
                        for (Node messageNode : messageNodes) {
                            MessageTO message = new MessageTO();
                            message.setTitle(messageNode.valueOf("title"));
                            message.setBody(messageNode.valueOf("body"));
                            messages.add(message);
                        }
                        messageMap.put(name, messages);
                    }
                }
            }
            config.setCannedMessages(messageMap);
        }
    }

    protected void loadEmailMessageTemplates(final NotificationConfigTO config, final List<Node> nodes) {
        if (nodes != null) {
            Map<String, EmailMessageTemplateTO> messageMap = new HashMap<String, EmailMessageTemplateTO>();
            for (Node listNode : nodes) {
                String name = listNode.valueOf("@name");
                if (!StringUtils.isEmpty(name)) {
                    EmailMessageTemplateTO message = new EmailMessageTemplateTO();
                    message.setSubject(listNode.valueOf("Subject"));
                    message.setMessage(listNode.valueOf("message"));
                    messageMap.put(name, message);
                }
            }
            config.setEmailMessageTemplates(messageMap);
        }
    }
 

    @Override
    public void sendRejectionNotification(String site,String to,String browserUrl,String reason,String from, boolean isPreviewable) {
        try {
            EmailMessageTemplateTO template = null;
            if (isPreviewable) {
                template = getRejectionEmailMessageTemplate(site);
            } else {
                template = this.getRejectionNonPreviewableEmailMessageTemplate(site);
            }

            String subject="Your content has got rejected.";
            String message="Your content has been rejected for following reason(s).\n";
            if(template != null)
            {
                subject=template.getSubject();
                message=template.getMessage();
            }
            if (StringUtils.isNotEmpty(reason)) {
                message += "\n\n" + reason;
            }
            notifyUser(site,to,message,subject,from,browserUrl, reason);
        } catch(Exception e) {
            logger.error("Could not queue the rejection notification:",e);
        }
    }

 
    @Override
    public void sendApprovalNotification(String site, String to, String browserUrl, String from) {
        try
        {
            logger.debug("Sending approval notification to:" + to);
            boolean isPreviewable = true;
            try {
                ContentItemTO contentItem = contentService.getContentItem(site, browserUrl);
                isPreviewable = contentItem.isPreviewable();
            } catch (Exception e) {
                logger.error("during Notification send item name read failed",e);
            }

            EmailMessageTemplateTO template = null;
            if (isPreviewable) {
                template = getApprovalEmailMessageTemplate(site);
            } else {
                template = getApprovalNonPreviewableEmailMessageTemplate(site);
            }
            String subject = "Your content has got the approval.";
            String message = "Your content has been approved.\n";
            if(template != null) {
                subject = template.getSubject();
                message = template.getMessage();
            }
            notifyUser(site,to,message,subject,from,browserUrl, "");
        } catch(Exception e) {
            logger.error("Could not queue the approval notification:",e);
        }
    }


    protected void notifyUser(final String site, final String toUser,final String content, final String subject,final String fromUser,String relativeUrl, String rejectReason) {
        logger.debug("Notifying user:" + toUser);
        if (StringUtils.isEmpty(toUser)) {
            logger.error("to User is empty or Null, not sending any email");
            return;
        }
        if (previewBaseUrl == null) {
            previewBaseUrl = siteService.getPreviewServerUrl(site);
        }
        if (liveBaseUrl == null) {
            liveBaseUrl = siteService.getLiveServerUrl(site);
        }
        String adminEmailAddress = siteService.getAdminEmailAddress(site);
        String userEmailAddress="";
        /*
        if(toUser.equals(AuthenticationUtil.getAdminUserName()))
            userEmailAddress=adminEmailAddress;
*/

        if(StringUtils.isEmpty(userEmailAddress)) {
            Map<String, String> profile = securityService.getUserProfile(toUser);
            if (profile != null) {
                userEmailAddress = profile.get("email");
            }
        }

        if (StringUtils.isEmpty(userEmailAddress)) {
            logger.error("Not able to find valid email address for user " + toUser + ", not sending any email");
            return;
        }

        Map<String, String> fromProfile = securityService.getUserProfile(fromUser);
        final String userFirstName = fromProfile.get("firstName");
        final String userLastName = fromProfile.get("lastName");
        final String replyTo = fromProfile.get("email");
        String fromPersonalName = "";
        if (userFirstName != null) {
            fromPersonalName = userFirstName + " ";
        }
        if (userLastName != null) {
            fromPersonalName += userLastName;
        }
        EmailMessageTO emailMessage= new EmailMessageTO(subject,content,userEmailAddress);
        emailMessage.setPreviewBaseUrl(previewBaseUrl);
        emailMessage.setLiveBaseUrl(liveBaseUrl);
        if (StringUtils.isNotEmpty(rejectReason)) {
            emailMessage.setRejectReason(rejectReason);
        }

        // reading item internal-name for email title
        String itemName = "";
        boolean isDocument = false;
        boolean isExternalDocument = false;
        String documentUrl = "";
        String browserUri = "";
        ContentItemTO contentItem = contentService.getContentItem(site, relativeUrl, 0);
        if (contentItem != null) {
            itemName = contentItem.getInternalName();
            browserUri = contentItem.getBrowserUri();

            if (contentItem.isPreviewable() && contentItem.isDocument()) {
                isDocument = true;
                String documentUrlProperty = "";
                /*
                documentUrlProperty = dmContentService.getTextPropertyByRelativePath(site, relativeUrl, DOCUMENT_EXTERNAL_URL_PROPERTY);
                if (StringUtils.isEmpty(documentUrlProperty)) { // attached asset present
                    List<DmContentItemTO> assets = contentItem.getAssets();
                    // expecting first asset
                    if (assets.size() > 0) {
                        DmContentItemTO docAsset = assets.get(0);
                        if (docAsset != null) {
                            documentUrl = docAsset.getUri();
                        }
                    }
                } else { // external link
                    isExternalDocument = true;
                    documentUrl = documentUrlProperty;
                }*/
            }
        }

        String absolutePath = contentService.expandRelativeSitePath(site, relativeUrl);
        DmPathTO path = new DmPathTO(absolutePath);
        String name = path.getName();

        String folderPath = (name.equals(DmConstants.INDEX_FILE)) ? relativeUrl.replace("/" + name, "") : relativeUrl;
        String internalName = folderPath;
        int index = folderPath.lastIndexOf('/');
        if(index != -1) {
            internalName = folderPath.substring(index + 1);
        }
        internalName = StringUtils.isEmpty(itemName) ? internalName : itemName;
        emailMessage.setPersonalFromName(fromPersonalName);
        emailMessage.setTitle(internalName);
        emailMessage.setAdminEmail(adminEmailAddress);
        // set browser URL
        if (isDocument) {
            if (isExternalDocument) {
                emailMessage.setBrowserUrlForExternalDocument(documentUrl);
            } else {
                emailMessage.setBrowserUrl(documentUrl);
            }
        } else {
            emailMessage.setBrowserUrl(browserUri);
        }

        if(replyTo != null)
            emailMessage.setReplyTo(replyTo);

        logger.debug("Queuing notification email request for user:" + userEmailAddress);
        emailMessages.addEmailMessage(emailMessage);
    }

    protected String getDateInSpecificTimezone(Date dt, String site) {
        String ret = "";
        try {
            SimpleDateFormat fmt = new SimpleDateFormat(MESSAGE_CONTENT_DATE_FORMAT);
            fmt.setTimeZone(TimeZone.getTimeZone(servicesConfig.getDefaultTimezone(site)));
            ret = (fmt.format(dt)).toString();
        } catch (Exception e) {
            logger.error("Date cannot be converted", e);
        }

        return ret;
    }

    @Override
    public void reloadConfiguration(String site) {
        CacheService cacheService = cacheTemplate.getCacheService();
        StudioCacheContext cacheContext = new StudioCacheContext(site, true);
        Object cacheKey = cacheTemplate.getKey(site, configPath.replaceFirst(CStudioConstants.PATTERN_SITE, site), configFileName);
        if (cacheService.hasScope(cacheContext)) {
            cacheService.remove(cacheContext, cacheKey);
        } else {
            cacheService.addScope(cacheContext);
        }
        NotificationConfigTO config = loadConfiguration(site);
        cacheService.put(cacheContext, cacheKey, config);
    }

}
