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

package org.craftercms.studio.impl.v2.service.cluster;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.crypto.CryptoException;
import org.craftercms.commons.crypto.TextEncryptor;
import org.craftercms.commons.crypto.impl.PbkAesTextEncryptor;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.util.StudioConfiguration;
import org.craftercms.studio.api.v2.dal.ClusterDAO;
import org.craftercms.studio.api.v2.dal.ClusterMember;
import org.craftercms.studio.api.v2.dal.MetaDAO;
import org.craftercms.studio.api.v2.service.cluster.ClusterNodeRegistration;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.craftercms.studio.api.v1.constant.StudioConstants.CLUSTER_MEMBER_AUTHENTICATION_TYPE;
import static org.craftercms.studio.api.v1.constant.StudioConstants.CLUSTER_MEMBER_LOCAL_ADDRESS;
import static org.craftercms.studio.api.v1.constant.StudioConstants.CLUSTER_MEMBER_PASSWORD;
import static org.craftercms.studio.api.v1.constant.StudioConstants.CLUSTER_MEMBER_PRIVATE_KEY;
import static org.craftercms.studio.api.v1.constant.StudioConstants.CLUSTER_MEMBER_TOKEN;
import static org.craftercms.studio.api.v1.constant.StudioConstants.CLUSTER_MEMBER_USERNAME;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.*;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.CLUSTER_LOCAL_ADDRESS;

public class ClusterNodeRegistrationImpl implements ClusterNodeRegistration {

    private static final Logger logger = LoggerFactory.getLogger(ClusterNodeRegistrationImpl.class);

    private ClusterDAO clusterDao;
    private MetaDAO metaDao;
    private StudioConfiguration studioConfiguration;

    public void init() {
        logger.debug("Autoregister cluster if cluster node is configured");
        HierarchicalConfiguration<ImmutableNode> registrationData = getConfiguration();
        ClusterMember clusterMember = new ClusterMember();
        if (registrationData != null && !registrationData.isEmpty()) {
            try {
                logger.debug("Collect and populate data for cluster node registration");
                clusterMember.setLocalAddress(registrationData.getString(CLUSTER_MEMBER_LOCAL_ADDRESS));
                if (!isRegistered(clusterMember.getLocalAddress())) {
                    Path path = Paths.get(studioConfiguration.getProperty(REPO_BASE_PATH),
                                          studioConfiguration.getProperty(SITES_REPOS_PATH));
                    String authenticationType = registrationData.getString(CLUSTER_MEMBER_AUTHENTICATION_TYPE);
                    String username = registrationData.getString(CLUSTER_MEMBER_USERNAME);
                    String password = registrationData.getString(CLUSTER_MEMBER_PASSWORD);
                    String token = registrationData.getString(CLUSTER_MEMBER_TOKEN);
                    String privateKey = registrationData.getString(CLUSTER_MEMBER_PRIVATE_KEY);
                    String gitUrl = studioConfiguration.getProperty(CLUSTERING_SYNC_URL_FORMAT);
                    if (StringUtils.isEmpty(username)) {
                        gitUrl = gitUrl.replace("{username}@", "");
                    } else {
                        gitUrl = gitUrl.replace("{username}", username);
                    }
                    gitUrl = gitUrl.replace("{localAddress}", clusterMember.getLocalAddress())
                                   .replace("{absolutePath}", path.toAbsolutePath().normalize().toString())
                             + "/{siteId}";
                    clusterMember.setGitUrl(gitUrl);
                    clusterMember.setState(ClusterMember.State.ACTIVE);
                    clusterMember.setGitRemoteName(getGitRemoteName(clusterMember));
                    clusterMember.setGitAuthType(authenticationType.toLowerCase());
                    clusterMember.setGitUsername(username);
                    TextEncryptor encryptor = null;
                    encryptor = new PbkAesTextEncryptor(studioConfiguration.getProperty(SECURITY_CIPHER_KEY),
                                                        studioConfiguration.getProperty(SECURITY_CIPHER_SALT));
                    if (StringUtils.isEmpty(password)) {
                        clusterMember.setGitPassword(password);
                    } else {
                        String hashedPassword = encryptor.encrypt(password);
                        clusterMember.setGitPassword(hashedPassword);
                    }

                    if (StringUtils.isEmpty(token)) {
                        clusterMember.setGitToken(token);
                    } else {
                        String hashedToken = encryptor.encrypt(token);
                        clusterMember.setGitToken(hashedToken);
                    }

                    if (StringUtils.isEmpty(privateKey)) {
                        clusterMember.setGitPrivateKey(privateKey);
                    } else {
                        String hashedPrivateKey = encryptor.encrypt(privateKey);
                        clusterMember.setGitPrivateKey(hashedPrivateKey);
                    }

                    logger.debug("Register cluster member");
                    registerClusterNode(clusterMember);
                }
            } catch (CryptoException e) {
                logger.error("Failed to register cluster member");
            }
        }
    }

    private HierarchicalConfiguration<ImmutableNode> getConfiguration() {
        return studioConfiguration.getSubConfig(CLUSTERING_NODE_REGISTRATION);
    }

    public void destroy() {
        HierarchicalConfiguration<ImmutableNode> registrationData = getConfiguration();
        if (registrationData != null && !registrationData.isEmpty()) {
            removeClusterNode(registrationData.getString(CLUSTER_MEMBER_LOCAL_ADDRESS));
        }
    }

    @Override
    public boolean isRegistered(String localAddress) {
        if (StringUtils.isNotEmpty(localAddress)) {
            Map<String, String> params = new HashMap<String, String>();
            params.put(CLUSTER_LOCAL_ADDRESS, localAddress);
            int cnt = clusterDao.countRegistrations(params);
            return cnt > 0;
        } else {
            return false;
        }
    }

    @Override
    public boolean registerClusterNode(ClusterMember clusterMember) {
        int result = clusterDao.addMember(clusterMember);
        return result > 0;
    }

    @Override
    public boolean removeClusterNode(String localAddress) {
        logger.error("Remove cluster node " + localAddress);
        Map<String, String> params = new HashMap<String, String>();
        params.put(CLUSTER_LOCAL_ADDRESS, localAddress);
        int result = clusterDao.removeMemberByLocalAddress(params);
        return result > 0;
    }

    private String getGitRemoteName(ClusterMember clusterMember) {
        // When the port is specified, replaces the colon since it's an invalid remote name character
        return clusterMember.getLocalAddress().replace(":", "_");
    }

    public ClusterDAO getClusterDao() {
        return clusterDao;
    }

    public void setClusterDao(ClusterDAO clusterDao) {
        this.clusterDao = clusterDao;
    }

    public MetaDAO getMetaDao() {
        return metaDao;
    }

    public void setMetaDao(MetaDAO metaDao) {
        this.metaDao = metaDao;
    }

    public StudioConfiguration getStudioConfiguration() {
        return studioConfiguration;
    }

    public void setStudioConfiguration(StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }
}
