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

package org.craftercms.studio.impl.v2.service.cmis;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConnectionException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.apache.commons.io.FilenameUtils;
import org.craftercms.commons.security.permissions.DefaultPermission;
import org.craftercms.commons.security.permissions.annotations.HasPermission;
import org.craftercms.commons.security.permissions.annotations.ProtectedResourceId;
import org.craftercms.studio.api.v1.exception.CmisPathNotFoundException;
import org.craftercms.studio.api.v1.exception.CmisRepositoryNotFoundException;
import org.craftercms.studio.api.v1.exception.CmisTimeoutException;
import org.craftercms.studio.api.v1.exception.CmisUnavailableException;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v2.dal.CmisContentItem;
import org.craftercms.studio.api.v2.dal.DataSourceRepository;
import org.craftercms.studio.api.v2.exception.configuration.ConfigurationException;
import org.craftercms.studio.api.v2.service.cmis.CmisService;
import org.craftercms.studio.api.v2.service.config.ConfigurationService;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.api.v2.utils.StudioUtils;
import org.craftercms.studio.model.rest.CmisUploadItem;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.beans.ConstructorProperties;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import static org.apache.chemistry.opencmis.commons.PropertyIds.NAME;
import static org.apache.chemistry.opencmis.commons.PropertyIds.OBJECT_ID;
import static org.apache.chemistry.opencmis.commons.PropertyIds.OBJECT_TYPE_ID;
import static org.apache.chemistry.opencmis.commons.SessionParameter.ATOMPUB_URL;
import static org.apache.chemistry.opencmis.commons.SessionParameter.BINDING_TYPE;
import static org.apache.chemistry.opencmis.commons.SessionParameter.COOKIES;
import static org.apache.chemistry.opencmis.commons.SessionParameter.PASSWORD;
import static org.apache.chemistry.opencmis.commons.SessionParameter.USER;
import static org.apache.chemistry.opencmis.commons.enums.BaseTypeId.CMIS_DOCUMENT;
import static org.apache.chemistry.opencmis.commons.enums.BaseTypeId.CMIS_FOLDER;
import static org.apache.chemistry.opencmis.commons.enums.BindingType.ATOMPUB;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_SITE_DATA_SOURCES_CONFIG_LOCATION;
import static org.craftercms.studio.permissions.PermissionResolverImpl.PATH_RESOURCE_ID;
import static org.craftercms.studio.permissions.PermissionResolverImpl.SITE_ID_RESOURCE_ID;

public class CmisServiceImpl implements CmisService {

    private static final Logger logger = LoggerFactory.getLogger(CmisServiceImpl.class);

    private static final String REPOSITORY_CONFIG_KEY = "repositories";

    // xml properties
    private static final String ID_PROPERTY = "id";
    private static final String TYPE_PROPERTY = "type";
    private static final String URL_PROPERTY = "url";
    private static final String USERNAME_PROPERTY = "username";
    private static final String PASSWORD_PROPERTY = "password";
    private static final String BASE_PATH_PROPERTY = "base-path";
    private static final String DOWNLOAD_URL_REGEX_PROPERTY = "download-url-regex";
    private static final String USE_SSL_PROPERTY = "use-ssl";

    private static final String MIME_TYPE_FOLDER = "folder";

    private static final String CMIS_SEARCH_QUERY =
            "select * from cmis:document where IN_TREE('{folderId}') and cmis:name like '%{searchTerm}%'";
    private static final String CMIS_SEARCH_QUERY_FOLDER_ID_VARIABLE = "{folderId}";
    private static final String CMIS_SEARCH_QUERY_SEARCH_TERM_VARIABLE = "{searchTerm}";
    private static final String ITEM_ID = "{item_id}";

    protected final StudioConfiguration studioConfiguration;
    protected final ContentService contentService;
    protected final ConfigurationService configurationService;

