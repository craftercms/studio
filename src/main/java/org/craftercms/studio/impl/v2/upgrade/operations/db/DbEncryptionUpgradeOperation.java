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
package org.craftercms.studio.impl.v2.upgrade.operations.db;

import org.craftercms.commons.crypto.CryptoException;
import org.craftercms.commons.crypto.TextEncryptor;
import org.craftercms.commons.git.utils.AuthenticationType;
import org.craftercms.commons.upgrade.exception.UpgradeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.craftercms.studio.api.v2.dal.RemoteRepository;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.impl.v2.upgrade.StudioUpgradeContext;
import org.craftercms.studio.impl.v2.upgrade.operations.AbstractUpgradeOperation;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.beans.ConstructorProperties;
import java.util.List;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;

/**
 * Implementation of {@link org.craftercms.commons.upgrade.UpgradeOperation} that upgrades encrypted values in
 * the database.
 *
 * @author joseross
 * @since 3.1.9
 */
public class DbEncryptionUpgradeOperation extends AbstractUpgradeOperation {

    private static final Logger logger = LoggerFactory.getLogger(DbEncryptionUpgradeOperation.class);

    protected final String REMOTE_REPOSITORIES_QUERY = "select id, remote_password, remote_token, " +
            "remote_private_key, authentication_type from remote_repository where authentication_type != '" +
            AuthenticationType.NONE + "'";
    protected final String REMOTE_REPOSITORIES_UPDATE = "update remote_repository set remote_password = " +
            ":remotePassword, remote_token = :remoteToken, remote_private_key = :remotePrivateKey where id = :id";

    protected TextEncryptor textEncryptor;

    @ConstructorProperties({"studioConfiguration", "textEncryptor"})
    public DbEncryptionUpgradeOperation(StudioConfiguration studioConfiguration,
                                        TextEncryptor textEncryptor) {
        super(studioConfiguration);
        this.textEncryptor = textEncryptor;
    }

    @Override
    public void doExecute(StudioUpgradeContext context) throws UpgradeException {
        try {
            NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(context.getDataSource());
            upgradeRemoteRepositories(jdbcTemplate);
        } catch (Exception e) {
            throw new UpgradeException("Error trying to upgrade database", e);
        }
    }

    protected String upgradeValue(String encrypted) throws CryptoException {
        return textEncryptor.encrypt(textEncryptor.decrypt(encrypted));
    }

    protected void upgradeRemoteRepositories(NamedParameterJdbcTemplate jdbcTemplate) throws CryptoException {
        logger.debug("Look for remote repositories to upgrade");
        List<RemoteRepository> remotes =
                jdbcTemplate.query(REMOTE_REPOSITORIES_QUERY, new BeanPropertyRowMapper<>(RemoteRepository.class));
        logger.debug("Found '{}' remote repositories to upgrade", remotes.size());

        if (isEmpty(remotes)) {
            return;
        }

        for (RemoteRepository remote : remotes) {
            logger.debug("Upgrade the remote repository with ID '{}'", remote.getId());
            switch (remote.getAuthenticationType()) {
                case AuthenticationType.BASIC:
                    remote.setRemotePassword(upgradeValue(remote.getRemotePassword()));
                    break;
                case AuthenticationType.TOKEN:
                    remote.setRemoteToken(upgradeValue(remote.getRemoteToken()));
                    break;
                case AuthenticationType.PRIVATE_KEY:
                    remote.setRemotePrivateKey(upgradeValue(remote.getRemotePrivateKey()));
                    break;
                default:
                    logger.warn("Unknown authentication type '{}' for the remote repository with ID '{}'",
                            remote.getAuthenticationType(), remote.getId());
            }
        }

        jdbcTemplate.batchUpdate(REMOTE_REPOSITORIES_UPDATE, remotes.stream()
                .map(BeanPropertySqlParameterSource::new)
                .toArray(BeanPropertySqlParameterSource[]::new));
    }

}
