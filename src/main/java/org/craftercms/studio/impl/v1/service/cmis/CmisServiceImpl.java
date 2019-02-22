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

package org.craftercms.studio.impl.v1.service.cmis;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConnectionException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.validation.annotations.param.ValidateIntegerParam;
import org.craftercms.commons.validation.annotations.param.ValidateParams;
import org.craftercms.commons.validation.annotations.param.ValidateSecurePathParam;
import org.craftercms.commons.validation.annotations.param.ValidateStringParam;
import org.craftercms.studio.api.v1.exception.CmisPathNotFoundException;
import org.craftercms.studio.api.v1.exception.CmisRepositoryNotFoundException;
import org.craftercms.studio.api.v1.exception.CmisTimeoutException;
import org.craftercms.studio.api.v1.exception.CmisUnavailableException;
import org.craftercms.studio.api.v1.exception.ServiceException;
import org.craftercms.studio.api.v1.exception.StudioPathNotFoundException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.cmis.CmisService;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.to.CmisContentItemTO;
import org.craftercms.studio.api.v1.to.DataSourceRepositoryTO;
import org.craftercms.studio.api.v1.util.StudioConfiguration;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;

import javax.activation.MimetypesFileTypeMap;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.InputStream;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import static org.craftercms.studio.api.v1.constant.StudioConstants.FILE_SEPARATOR;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.CONFIGURATION_SITE_DATA_SOURCES_CONFIG_BASE_PATH;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.CONFIGURATION_SITE_DATA_SOURCES_CONFIG_FILE_NAME;

public class CmisServiceImpl implements CmisService {

    private static final Logger logger = LoggerFactory.getLogger(CmisServiceImpl.class);

    private static final String REPOSITORY_CONFIG_XPATH = "/cmis/repositories/repository[id='{cmisrepoid}']";
    private static final String CMIS_REPO_ID_VARIABLE = "{cmisrepoid}";

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

    private static final String CMIS_SEARCH_QUERY = "select * from cmis:document where IN_TREE('{folderId}') and cmis:name like '%{searchTerm}%'";
    private static final String CMIS_SEARCH_QUERY_FOLDER_ID_VARIABLE = "{folderId}";
    private static final String CMIS_SEARCH_QUERY_SEARCH_TERM_VARIABLE = "{searchTerm}";

    @Override
    @ValidateParams
    public int listTotal(@ValidateStringParam(name = "site") String site, @ValidateStringParam(name = "cmisRepo") String cmisRepo, @ValidateSecurePathParam(name = "path") String path) throws CmisUnavailableException, CmisTimeoutException, CmisRepositoryNotFoundException {
        int toRet = 0;
        DataSourceRepositoryTO repositoryConfig = getConfiguration(site, cmisRepo);
        if (repositoryConfig != null) {
            Session session = createCMISSession(repositoryConfig);
            if (session != null) {
                String contentPath = Paths.get(repositoryConfig.getBasePath(), path).toString();
                CmisObject cmisObject = session.getObjectByPath(contentPath);
                if (cmisObject != null) {
                    if (BaseTypeId.CMIS_FOLDER.equals(cmisObject.getBaseTypeId())) {
                        Folder folder = (Folder)cmisObject;
                        Iterable<CmisObject> iterable = folder.getChildren();
                        Iterator<CmisObject> iterator = iterable.iterator();
                        while (iterator.hasNext()) {
                            toRet++;
                            iterator.next();
                        }
                    }
                }
            }
        }
        return toRet;
    }