    @ConstructorProperties({"studioConfiguration", "contentService", "configurationService"})
    public CmisServiceImpl(StudioConfiguration studioConfiguration, ContentService contentService,
                           ConfigurationService configurationService) {
        this.studioConfiguration = studioConfiguration;
        this.contentService = contentService;
        this.configurationService = configurationService;
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = "list_cmis")
    public List<CmisContentItem> list(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId,
                                      String cmisRepo, String path)
            throws CmisRepositoryNotFoundException, CmisUnavailableException, CmisTimeoutException,
                    ConfigurationException {
        List<CmisContentItem> items = new ArrayList<>();
        DataSourceRepository repositoryConfig = getConfiguration(siteId, cmisRepo);
        Session session = createCMISSession(repositoryConfig);
        if (session != null) {
            String contentPath = Paths.get(repositoryConfig.getBasePath(), path).toString();
            CmisObject cmisObject = session.getObjectByPath(contentPath);
            if (cmisObject != null && CMIS_FOLDER.equals(cmisObject.getBaseTypeId())) {
                Folder folder = (Folder)cmisObject;
                Iterable<CmisObject> iterable = folder.getChildren();
                for (CmisObject cmisItem : iterable) {
                    CmisContentItem item = new CmisContentItem();
                    item.setItemName(cmisItem.getName());
                    if (CMIS_DOCUMENT.equals(cmisItem.getBaseTypeId())) {
                        Document cmisDoc = (Document) cmisItem;
                        item.setItemPath(cmisDoc.getPaths().get(0));
                        item.setMimeType(cmisDoc.getContentStreamMimeType());
                        String contentId = cmisDoc.getId();
                        StringTokenizer st = new StringTokenizer(contentId, ";");
                        if (st.hasMoreTokens()) {
                            item.setItemId(st.nextToken());
                        }
                        item.setSize(cmisDoc.getContentStreamLength());
                        items.add(item);
                    } else if (CMIS_FOLDER.equals(cmisItem.getBaseTypeId())) {
                        Folder cmisFolder = (Folder) cmisItem;
                        item.setItemId(cmisFolder.getId());
                        item.setItemPath(cmisFolder.getPath());
                        item.setMimeType(MIME_TYPE_FOLDER);
                        item.setSize(-1);
                        items.add(item);
                    }
                }
            }
        }
        return items;
    }

    private DataSourceRepository getConfiguration(String site, String cmisRepo)
            throws CmisRepositoryNotFoundException, ConfigurationException {
        var config = configurationService.getXmlConfiguration(site, getConfigLocation());
        var repo = config.childConfigurationsAt(REPOSITORY_CONFIG_KEY).stream()
                                                            .filter(r -> cmisRepo.equals(r.getString(ID_PROPERTY)))
                                                            .findFirst()
                                                            .orElseThrow(CmisRepositoryNotFoundException::new);
        var repositoryConfig = new DataSourceRepository();
        repositoryConfig.setId(repo.getString(ID_PROPERTY));
        repositoryConfig.setType(repo.getString(TYPE_PROPERTY));
        repositoryConfig.setUrl(repo.getString(URL_PROPERTY));
        repositoryConfig.setUsername(repo.getString(USERNAME_PROPERTY));
        repositoryConfig.setPassword(repo.getString(PASSWORD_PROPERTY));
        repositoryConfig.setBasePath(repo.getString(BASE_PATH_PROPERTY));
        repositoryConfig.setDownloadUrlRegex(repo.getString(DOWNLOAD_URL_REGEX_PROPERTY));
        repositoryConfig.setUseSsl(repo.getBoolean(USE_SSL_PROPERTY, false));
        return repositoryConfig;
    }

    private Session createCMISSession(DataSourceRepository config)
            throws CmisUnavailableException, CmisTimeoutException {

        if (config.isUseSsl()) {
            SSLContext sc;
            try {
                sc = getSSLContext();
                // Ignore differences between given hostname and certificate hostname
                HostnameVerifier hv = (hostname, session) -> true;
                HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
                HttpsURLConnection.setDefaultHostnameVerifier(hv);
            } catch (KeyManagementException | NoSuchAlgorithmException  e) {
                logger.error("Error initializing SSL context", e);
            }
        }

        // Create a SessionFactory and set up the SessionParameter map
        SessionFactory sessionFactory = SessionFactoryImpl.newInstance();
        Map<String, String> parameter = new HashMap<>();
        parameter.put(USER, config.getUsername());
        parameter.put(PASSWORD, config.getPassword());

        // connection settings - we're connecting to a public cmis repo,
        // using the AtomPUB binding, but there are other options here,
        // or you can substitute your own URL
        parameter.put(ATOMPUB_URL, config.getUrl());
        parameter.put(BINDING_TYPE, ATOMPUB.value());
        parameter.put(COOKIES, "true");

        // find all the repositories at this URL - there should only be one.
        List<Repository> repositories;
        repositories = sessionFactory.getRepositories(parameter);

        // create session with the first (and only) repository
        Repository repository = repositories.get(0);
        parameter.put(SessionParameter.REPOSITORY_ID, repository.getId());
        Session session;
        try {
            session = sessionFactory.createSession(parameter);
        } catch (CmisConnectionException e) {
            throw new CmisTimeoutException(e);
        } catch (CmisBaseException e) {
            throw new CmisUnavailableException(e);
        }

        return session;
    }

