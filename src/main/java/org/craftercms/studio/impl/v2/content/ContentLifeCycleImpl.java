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

import org.apache.commons.io.IOUtils;
import org.craftercms.studio.api.v1.constant.DmConstants;
import org.craftercms.studio.api.v1.constant.StudioConstants;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.script.ScriptExecutor;
import org.craftercms.studio.api.v2.content.ContentLifeCycle;
import org.craftercms.studio.api.v2.content.ContentLoader;
import org.craftercms.studio.api.v2.content.LifecycleContent;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.script.ScriptException;
import java.beans.ConstructorProperties;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static com.rometools.utils.Strings.isEmpty;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.craftercms.studio.api.v1.constant.DmConstants.*;
import static org.craftercms.studio.api.v1.constant.DmConstants.KEY_CONTENT_TYPE;
import static org.craftercms.studio.api.v1.constant.StudioConstants.*;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONTENT_PROCESSOR_CONTENT_LIFE_CYCLE_SCRIPT_LOCATION;
import static org.craftercms.studio.impl.v2.utils.security.SecurityUtils.getCurrentUsername;

/**
 * Default implementation of the {@link ContentLifeCycle} interface.
 */
public class ContentLifeCycleImpl implements ContentLifeCycle, ApplicationContextAware {
	private static final Logger logger = LoggerFactory.getLogger(ContentLifeCycleImpl.class);

	protected final StudioConfiguration studioConfiguration;
	protected final ScriptExecutor scriptExecutor;
	protected ApplicationContext applicationContext;

	@ConstructorProperties({"studioConfiguration", "scriptExecutor"})
	public ContentLifeCycleImpl(final StudioConfiguration studioConfiguration, final ScriptExecutor scriptExecutor) {
		this.studioConfiguration = studioConfiguration;
		this.scriptExecutor = scriptExecutor;
	}

	@Override
	public void execute(String siteId, LifecycleContent lifecycleContent, ContentLoader contentLoader) throws ServiceLayerException {
		String contentType = lifecycleContent.getContentType();
		String repoPath = lifecycleContent.getRepoPath();

		// Validate contentType param
		if (isEmpty(contentType) || CONTENT_TYPE_UNKNOWN.equals(contentType)) {
			logger.warn("No content type provided for site '{}' path '{}'. Skipping script execution.", siteId, repoPath);
			return;
		}

		// Check if the script exists
		String scriptPath = getScriptPath(siteId, contentType);
		String script;
		try (InputStream content = contentLoader.getContentRaw(siteId, scriptPath)) {
			if (content == null) {
				logger.warn("No content lifecycle script found for site '{}' path '{}' contentType '{}'. Skipping content lifecycle.", siteId, repoPath, contentType);
				return;
			}
			script = IOUtils.toString(content, UTF_8);
		} catch (IOException e) {
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
			scriptExecutor.executeScriptString(siteId, script, model);
		} catch (ScriptException e) {
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
	private Map<String, Object> buildModel(String siteId, LifecycleContent lifecycleContent, ContentLoader contentLoader) {
		Map<String, Object> model = new HashMap<>();
		model.put(KEY_SITE, siteId);
		model.put(KEY_USER, getCurrentUsername());
		model.put(KEY_PATH, lifecycleContent.getRepoPath());
		model.put(KEY_CONTENT_TYPE, lifecycleContent.getContentType());
		model.put(CONTENT_LIFECYCLE_OPERATION, lifecycleContent.getOperation().toString());
		model.put(KEY_CONTENT_LOADER, contentLoader);

		model.put(KEY_LIFECYCLE_CONTENT, lifecycleContent);

		if (shouldIncludeApplicationContext()) {
			model.put(DmConstants.KEY_APPLICATION_CONTEXT, applicationContext);
		}
		addSpringBeans(model);

		return model;
	}

	private void addSpringBeans(Map<String, Object> model) {
		String[] enabledBeans = studioConfiguration.getArray(CONTENT_LIFECYCLE_INCLUDED_BEANS, String.class);
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

	private boolean shouldIncludeApplicationContext() {
		return studioConfiguration.getProperty(CONTENT_LIFECYCLE_INCLUDE_APPLICATION_CONTEXT, Boolean.class, false);
	}

	/**
	 * Get the controller script path for the given site and content type.
	 */
	protected String getScriptPath(String site, String contentType) {
		return studioConfiguration.getProperty(CONTENT_PROCESSOR_CONTENT_LIFE_CYCLE_SCRIPT_LOCATION)
			.replaceAll(StudioConstants.PATTERN_SITE, site)
			.replaceAll(StudioConstants.PATTERN_CONTENT_TYPE, contentType);
	}

	@Override
	public void setApplicationContext(@NotNull final ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
}
