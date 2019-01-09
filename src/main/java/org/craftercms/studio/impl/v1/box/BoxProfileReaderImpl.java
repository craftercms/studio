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

package org.craftercms.studio.impl.v1.box;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.io.FileHandler;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v1.box.BoxProfile;
import org.craftercms.studio.api.v1.box.BoxProfileReader;
import org.craftercms.studio.api.v1.exception.BoxException;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.springframework.beans.factory.annotation.Required;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

/**
 * {@inheritDoc}
 */
public class BoxProfileReaderImpl implements BoxProfileReader {

	public static final String KEY_PROFILE = "profile";
	public static final String KEY_ID = "id";
	public static final String KEY_CLIENT_ID = "clientId";
	public static final String KEY_CLIENT_SECRET = "clientSecret";
	public static final String KEY_ENTERPRISE_ID = "enterpriseId";
	public static final String KEY_PUBLIC_KEY_ID = "publicKeyId";
	public static final String KEY_PRIVATE_KEY_PASS = "privateKeyPassword";
	public static final String KEY_PRIVATE_KEY_PATH = "privateKeyPath";
	public static final String KEY_PRIVATE_KEY = "privateKey";
	public static final String KEY_UPLOAD_FOLDER = "uploadFolder";

	/**
	 * The full path of the configuration file in the site repository
	 */
	protected String configPath;

	protected ContentService contentService;

	@Required
	public void setConfigPath(final String configPath) {
		this.configPath = configPath;
	}

	@Required
	public void setContentService(final ContentService contentService) {
		this.contentService = contentService;
	}

	protected HierarchicalConfiguration getConfiguration(String site) throws BoxException {
		try {
			InputStream input = contentService.getContent(site, configPath);
			Parameters params = new Parameters();
			FileBasedConfigurationBuilder<XMLConfiguration> builder = new FileBasedConfigurationBuilder<>(XMLConfiguration.class);

			XMLConfiguration config = builder.configure(params.xml()).getConfiguration();
			FileHandler fileHandler = new FileHandler(config);

			fileHandler.setEncoding("UTF-8");
			fileHandler.load(input);

			return config;
		} catch (Exception e) {
			throw new BoxException("Unable to read box configuration file", e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BoxProfile getProfile(final String site, final String profileId) throws BoxException {
		try {
			HierarchicalConfiguration config = getConfiguration(site);
			@SuppressWarnings("unchecked")
			List<HierarchicalConfiguration> profiles = config.configurationsAt(KEY_PROFILE);
			Optional<HierarchicalConfiguration> profileConfig = profiles.stream().filter(item -> profileId.equals(item.getString(KEY_ID))).findFirst();

			HierarchicalConfiguration profile = profileConfig.orElseThrow(() -> new BoxException("Profile not found"));

			BoxProfile boxProfile = new BoxProfile();
			boxProfile.setClientId(profile.getString(KEY_CLIENT_ID));
			boxProfile.setClientSecret(profile.getString(KEY_CLIENT_SECRET));
			boxProfile.setEnterpriseId(profile.getString(KEY_ENTERPRISE_ID));
			String boxPrivateKey = profile.getString(KEY_PRIVATE_KEY);
			if (StringUtils.isNotBlank(boxPrivateKey)) {
				boxProfile.setPrivateKey(boxPrivateKey);
			} else {
				boxProfile.setPrivateKey(new String(
						Files.readAllBytes(Paths.get(profile.getString(KEY_PRIVATE_KEY_PATH)))));
			}

			boxProfile.setPrivateKeyPassword(profile.getString(KEY_PRIVATE_KEY_PASS));
			boxProfile.setPublicKeyId(profile.getString(KEY_PUBLIC_KEY_ID));
			boxProfile.setUploadFolder(profile.getString(KEY_UPLOAD_FOLDER));
			return boxProfile;

		} catch (IOException e) {
			throw new BoxException("Unable to read private key file", e);
		}
	}
}