    private SSLContext getSSLContext() throws KeyManagementException, NoSuchAlgorithmException {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                    public void checkClientTrusted(
                            X509Certificate[] certs, String authType) {
                    }
                    public void checkServerTrusted(
                            X509Certificate[] certs, String authType) {
                    }
                }
        };

        // Install the all-trusting trust manager
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new SecureRandom());

        return sc;
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = "search_cmis")
    public List<CmisContentItem> search(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId, String cmisRepo,
                                        String searchTerm, String path)
            throws CmisRepositoryNotFoundException, CmisUnavailableException, CmisTimeoutException,
                    ConfigurationException {
        List<CmisContentItem> toRet = new ArrayList<>();
        DataSourceRepository repositoryConfig = getConfiguration(siteId, cmisRepo);
        Session session = createCMISSession(repositoryConfig);
        if (session != null) {
            String contentPath = Paths.get(repositoryConfig.getBasePath(), path).toString();
            CmisObject cmisObject = session.getObjectByPath(contentPath);
            if (cmisObject != null) {
                if (CMIS_FOLDER.equals(cmisObject.getBaseTypeId())) {
                    String queryString = CMIS_SEARCH_QUERY.replace(CMIS_SEARCH_QUERY_FOLDER_ID_VARIABLE,
                            cmisObject.getId()).replace(CMIS_SEARCH_QUERY_SEARCH_TERM_VARIABLE, searchTerm);
                    ItemIterable<QueryResult> result = session.query(queryString, false);
                    for (QueryResult qr : result) {
                        CmisContentItem item = new CmisContentItem();
                        String contentId = qr.getPropertyById(OBJECT_ID).getFirstValue().toString();
                        StringTokenizer st = new StringTokenizer(contentId, ";");
                        if (st.hasMoreTokens()) {
                            item.setItemId(st.nextToken());
                        }
                        CmisObject qrObject = session.getObject(item.getItemId());
                        Document cmisDoc = (Document) qrObject;
                        item.setItemName(cmisDoc.getName());
                        item.setItemPath(cmisDoc.getPaths().get(0));
                        item.setMimeType(cmisDoc.getContentStreamMimeType());
                        item.setSize(cmisDoc.getContentStreamLength());
                        toRet.add(item);
                    }
                }
            }
        }
        return toRet;
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = "clone_content_cmis")
    public void cloneContent(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId, String cmisRepoId,
                             String cmisPath, @ProtectedResourceId(PATH_RESOURCE_ID) String studioPath)
            throws CmisRepositoryNotFoundException, CmisUnavailableException,
            CmisTimeoutException, CmisPathNotFoundException, ServiceLayerException, UserNotFoundException {
        if (!contentService.contentExists(siteId, studioPath)) {
            // create all missing folders in the path
            contentService.createFolder(siteId, FilenameUtils.getFullPathNoEndSeparator(studioPath),
                                        FilenameUtils.getName(studioPath));
        }
        DataSourceRepository repositoryConfig = getConfiguration(siteId, cmisRepoId);
        logger.debug("Create new CMIS session");
        Session session = createCMISSession(repositoryConfig);
        if (session == null) {
            throw new CmisUnauthorizedException();
        }
        String contentPath = Paths.get(repositoryConfig.getBasePath(), cmisPath).toString();
        logger.debug("Find object for CMIS path: {0}", contentPath);
        CmisObject cmisObject = session.getObjectByPath(contentPath);
        if (cmisObject == null || !CMIS_DOCUMENT.equals(cmisObject.getBaseTypeId())) {
            throw new CmisPathNotFoundException();
        }
        Document cmisDoc = (Document) cmisObject;
        String fileName = cmisDoc.getName();
        ContentStream contentStream = cmisDoc.getContentStream();
        logger.debug("Save CMIS file {0} to: {1}", fileName, studioPath);
        try (InputStream inputStream = contentStream.getStream()) {
            contentService.writeContentAsset(siteId, studioPath, fileName, inputStream, null, null,
                                 null, null, null, "true", null);
        } catch (IOException e) {
            throw new ServiceLayerException("Error cloning CMIS object " + cmisPath + " to site " + siteId + " at " +
                                            studioPath);
        }
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = "upload_content_cmis")
    public CmisUploadItem uploadContent(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId, String cmisRepoId,
                             String cmisPath,
                              String filename, InputStream content)
            throws CmisUnavailableException, CmisTimeoutException, CmisRepositoryNotFoundException,
            CmisPathNotFoundException, ConfigurationException {
        DataSourceRepository repositoryConfig = getConfiguration(siteId, cmisRepoId);
        CmisUploadItem cmisUploadItem = new CmisUploadItem();
        logger.debug("Create new CMIS session");
        Session session = createCMISSession(repositoryConfig);
        if (session != null) {
            String contentPath = Paths.get(repositoryConfig.getBasePath(), cmisPath).toString();
            logger.debug("Find object for CMIS path: " + contentPath);
            CmisObject cmisObject = session.getObjectByPath(contentPath);
            if (cmisObject != null) {
                if (BaseTypeId.CMIS_FOLDER.equals(cmisObject.getBaseTypeId())) {
                    CmisObject docObject = null;
                    try {
                        docObject = session.getObjectByPath(Paths.get(contentPath, filename).toString());
                    } catch (CmisBaseException e) {
                        // Content does not exist - no error
                        logger.debug("File " + filename + " does not exist at " + contentPath);
                    }
                    String mimeType = StudioUtils.getMimeType(filename);
                    ContentStream contentStream =
                            session.getObjectFactory().createContentStream(filename, -1, mimeType, content);
                    Folder folder  = (Folder)cmisObject;
                    cmisUploadItem.setName(filename);
                    cmisUploadItem.setFolder(false);
                    cmisUploadItem.setFileExtension(FilenameUtils.getExtension(filename));
                    if (docObject != null) {
                        Document doc = (Document) docObject;
                        doc.setContentStream(contentStream, true);

                        String contentId = doc.getId();
                        StringTokenizer st = new StringTokenizer(contentId, ";");
                        if (st.hasMoreTokens()) {
                            cmisUploadItem.setUrl(repositoryConfig.getDownloadUrlRegex().replace(ITEM_ID,
                                    st.nextToken()));
                        }
                        session.removeObjectFromCache(doc.getId());
                    } else {
                        Map<String, Object> properties = new HashMap<>();
                        properties.put(OBJECT_TYPE_ID, CMIS_DOCUMENT.value());
                        properties.put(NAME, filename);
                        Document newDoc = folder.createDocument(properties, contentStream, null);
                        session.removeObjectFromCache(newDoc.getId());
                        String contentId = newDoc.getId();
                        StringTokenizer st = new StringTokenizer(contentId, ";");
                        if (st.hasMoreTokens()) {
                            cmisUploadItem.setUrl(repositoryConfig.getDownloadUrlRegex().replace(ITEM_ID,
                                    st.nextToken()));
                        }
                    }
                    session.clear();
                } else if (CMIS_DOCUMENT.equals(cmisObject.getBaseTypeId())) {
                    throw new CmisPathNotFoundException();
                }
            } else {
                throw new CmisPathNotFoundException();
            }
        } else {
            throw new CmisUnauthorizedException();
        }
        return cmisUploadItem;
    }

    private String getConfigLocation() {
        return studioConfiguration.getProperty(CONFIGURATION_SITE_DATA_SOURCES_CONFIG_LOCATION);
    }

}
