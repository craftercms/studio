/*
 * Copyright (C) 2007-2018 Crafter Software Corporation. All rights reserved.
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
 *
 */

package org.craftercms.studio.impl.v2.service.cluster;

import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.crypto.CryptoException;
import org.craftercms.commons.crypto.TextEncryptor;
import org.craftercms.commons.crypto.impl.PbkAesTextEncryptor;
import org.craftercms.studio.api.v1.dal.RemoteRepository;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.util.StudioConfiguration;
import org.craftercms.studio.api.v2.dal.ClusterDAO;
import org.craftercms.studio.api.v2.dal.ClusterMember;
import org.craftercms.studio.api.v2.dal.MetaDAO;
import org.craftercms.studio.api.v2.service.cluster.ClusterNodeRegistration;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.craftercms.studio.api.v1.util.StudioConfiguration.CLUSTERING_NODE_REGISTRATION;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.SECURITY_CIPHER_KEY;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.SECURITY_CIPHER_SALT;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.CLUSTER_LOCAL_IP;

public class ClusterNodeRegistrationImpl implements ClusterNodeRegistration {

    private static final Logger logger = LoggerFactory.getLogger(ClusterNodeRegistrationImpl.class);
    private static final String GIT_URL_PATTERN = "ssh://{username}@{localIp}{absolutePath}";

    private ClusterDAO clusterDao;
    private MetaDAO metaDao;
    private StudioConfiguration studioConfiguration;

    public void init() {
        Map<String, String> registrationData = getConfiguration();
        ClusterMember clusterMember = new ClusterMember();
        if (registrationData != null && !registrationData.isEmpty()) {
            try {
                clusterMember.setLocalIp(registrationData.get("localIp"));

                Path path = Paths.get(studioConfiguration.getProperty(StudioConfiguration.REPO_BASE_PATH),
                        studioConfiguration.getProperty(StudioConfiguration.SITES_REPOS_PATH));
                String authenticationType = registrationData.get("authenticationType");
                String username = registrationData.get("username");
                String password = registrationData.get("password");
                String token = registrationData.get("token");
                String privateKey = registrationData.get("privateKey");
                String gitUrl = GIT_URL_PATTERN.replace("{username}", username)
                        .replace("{localIp}", clusterMember.getLocalIp())
                        .replace("{absolutePath}", path.toAbsolutePath().normalize().toString())
                        + "/{siteId}";
                clusterMember.setGitUrl(gitUrl);
                clusterMember.setState(ClusterMember.State.ACTIVE);
                clusterMember.setGitRemoteName(clusterMember.getLocalIp());
                clusterMember.setGitAuthType(authenticationType.toLowerCase());
                clusterMember.setGitUsername(username);
                TextEncryptor encryptor = null;
                encryptor = new PbkAesTextEncryptor(studioConfiguration.getProperty(SECURITY_CIPHER_KEY),
                        studioConfiguration.getProperty(SECURITY_CIPHER_SALT));
                String hashedPassword = encryptor.encrypt(password);
                clusterMember.setGitPassword(hashedPassword);

                clusterMember.setGitToken(token);
                clusterMember.setGitPrivateKey(privateKey);
                registerClusterNode(clusterMember);
            } catch (CryptoException e) {
                logger.error("Failed to register cluster member");
            }
        }
    }

    private Map<String, String> getConfiguration() {
        Map<String, String> registrationData = new HashMap<String, String>();
        registrationData = studioConfiguration.getProperty(CLUSTERING_NODE_REGISTRATION, registrationData.getClass());
        return registrationData;
    }

    public void destroy() {
        Map<String, String> registrationData = getConfiguration();
        if (registrationData != null && !registrationData.isEmpty()) {
            removeClusterNode(registrationData.get("localIp"));
        }
    }

    @Override
    public boolean isRegistered(String localIp) {
        if (StringUtils.isNotEmpty(localIp)) {
            Map<String, String> params = new HashMap<String, String>();
            params.put(CLUSTER_LOCAL_IP, localIp);
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
    public boolean removeClusterNode(String localIp) {
        logger.error("Remove cluster node " + localIp);
        Map<String, String> params = new HashMap<String, String>();
        params.put(CLUSTER_LOCAL_IP, localIp);
        int result = clusterDao.removeMemberByLocalIp(params);
        return result > 0;
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
