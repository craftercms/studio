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

package org.craftercms.studio.impl.v2.upgrade.operations.contentType;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.craftercms.commons.upgrade.exception.UpgradeException;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.impl.v2.upgrade.StudioUpgradeContext;
import org.craftercms.studio.impl.v2.upgrade.operations.site.BatchXsltFileUpgradeOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.ConstructorProperties;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_SITE_CONTENT_TYPES_CONFIG_FILE_NAME;

/**
 * Upgrader to merge config.xml into form-definition.xml files for content types.
 */
public class ContentTypeConfigMergeUpgrader extends BatchXsltFileUpgradeOperation {
	private static final Logger logger = LoggerFactory.getLogger(ContentTypeConfigMergeUpgrader.class);

	protected static final String CONFIG_FILE_PARAM = "configFileName";
	private String configFileName;

	@ConstructorProperties("studioConfiguration")
	public ContentTypeConfigMergeUpgrader(StudioConfiguration studioConfiguration) {
		super(studioConfiguration);
	}

	@Override
	protected void doInit(final HierarchicalConfiguration config) {
		super.doInit(config);
		configFileName = studioConfiguration.getProperty(CONFIGURATION_SITE_CONTENT_TYPES_CONFIG_FILE_NAME);
	}

	@Override
	public void doExecute(StudioUpgradeContext context) throws UpgradeException {
		super.doExecute(context);
		Path repositoryRoot = context.getRepositoryPath();
		for (String deletedFile : ListUtils.emptyIfNull(deletedFiles)) {
			logger.info("Deleting config file after merging: {}", deletedFile);
			try {
				Files.deleteIfExists(repositoryRoot.resolve(deletedFile));
			} catch (IOException e) {
				throw new UpgradeException("Error deleting config file after merging: " + deletedFile, e);
			}
		}
	}

	@Override
	protected void executeTemplate(StudioUpgradeContext context, String formDefinitionPath, OutputStream os) throws UpgradeException {
		logger.info("Executing config merge template for form definition: {}", formDefinitionPath);
		super.executeTemplate(context, formDefinitionPath, os);

		Path configFilePath = Path.of(formDefinitionPath).resolveSibling(configFileName);
		trackDeletedFiles(configFilePath.toString());
	}

	@Override
	protected Map<String, Object> getTemplateParameters(StudioUpgradeContext context, String formDefinitionPath) {
		Map<String, Object> params = new HashMap<>(super.getTemplateParameters(context, formDefinitionPath));
		params.put(CONFIG_FILE_PARAM, Path.of(formDefinitionPath).resolveSibling(configFileName));
		return params;
	}
}