    @Override
    @ValidateParams
    public List<CmisContentItemTO> list(@ValidateStringParam(name = "site") String site, @ValidateStringParam(name = "cmisRepo") String cmisRepo, @ValidateSecurePathParam(name = "path") String path, @ValidateIntegerParam(name = "start") int start, @ValidateIntegerParam(name = "number") int number) throws CmisUnavailableException, CmisTimeoutException, CmisRepositoryNotFoundException {
        List<CmisContentItemTO> toRet = new ArrayList<CmisContentItemTO>();
        DataSourceRepositoryTO repositoryConfig = getConfiguration(site, cmisRepo);
        if (repositoryConfig != null) {
            Session session = createCMISSession(repositoryConfig);
            if (session != null) {
                String contentPath = Paths.get(repositoryConfig.getBasePath(), path).toString();
                CmisObject cmisObject = session.getObjectByPath(contentPath);
                if (cmisObject != null) {
                    if (BaseTypeId.CMIS_FOLDER.equals(cmisObject.getBaseTypeId())) {
                        Folder folder = (Folder)cmisObject;
                        Iterable<CmisObject> iterable = folder.getChildren();
                        Iterator<CmisObject> iterator = iterable.iterator();
                        int index = 0;
                        int count = 0;
                        while (iterator.hasNext()) {
                            if (start <= index && count < number) {
                                CmisContentItemTO item = new CmisContentItemTO();
                                CmisObject cmisItem = iterator.next();
                                item.setItem_name(cmisItem.getName());
                                if (BaseTypeId.CMIS_DOCUMENT.equals(cmisItem.getBaseTypeId())) {
                                    org.apache.chemistry.opencmis.client.api.Document cmisDoc = (org.apache.chemistry.opencmis.client.api.Document)cmisItem;
                                    item.setItem_path(cmisDoc.getPaths().get(0));
                                    item.setMime_type(cmisDoc.getContentStreamMimeType());
                                    String contentId = cmisDoc.getId();
                                    StringTokenizer st = new StringTokenizer(contentId, ";");
                                    if (st.hasMoreTokens()) {
                                        item.setItem_id(st.nextToken());
                                    }
                                    item.setSize(cmisDoc.getContentStreamLength());
                                    toRet.add(item);
                                } else if (BaseTypeId.CMIS_FOLDER.equals(cmisItem.getBaseTypeId())) {
                                    Folder cmisFolder = (Folder)cmisItem;
                                    item.setItem_id(cmisFolder.getId());
                                    item.setItem_path(cmisFolder.getPath());
                                    item.setMime_type(MIME_TYPE_FOLDER);
                                    item.setSize(-1);
                                    toRet.add(item);
                                }
                                count++;
                            } else {
                                iterator.next();
                            }
                            index++;
                        }
                    }
                }
            }
        }
        return toRet;
    }

    private DataSourceRepositoryTO getConfiguration(String site, String cmisRepo) throws CmisRepositoryNotFoundException {
        String configPath = Paths.get(getConfigLocation(), getConfigFileName()).toString();
        Document document =  null;
        DataSourceRepositoryTO repositoryConfig = null;
        try {
            document = contentService.getContentAsDocument(site, configPath);
            Node node = document.selectSingleNode(REPOSITORY_CONFIG_XPATH.replace(CMIS_REPO_ID_VARIABLE, cmisRepo));
            if (node != null) {
                repositoryConfig = new DataSourceRepositoryTO();
                repositoryConfig.setId(getPropertyValue(node, ID_PROPERTY));
                repositoryConfig.setType(getPropertyValue(node, TYPE_PROPERTY));
                repositoryConfig.setUrl(getPropertyValue(node, URL_PROPERTY));
                repositoryConfig.setUsername(getPropertyValue(node, USERNAME_PROPERTY));
                repositoryConfig.setPassword(getPropertyValue(node, PASSWORD_PROPERTY));
                repositoryConfig.setBasePath(getPropertyValue(node, BASE_PATH_PROPERTY));
                repositoryConfig.setDownloadUrlRegex(getPropertyValue(node, DOWNLOAD_URL_REGEX_PROPERTY));
                repositoryConfig.setUseSsl(Boolean.parseBoolean(getPropertyValue(node, USE_SSL_PROPERTY)));
            } else {
                throw new CmisRepositoryNotFoundException();
            }
        } catch (DocumentException e) {
            logger.error("Error while getting configuration for site: " + site + " cmis: " + cmisRepo + " (config path: " + configPath + ")");
        }
        return repositoryConfig;
    }

    private String getPropertyValue(Node repositoryNode, String property) {
        Node propertyNode = repositoryNode.selectSingleNode(property);
        if (propertyNode != null) {
            return propertyNode.getStringValue();
        }
        return StringUtils.EMPTY;
    }

