/*
 * Copyright (C) 2007-2025 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.impl.v2.content;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.script.ScriptExecutor;
import org.craftercms.studio.api.v2.content.ContentLifecycle;
import org.craftercms.studio.api.v2.content.ContentLoader;
import org.craftercms.studio.api.v2.content.LifecycleContent;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.beans.ConstructorProperties;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static com.rometools.utils.Strings.isEmpty;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.regex.Matcher.quoteReplacement;
import static org.apache.commons.io.FilenameUtils.directoryContains;
import static org.apache.commons.lang3.ArrayUtils.nullToEmpty;
import static org.craftercms.studio.api.v1.constant.DmConstants.*;
import static org.craftercms.studio.api.v1.constant.StudioConstants.*;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_SITE_CONTENT_TYPES_CONFIG_BASE_PATH;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONTENT_PROCESSOR_CONTENT_LIFE_CYCLE_SCRIPT_LOCATION;
import static org.craftercms.studio.impl.v2.utils.security.SecurityUtils.getCurrentUsername;

/**
 * Content processing implementation of the {@link ContentLifecycle} interface.
 * Executes the content lifecycle script (if exists) with the given parameters.
 */
public class ContentLifecycleImpl implements ContentLifecycle, ApplicationContextAware {
	private static final Logger logger = LoggerFactory.getLogger(ContentLifecycleImpl.class);

	private static final String SCRIPT_LOGGER_NAME_FORMAT = "Lifecycle-%s-%s";

	protected final StudioConfiguration studioConfiguration;
	protected final ScriptExecutor scriptExecutor;
	protected ApplicationContext applicationContext;

	@ConstructorProperties({"studioConfiguration", "scriptExecutor"})
	public ContentLifecycleImpl(final StudioConfiguration studioConfiguration, final ScriptExecutor scriptExecutor) {
		this.studioConfiguration = studioConfiguration;
		this.scriptExecutor = scriptExecutor;
	}

	@Override
	public void execute(String siteId, LifecycleContent lifecycleContent, ContentLoader contentLoader) throws ServiceLayerException {
		String contentType = lifecycleContent.getContentType();
		String repoPath = lifecycleContent.getRepoPath();

		logger.debug("Executing content lifecycle for site '{}' path '{}' contentType '{}' operation '{}'.",
				siteId, repoPath, contentType, lifecycleContent.getOperation());

		// Validate contentType param
		if (isEmpty(contentType) || CONTENT_TYPE_UNKNOWN.equals(contentType)) {
			logger.warn("No content type provided for site '{}' path '{}'. Skipping script execution.", siteId, repoPath);
			return;
		}

		String scriptPath = getScriptPath(siteId, contentType);
		// Validate script path
		String contentTypesRoot = studioConfiguration.getProperty(CONFIGURATION_SITE_CONTENT_TYPES_CONFIG_BASE_PATH);
		if (!directoryContains(contentTypesRoot, scriptPath)) {
			logger.error(format("Invalid script path '%s' for site '%s' content-type '%s'." +
					" The content type script must be a valid path under '%s'.", scriptPath, siteId, contentType, contentTypesRoot));
			return;
		}
		String script;
		// Check if the script exists
		try (InputStream content = contentLoader.getContentRaw(siteId, scriptPath)) {
			if (content == null) {
				logger.warn("No content lifecycle script found for site '{}' path '{}' contentType '{}'. Skipping content lifecycle.", siteId, repoPath, contentType);
				return;
			}
			script = IOUtils.toString(content, UTF_8);
		} catch (IOException e) {
			logger.error("Failed to load controller script for site '{}' path '{}' contentType '{}'", siteId, repoPath, contentType, e);
			throw new ServiceLayerException(format("Failed to load controller script for site '%s' path '%s'  contentType '%s'.", siteId, repoPath, contentType), e);
		}

		if (isEmpty(script)) {
			logger.warn("Empty lifecycle script found for site '{}' path '{}' contentType '{}'. Skipping content lifecycle.", siteId, repoPath, contentType);
			return;
		}

		// Build model
		Map<String, Object> model = buildModel(siteId, lifecycleContent, contentLoader);

		// Execute the script
		try {
			logger.debug("Executing content lifecycle script for site '{}' path '{}' contentType '{}' operation '{}'.", siteId, repoPath, contentType, lifecycleContent.getOperation());
			scriptExecutor.executeScriptString(siteId, script, model);
			logger.debug("Content lifecycle script executed successfully for site '{}' path '{}' contentType '{}' operation '{}'.", siteId, repoPath, contentType, lifecycleContent.getOperation());
		} catch (Exception e) {
			throw new ServiceLayerException(format("Failed to execute content lifecycle script for site '%s' path '%s' contentType '%s'.", siteId, repoPath, contentType), e);
		}
	}

	/**
	 * Builds the model to be passed to the script.
	 *
	 * @param siteId           the site id
	 * @param lifecycleContent the {@link LifecycleContent} object to enable the controller
	 *                         to alter the content
	 * @return a map with the model to be passed to the script
	 */
	protected Map<String, Object> buildModel(String siteId, LifecycleContent lifecycleContent, ContentLoader contentLoader) {
		Map<String, Object> model = new HashMap<>();
		model.put(KEY_SITE, siteId);
		model.put(KEY_USER, getCurrentUsername());
		model.put(KEY_PATH, lifecycleContent.getRepoPath());
		model.put(KEY_CONTENT_TYPE, lifecycleContent.getContentType());
		model.put(CONTENT_LIFECYCLE_OPERATION, lifecycleContent.getOperation().toString());
		model.put(KEY_CONTENT_LOADER, contentLoader);

		model.put(KEY_SOURCE_PATH, lifecycleContent.getSourcePath());

		model.put(KEY_LIFECYCLE_CONTENT, lifecycleContent);

		model.put(KEY_LOGGER, LoggerFactory.getLogger(format(SCRIPT_LOGGER_NAME_FORMAT, siteId, lifecycleContent.getContentType())));

		if (shouldIncludeApplicationContext()) {
			model.put(KEY_APPLICATION_CONTEXT, applicationContext);
		}
		addSpringBeans(model);

		return model;
	}

	protected void addSpringBeans(Map<String, Object> model) {
		String[] enabledBeans = nullToEmpty(studioConfiguration.getArray(CONTENT_LIFECYCLE_INCLUDED_BEANS, String.class));
		for (String beanName : enabledBeans) {
			try {
				Object bean = applicationContext.getBean(beanName);
				model.put(beanName, bean);
			} catch (NoSuchBeanDefinitionException e) {
				logger.error("Bean '{}' not found in application context. Skipping.", beanName);
			} catch (Exception e) {
				logger.error("Error while adding bean '{}' to model. Skipping.", beanName, e);
			}
		}
	}

	protected boolean shouldIncludeApplicationContext() {
		return studioConfiguration.getProperty(CONTENT_LIFECYCLE_INCLUDE_APPLICATION_CONTEXT, Boolean.class, false);
	}

	/**
	 * Get the controller script path for the given site and content type.
	 */
	protected String getScriptPath(String site, String contentType) {
		return studioConfiguration.getProperty(CONTENT_PROCESSOR_CONTENT_LIFE_CYCLE_SCRIPT_LOCATION)
				.replaceAll(PATTERN_SITE, quoteReplacement(site))
				.replaceAll(PATTERN_CONTENT_TYPE, quoteReplacement(contentType))
				.transform(FilenameUtils::normalize);
	}

	@Override
	public void setApplicationContext(@NonNull final ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
}
