/*
 * Copyright (C) 2007-2026 Crafter Software Corporation. All Rights Reserved.
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
import java.util.ArrayList;
import java.util.Collection;
import static java.util.Collections.singletonList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.craftercms.commons.mail.EmailUtils;
import org.craftercms.commons.validation.annotations.param.ValidateStringParam;
import static org.craftercms.studio.api.v1.constant.SecurityConstants.KEY_EMAIL;
import static org.craftercms.studio.api.v1.constant.SecurityConstants.KEY_EXTERNALLY_MANAGED;
import static org.craftercms.studio.api.v1.constant.SecurityConstants.KEY_FIRSTNAME;
import static org.craftercms.studio.api.v1.constant.SecurityConstants.KEY_LASTNAME;
import static org.craftercms.studio.api.v1.constant.SecurityConstants.KEY_USERNAME;
import static org.craftercms.studio.api.v1.constant.StudioConstants.MODULE_STUDIO;
import static org.craftercms.studio.api.v1.constant.StudioXmlConstants.DOCUMENT_ELEMENT_APPROVER_EMAILS;
import static org.craftercms.studio.api.v1.constant.StudioXmlConstants.DOCUMENT_ELEMENT_DEPLOYMENT_FAILURE_NOTIFICATION;
import static org.craftercms.studio.api.v1.constant.StudioXmlConstants.DOCUMENT_ELEMENT_EMAIL_TEMPLATES;
import static org.craftercms.studio.api.v1.constant.StudioXmlConstants.DOCUMENT_ELEMENT_REPOSITORY_MERGE_CONFLICT_NOTIFICATION;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v1.to.EmailMessageQueueTo;
import org.craftercms.studio.api.v1.to.EmailMessageTO;
import org.craftercms.studio.api.v1.to.EmailMessageTemplateTO;
import org.craftercms.studio.api.v1.to.NotificationConfigTO;
import org.craftercms.studio.api.v2.dal.User;
import org.craftercms.studio.api.v2.dal.item.ContentItem;
import org.craftercms.studio.api.v2.dal.publish.PublishItem;
import org.craftercms.studio.api.v2.dal.publish.PublishPackage;
import org.craftercms.studio.api.v2.service.config.ConfigurationService;
import org.craftercms.studio.api.v2.service.content.ContentService;
import org.craftercms.studio.api.v2.service.notification.NotificationService;
import org.craftercms.studio.api.v2.service.security.UserService;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_ENVIRONMENT_ACTIVE;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.NOTIFICATION_CONFIGURATION_FILE;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.NOTIFICATION_TIMEZONE;
import static org.craftercms.studio.api.v2.utils.StudioUtils.isPageDescriptor;
import static org.craftercms.studio.impl.v1.util.ContentUtils.getPreviewUrl;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;

import freemarker.core.HTMLOutputFormat;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapperBuilder;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import jakarta.validation.Valid;

public class NotificationServiceImpl implements NotificationService {
	private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);

	private static final String NOTIFICATION_KEY_DEPLOYMENT_ERROR = "deploymentError";
	private static final String NOTIFICATION_KEY_CONTENT_APPROVED = "contentApproved";
	private static final String NOTIFICATION_KEY_SUBMITTED_FOR_REVIEW = "submittedForReview";
	private static final String NOTIFICATION_KEY_CONTENT_REJECTED = "contentRejected";
	private static final String NOTIFICATION_KEY_REPOSITORY_MERGE_CONFLICT = "repositoryMergeConflict";

	private static final String TEMPLATE_MODEL_DEPLOYMENT_ERROR = "deploymentError";
	private static final String TEMPLATE_MODEL_FILES = "files";
	private static final String TEMPLATE_MODEL_PACKAGE = "publishPackage";
	private static final String TEMPLATE_MODEL_SUBMITTER = "submitter";
	private static final String TEMPLATE_MODEL_REVIEWER = "reviewer";
	private static final String TEMPLATE_MODEL_SITE_NAME = "siteName";
	private static final String TEMPLATE_MODEL_LIVE_URL = "liveUrl";
	private static final String TEMPLATE_MODEL_AUTHORING_URL = "authoringUrl";

	private static final String MESSAGE_ELEMENT_SUBJECT = "subject";
	private static final String MESSAGE_ELEMENT_BODY = "body";
	private static final String MESSAGE_ATTRIBUTE_KEY = "key";

	protected ContentService contentService;
	protected EmailMessageQueueTo emailMessages;
	protected ServicesConfig servicesConfig;
	protected UserService userService;
	private Configuration configuration;
	protected StudioConfiguration studioConfiguration;
	protected ConfigurationService configurationService;

	protected Cache<String, NotificationConfigTO> cache;

	public void init() {
		configuration = new Configuration(Configuration.VERSION_2_3_23);
		configuration.setTimeZone(TimeZone.getTimeZone(getTemplateTimezone()));
		configuration.setObjectWrapper(new DefaultObjectWrapperBuilder(Configuration.VERSION_2_3_23).build());
		configuration.setOutputFormat(HTMLOutputFormat.INSTANCE);
	}

	@Override
	public void notifyPublishError(final PublishPackage publishPackage, final Throwable throwable,
				       final Collection<PublishItem> filesUnableToPublish) {
		String siteId = publishPackage.getSite().getSiteId();
		try {
			final NotificationConfigTO notificationConfig = getNotificationConfig(siteId);
			final Map<String, Object> templateModel = new HashMap<>();
			templateModel.put(TEMPLATE_MODEL_PACKAGE, publishPackage);
			templateModel.put(TEMPLATE_MODEL_DEPLOYMENT_ERROR, ExceptionUtils.getStackTrace(throwable));
			templateModel.put(TEMPLATE_MODEL_FILES, filesUnableToPublish);
			notify(siteId, notificationConfig.getDeploymentFailureNotifications(), NOTIFICATION_KEY_DEPLOYMENT_ERROR,
				templateModel);
		} catch (Throwable e) {
			logger.error("Failed to send publishing error notification for site '{}'", siteId, e);
		}
	}

	@Override
	public void notifyPackageApproval(final PublishPackage publishPackage, final Collection<String> paths) {
		String siteId = publishPackage.getSite().getSiteId();
		logger.debug("Sending content approval notification for site '{}', package '{}'", siteId, publishPackage.getId());
		try {
			String submitterUsername = publishPackage.getSubmitter().getUsername();
			final Map<String, Object> submitterUser = getUserProfile(submitterUsername);
			Map<String, Object> templateModel = new HashMap<>();
			templateModel.put(TEMPLATE_MODEL_PACKAGE, publishPackage);
			templateModel.put(TEMPLATE_MODEL_FILES, convertPathsToContent(siteId, paths));
			templateModel.put(TEMPLATE_MODEL_REVIEWER, publishPackage.getReviewer());

			notify(siteId, singletonList(submitterUser.get(KEY_EMAIL).toString()), NOTIFICATION_KEY_CONTENT_APPROVED,
				templateModel);
		} catch (UserNotFoundException e) {
			logger.error("Failed to send content approval notification because user was not found", e);
		} catch (Throwable e) {
			logger.error("Failed to send content approval notification for site '{}'", siteId, e);
		}
	}

	@Override
	public void notifyPackageRejection(PublishPackage publishPackage, Collection<String> paths) {
		String siteId = publishPackage.getSite().getSiteId();
		logger.debug("Sending content rejection notification for site '{}', package '{}'", siteId, publishPackage.getId());
		try {
			Map<String, Object> submitterUser = getUserProfile(publishPackage.getSubmitter().getUsername());
			Map<String, Object> templateModel = new HashMap<>();
			templateModel.put(TEMPLATE_MODEL_PACKAGE, publishPackage);
			templateModel.put(TEMPLATE_MODEL_FILES, convertPathsToContent(siteId, paths));
			templateModel.put(TEMPLATE_MODEL_REVIEWER, publishPackage.getReviewer());

			String email = submitterUser.get(KEY_EMAIL).toString();
			notify(siteId, List.of(email), NOTIFICATION_KEY_CONTENT_REJECTED, templateModel);
		} catch (UserNotFoundException e) {
			logger.error("Failed to send content rejection notification because user was not found", e);
		} catch (Exception e) {
			logger.error("Failed to send content rejection notification for site '{}'", siteId, e);
		}
	}

	protected Map<String, Object> getUserProfile(String username)
		throws ServiceLayerException, UserNotFoundException {
		Map<String, Object> toRet = new HashMap<>();
		User user = userService.getUserByIdOrUsername(-1, username);
		toRet.put(KEY_USERNAME, username);
		toRet.put(KEY_FIRSTNAME, user.getFirstName());
		toRet.put(KEY_LASTNAME, user.getLastName());
		toRet.put(KEY_EMAIL, user.getEmail());
		toRet.put(KEY_EXTERNALLY_MANAGED, user.isExternallyManaged());
		return toRet;
	}

	@Override
	public void notifyPackageSubmission(final PublishPackage publishPackage, final Collection<String> paths) {
		String siteId = publishPackage.getSite().getSiteId();
		logger.debug("Sending content submission notification for site '{}', package '{}'", siteId, publishPackage.getId());
		try {
			final NotificationConfigTO notificationConfig = getNotificationConfig(siteId);
			Map<String, Object> templateModel = new HashMap<>();
			templateModel.put(TEMPLATE_MODEL_PACKAGE, publishPackage);
			templateModel.put(TEMPLATE_MODEL_FILES, convertPathsToContent(siteId, paths));
			templateModel.put(TEMPLATE_MODEL_SUBMITTER, publishPackage.getSubmitter());

			notify(siteId, notificationConfig.getApproverEmails(), NOTIFICATION_KEY_SUBMITTED_FOR_REVIEW,
				templateModel);
		} catch (Throwable e) {
			logger.error("Failed to send content submission notification for site '{}'", siteId, e);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	@Valid
	public void notify(@ValidateStringParam final String site, final Collection<String> toUsers,
			   @ValidateStringParam final String key,
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
				logger.error("Failed to find key '{}' in site '{}'", key, site);
			}
		} catch (Throwable e) {
			logger.error("Failed to send notification with key '{}' for site '{}'", key, site, e);
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
		} catch (Throwable e) {
			logger.error("Failed to send notification with key '{}' for site '{}'", key, site, e);
		}
	}

	protected NotificationConfigTO loadConfig(final String site) throws SiteNotFoundException {
		var environment = studioConfiguration.getProperty(CONFIGURATION_ENVIRONMENT_ACTIVE);
		var configPath = getConfigPath();
		var cacheKey = configurationService.getCacheKey(site, MODULE_STUDIO, configPath, environment, "object");

		try {
			NotificationConfigTO config = cache.getIfPresent(cacheKey);
			if (Objects.isNull(config)) {
				logger.debug("Cache miss for key '{}' while loading notification config in site '{}'",
					cacheKey, site);

				config = new NotificationConfigTO();
				Document document =
					configurationService.getConfigurationAsDocument(site, MODULE_STUDIO, configPath, environment);
				Element root = document.getRootElement();

				if (root.getNodeType() == Node.ELEMENT_NODE) {
					loadEmailTemplates((Element) root.selectSingleNode(DOCUMENT_ELEMENT_EMAIL_TEMPLATES),
						config.getEmailMessageTemplates());

					String adminEmailAddress = getAdminEmailAddress(site);
					loadEmailList(site, (Element) root.selectSingleNode(DOCUMENT_ELEMENT_DEPLOYMENT_FAILURE_NOTIFICATION),
						config.getDeploymentFailureNotifications(), adminEmailAddress);
					loadEmailList(site, (Element) root.selectSingleNode(DOCUMENT_ELEMENT_APPROVER_EMAILS),
						config.getApproverEmails(), adminEmailAddress);
					loadEmailList(site, (Element) root.selectSingleNode(DOCUMENT_ELEMENT_REPOSITORY_MERGE_CONFLICT_NOTIFICATION),
						config.getRepositoryMergeConflictNotifications(), adminEmailAddress);
				} else {
					logger.info("Failed to execute against non-XML-element '{}' in site '{}'",
						root.getUniquePath(), site);
				}

				cache.put(cacheKey, config);
			}
			return config;
		} catch (Exception e) {
			logger.error("Failed to load notification configuration for site '{}' from path '{}'",
				site, getConfigPath(), e);
			return null;
		}
	}

	private void loadEmailList(final String site, final Element emailListElement,
				   final List<String> emailList, final String defaultEmail) {
		if (emailListElement == null) {
			logger.error("Failed to load email list in site '{}'", site);
			return;
		}
		List<Element> emails = emailListElement.elements();
		if (emails.isEmpty()) {
			emailList.add(defaultEmail);
			return;
		}
		for (Element emailNode : emails) {
			final String email = emailNode.getText();
			if (EmailUtils.validateEmail(email)) {
				emailList.add(email);
			} else {
				logger.warn("Invalid email address '{}' in site '{}' in notification config '{}' element", email, site, emailListElement.getName());
			}
		}
	}

	private String getAdminEmailAddress(final String site) throws SiteNotFoundException {
		String adminEmail = servicesConfig.getAdminEmailAddress(site);
		if (EmailUtils.validateEmail(adminEmail)) {
			return adminEmail;
		}
		logger.warn("Invalid admin email address '{}' configured for site '{}'", adminEmail, site);
		return null;
	}

	protected void loadEmailTemplates(final Element emailTemplates,
					  final Map<String, EmailMessageTemplateTO> messageContainer) {
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
						logger.error("Failed to load email templates, message malformed");
					}
				}
			} else {
				logger.error("Failed to load email templates, messages element is empty");
			}
		} else {
			logger.error("Failed to load email templates, the email template element was not found");
		}
	}

	protected NotificationConfigTO getNotificationConfig(final String site) throws SiteNotFoundException {
		return loadConfig(site);
	}


	protected void sendEmail(final String message, final String subject, final Collection<String> sendTo) {
		EmailMessageTO emailMessage = new EmailMessageTO(subject, message, StringUtils.join(sendTo, ','));
		if (logger.isDebugEnabled()) {
			logger.debug("Sending email message to '{}'. Subject '{}'. Content: '{}'", sendTo, subject, message);
		}
		emailMessages.addEmailMessage(emailMessage);
	}

	protected String processMessage(final String templateName, final String message, final Map<String, Object>
		templateModel) {
		StringWriter out = new StringWriter();
		try {
			Template t = new Template(templateName, new StringReader(message), configuration);
			t.process(templateModel, out);
			return out.toString();
		} catch (TemplateException | IOException e) {
			logger.error("Failed to process notification message with template '{}'", templateName, e);
		}
		return null;
	}

	protected Collection<NotificationContentItem> convertPathsToContent(final String site, final Collection<String> listOfPaths) throws UserNotFoundException, ServiceLayerException {
		List<NotificationContentItem> notificationItems = new ArrayList<>(listOfPaths.size());
		List<ContentItem> contentItems = contentService.getContentItemsByPath(site, listOfPaths, true);

		for (ContentItem contentItem : contentItems) {
			NotificationContentItem item = new NotificationContentItem(isPageDescriptor(contentItem.getPath()),
					contentItem.getLabel(),
					getPreviewUrl(servicesConfig, site, contentItem.getPath())
			);
			notificationItems.add(item);
		}
		return notificationItems;
	}

	@Override
	@Valid
	public void notifyRepositoryMergeConflict(@ValidateStringParam final String site,
						  final List<String> filesUnableToMerge) {
		try {
			final NotificationConfigTO notificationConfig = getNotificationConfig(site);
			final Map<String, Object> templateModel = new HashMap<>();
			templateModel.put(TEMPLATE_MODEL_FILES, filesUnableToMerge);
			notify(site, notificationConfig.getRepositoryMergeConflictNotifications(),
				NOTIFICATION_KEY_REPOSITORY_MERGE_CONFLICT, templateModel);
		} catch (Throwable e) {
			logger.error("Failed to notify on repository merge conflict in site '{}'", site, e);
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

	@SuppressWarnings("unused")
	public void setEmailMessages(final EmailMessageQueueTo emailMessages) {
		this.emailMessages = emailMessages;
	}

	public void setServicesConfig(final ServicesConfig servicesConfig) {
		this.servicesConfig = servicesConfig;
	}

	public void setUserService(final UserService userService) {
		this.userService = userService;
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

	/**
	 * Simple class representing content items in notifications. It contains only the information needed for notifications
	 */
	public static class NotificationContentItem {
		private final boolean page;
		private final String label;
		private final String browserUri;

		/**
		 * @param page       indicates if the content item is a page descriptor or not
		 * @param label      item label
		 * @param browserUri for pages, item URI to be used in notifications
		 */
		public NotificationContentItem(boolean page, String label, String browserUri) {
			this.page = page;
			this.label = label;
			this.browserUri = browserUri;
		}

		public boolean isPage() {
			return page;
		}

		public String getInternalName() {
			return label;
		}

		public String getName() {
			return label;
		}

		public String getBrowserUri() {
			return browserUri;
		}
	}

}