    private Session createCMISSession(DataSourceRepositoryTO config) throws CmisUnavailableException, CmisTimeoutException {

        if (config.isUseSsl()) {
            SSLContext sc = null;
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
        Map<String, String> parameter = new HashMap<String, String>();
        parameter.put(SessionParameter.USER, config.getUsername());
        parameter.put(SessionParameter.PASSWORD, config.getPassword());

        // connection settings - we're connecting to a public cmis repo,
        // using the AtomPUB binding, but there are other options here,
        // or you can substitute your own URL
        parameter.put(SessionParameter.ATOMPUB_URL, config.getUrl());
        parameter.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());
        parameter.put(SessionParameter.COOKIES, "true");

        // find all the repositories at this URL - there should only be one.
        List<Repository> repositories = new ArrayList<Repository>();
        repositories = sessionFactory.getRepositories(parameter);

        // create session with the first (and only) repository
        Repository repository = repositories.get(0);
        parameter.put(SessionParameter.REPOSITORY_ID, repository.getId());
        Session session = null;
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
    @ValidateParams
    public long searchTotal(@ValidateStringParam(name = "site") String site, @ValidateStringParam(name = "cmisRepo") String cmisRepo, @ValidateStringParam(name = "searchTerm") String searchTerm, @ValidateSecurePathParam(name = "path") String path) throws CmisUnavailableException, CmisTimeoutException, CmisRepositoryNotFoundException {
        long toRet = 0;
        DataSourceRepositoryTO repositoryConfig = getConfiguration(site, cmisRepo);
        if (repositoryConfig != null) {
            Session session = createCMISSession(repositoryConfig);
            if (session != null) {
                String contentPath = Paths.get(repositoryConfig.getBasePath(), path).toString();
                CmisObject cmisObject = session.getObjectByPath(contentPath);
                if (cmisObject != null) {
                    if (BaseTypeId.CMIS_FOLDER.equals(cmisObject.getBaseTypeId())) {
                        String queryString = CMIS_SEARCH_QUERY.replace(CMIS_SEARCH_QUERY_FOLDER_ID_VARIABLE, cmisObject.getId()).replace(CMIS_SEARCH_QUERY_SEARCH_TERM_VARIABLE, searchTerm);
                        ItemIterable<QueryResult> result = session.query(queryString, false);
                        toRet = result.getTotalNumItems();
                    }
                }
            }
        }
        return toRet;
    }

    @Override
    @ValidateParams
    public List<CmisContentItemTO> search(@ValidateStringParam(name = "site") String site, @ValidateStringParam(name = "cmisRepo") String cmisRepo, @ValidateStringParam(name = "searchTerm") String searchTerm, @ValidateSecurePathParam(name = "path") String path, @ValidateIntegerParam(name = "start") int start, @ValidateIntegerParam(name = "number") int number) throws CmisUnavailableException, CmisTimeoutException, CmisRepositoryNotFoundException {
        List<CmisContentItemTO> toRet = new ArrayList<CmisContentItemTO>();
        DataSourceRepositoryTO repositoryConfig = getConfiguration(site, cmisRepo);
        if (repositoryConfig != null) {
            Session session = createCMISSession(repositoryConfig);
            if (session != null) {
                String contentPath = Paths.get(repositoryConfig.getBasePath(), path).toString();
                CmisObject cmisObject = session.getObjectByPath(contentPath);
                if (cmisObject != null) {
                    if (BaseTypeId.CMIS_FOLDER.equals(cmisObject.getBaseTypeId())) {
                        String queryString = CMIS_SEARCH_QUERY.replace(CMIS_SEARCH_QUERY_FOLDER_ID_VARIABLE, cmisObject.getId()).replace(CMIS_SEARCH_QUERY_SEARCH_TERM_VARIABLE, searchTerm);
                        ItemIterable<QueryResult> result = session.query(queryString, false);
                        result.skipTo(start);
                        Iterator<QueryResult> iterator = result.iterator();
                        int count = 0;
                        while (iterator.hasNext()) {
                            if (count < number) {
                                CmisContentItemTO item = new CmisContentItemTO();
                                QueryResult qr = iterator.next();

                                String contentId = qr.getPropertyById(PropertyIds.OBJECT_ID).getFirstValue().toString();
                                StringTokenizer st = new StringTokenizer(contentId, ";");
                                if (st.hasMoreTokens()) {
                                    item.setItem_id(st.nextToken());
                                }
                                CmisObject qrObject = session.getObject(item.getItem_id());
                                org.apache.chemistry.opencmis.client.api.Document cmisDoc = (org.apache.chemistry.opencmis.client.api.Document)qrObject;
                                item.setItem_name(cmisDoc.getName());
                                item.setItem_path(cmisDoc.getPaths().get(0));
                                item.setMime_type(cmisDoc.getContentStreamMimeType());
                                item.setSize(cmisDoc.getContentStreamLength());
                                toRet.add(item);
                                count++;
                            } else {
                                iterator.next();
                            }
                        }
                    }
                }
            }
        }
        return toRet;
    }

    @Override
    @ValidateParams
    public void cloneContent(@ValidateStringParam(name = "siteId") String siteId, @ValidateStringParam(name = "cmisRepoId") String cmisRepoId, @ValidateSecurePathParam(name = "cmisPath") String cmisPath, @ValidateSecurePathParam(name = "studioPath") String studioPath) throws CmisUnavailableException, CmisTimeoutException, CmisPathNotFoundException, ServiceException, StudioPathNotFoundException, CmisRepositoryNotFoundException {
        if (!contentService.contentExists(siteId, studioPath)) throw new StudioPathNotFoundException();
        List<CmisContentItemTO> toRet = new ArrayList<CmisContentItemTO>();
        DataSourceRepositoryTO repositoryConfig = getConfiguration(siteId, cmisRepoId);
        if (repositoryConfig != null) {
            logger.debug("Create new CMIS session");
            Session session = createCMISSession(repositoryConfig);
            if (session != null) {
                String contentPath = Paths.get(repositoryConfig.getBasePath(), cmisPath).toString();
                logger.debug("Find object for CMIS path: " + contentPath);
                CmisObject cmisObject = session.getObjectByPath(contentPath);
                if (cmisObject != null) {
                    if (BaseTypeId.CMIS_FOLDER.equals(cmisObject.getBaseTypeId())) {
                        throw new CmisPathNotFoundException();
                    } else if (BaseTypeId.CMIS_DOCUMENT.equals(cmisObject.getBaseTypeId())) {
                        org.apache.chemistry.opencmis.client.api.Document cmisDoc = (org.apache.chemistry.opencmis.client.api.Document)cmisObject;
                        String fileName = cmisDoc.getName();
                        String savePath = studioPath + FILE_SEPARATOR + fileName;
                        ContentStream cs = cmisDoc.getContentStream();
                        logger.debug("Save CMIS file to: " + savePath);
                        contentService.writeContent(siteId, savePath, cs.getStream());
                    }
                } else {
                    throw new CmisPathNotFoundException();
                }
            } else {
                throw new CmisUnauthorizedException();
            }
        }
    }

    @Override
    @ValidateParams
    public void uploadContent(@ValidateStringParam(name = "siteId") String siteId,
                              @ValidateStringParam(name = "cmisRepoId") String cmisRepoId,
                              @ValidateSecurePathParam(name = "cmisPath") String cmisPath,
                              @ValidateSecurePathParam(name = "filename") String filename, InputStream content)
            throws CmisUnavailableException, CmisTimeoutException, CmisPathNotFoundException,
            CmisRepositoryNotFoundException {
        List<CmisContentItemTO> toRet = new ArrayList<CmisContentItemTO>();
        DataSourceRepositoryTO repositoryConfig = getConfiguration(siteId, cmisRepoId);
        if (repositoryConfig != null) {
            logger.debug("Create new CMIS session");
            Session session = createCMISSession(repositoryConfig);
            if (session != null) {
                String contentPath = Paths.get(repositoryConfig.getBasePath(), cmisPath).toString();
                logger.debug("Find object for CMIS path: " + contentPath);
                CmisObject cmisObject = session.getObjectByPath(contentPath);
                if (cmisObject != null) {
                    if (BaseTypeId.CMIS_FOLDER.equals(cmisObject.getBaseTypeId())) {
                        MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
                        String mimeType = mimeTypesMap.getContentType(filename);
                        ContentStream contentStream =
                                session.getObjectFactory().createContentStream(filename, -1, mimeType, content);
                        Folder folder  = (Folder)cmisObject;
                        Map<String, Object> properties = new HashMap<String, Object>();
                        properties.put(PropertyIds.OBJECT_TYPE_ID, BaseTypeId.CMIS_DOCUMENT.value());
                        properties.put(PropertyIds.NAME, filename);
                        org.apache.chemistry.opencmis.client.api.Document newDoc =
                                folder.createDocument(properties, contentStream, null);
                        session.removeObjectFromCache(newDoc.getId());
                        session.clear();
                    } else if (BaseTypeId.CMIS_DOCUMENT.equals(cmisObject.getBaseTypeId())) {
                        throw new CmisPathNotFoundException();
                    }
                } else {
                    throw new CmisPathNotFoundException();
                }
            } else {
                throw new CmisUnauthorizedException();
            }
        }
    }

    private String getConfigLocation() {
        return studioConfiguration.getProperty(CONFIGURATION_SITE_DATA_SOURCES_CONFIG_BASE_PATH);
    }

    private String getConfigFileName() {
        return studioConfiguration.getProperty(CONFIGURATION_SITE_DATA_SOURCES_CONFIG_FILE_NAME);
    }

    public StudioConfiguration getStudioConfiguration() { return studioConfiguration; }
    public void setStudioConfiguration(StudioConfiguration studioConfiguration) { this.studioConfiguration = studioConfiguration; }

    public ContentService getContentService() { return contentService; }
    public void setContentService(ContentService contentService) { this.contentService = contentService; }

    protected StudioConfiguration studioConfiguration;
    protected ContentService contentService;
}
