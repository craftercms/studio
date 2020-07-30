/*
 * Copyright (C) 2007-2020 Crafter Software Corporation. All Rights Reserved.
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
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v2.dal.ClusterMember;
import org.craftercms.studio.api.v2.dal.RemoteRepository;
import org.craftercms.studio.api.v2.exception.UpgradeException;
import org.craftercms.studio.impl.v2.upgrade.operations.AbstractUpgradeOperation;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Implementation of {@link org.craftercms.studio.api.v2.upgrade.UpgradeOperation} that upgrades encrypted values in
 * the database.
 *
 * @author joseross
 * @since 3.1.9
 */
public class DbEncryptionUpgradeOperation extends AbstractUpgradeOperation {

    private static final Logger logger = LoggerFactory.getLogger(DbEncryptionUpgradeOperation.class);

    protected String REMOTE_REPOSITORIES_QUERY = "select id, remote_password, remote_token, remote_private_key, " +
            "authentication_type from remote_repository where authentication_type != ?";
    protected String REMOTE_REPOSITORIES_UPDATE = "update remote_repository set remote_password = ?, " +
            "remote_token = ?, remote_private_key = ? where id = ?";

    protected String CLUSTER_MEMBERS_QUERY = "select id, git_password, git_token, git_private_key, git_auth_type " +
            "from cluster where git_auth_type != ?";
    protected String CLUSTER_MEMBERS_UPDATE = "update cluster set git_password = ?, git_token = ?, " +
            "git_private_key = ? where id = ?";

    protected TextEncryptor textEncryptor;

    public DbEncryptionUpgradeOperation(TextEncryptor textEncryptor) {
        this.textEncryptor = textEncryptor;
    }

    @Override
    public void execute(String site) throws UpgradeException {
        try {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            upgradeRemoteRepositories(jdbcTemplate);
            upgradeClusterMembers(jdbcTemplate);
        } catch (Exception e) {
            throw new UpgradeException("Error trying to upgrade database", e);
        }
    }

    protected String upgradeValue(String encrypted) throws CryptoException {
        return textEncryptor.encrypt(textEncryptor.decrypt(encrypted));
    }

    protected void upgradeRemoteRepositories(JdbcTemplate jdbcTemplate) throws CryptoException {
        logger.debug("Looking for remote repositories to upgrade");
        List<RemoteRepository> remotes = jdbcTemplate.query(REMOTE_REPOSITORIES_QUERY,
                new Object[] { RemoteRepository.AuthenticationType.NONE },
                new BeanPropertyRowMapper<>(RemoteRepository.class));
        logger.debug("Found {0} remote repositories", remotes.size());

        for (RemoteRepository remote : remotes) {
            logger.debug("Upgrading remote repository with id: {0}", remote.getId());
            switch (remote.getAuthenticationType()) {
                case RemoteRepository.AuthenticationType.BASIC:
                    remote.setRemotePassword(upgradeValue(remote.getRemotePassword()));
                    break;
                case RemoteRepository.AuthenticationType.TOKEN:
                    remote.setRemoteToken(upgradeValue(remote.getRemoteToken()));
                    break;
                case RemoteRepository.AuthenticationType.PRIVATE_KEY:
                    remote.setRemotePrivateKey(upgradeValue(remote.getRemotePrivateKey()));
                    break;
                default:
                    logger.warn("Unknown authentication type {0} for remote repository with id {1}",
                            remote.getAuthenticationType(), remote.getId());
            }
        }

        jdbcTemplate.batchUpdate(REMOTE_REPOSITORIES_UPDATE, remotes.stream()
                .map(remote ->
                        new Object[] {
                                remote.getRemotePassword(), remote.getRemoteToken(), remote.getRemotePrivateKey(),
                                remote.getId()
                })
                .collect(toList()));
    }

    protected void upgradeClusterMembers(JdbcTemplate jdbcTemplate) throws CryptoException {
        logger.debug("Looking for cluster members to upgrade");
        List<ClusterMember> members = jdbcTemplate.query(CLUSTER_MEMBERS_QUERY,
                new Object[] { RemoteRepository.AuthenticationType.NONE },
                new BeanPropertyRowMapper<>(ClusterMember.class));
        logger.debug("Found {0} cluster members", members.size());

        for (ClusterMember member : members) {
            logger.debug("Upgrading remote repository with id: {0}", member.getId());
            switch (member.getGitAuthType()) {
                case RemoteRepository.AuthenticationType.BASIC:
                    member.setGitPassword(upgradeValue(member.getGitPassword()));
                    break;
                case RemoteRepository.AuthenticationType.TOKEN:
                    member.setGitToken(upgradeValue(member.getGitToken()));
                    break;
                case RemoteRepository.AuthenticationType.PRIVATE_KEY:
                    member.setGitPrivateKey(upgradeValue(member.getGitPrivateKey()));
                    break;
                default:
                    logger.warn("Unknown authentication type {0} for cluster member with id {1}",
                            member.getGitAuthType(), member.getId());
            }
        }

        jdbcTemplate.batchUpdate(CLUSTER_MEMBERS_UPDATE, members.stream()
                .map(member ->
                        new Object[] {
                                member.getGitPassword(), member.getGitToken(), member.getGitPrivateKey(),
                                member.getId()
                })
                .collect(toList()));
    }

}
