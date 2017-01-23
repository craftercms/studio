/*******************************************************************************
 * Crafter Studio Web-content authoring solution
 *     Copyright (C) 2007-2016 Crafter Software Corporation.
 * 
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.craftercms.studio.impl.v1.repository.alfresco;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.activation.MimetypesFileTypeMap;
import javax.annotation.PreDestroy;
import javax.servlet.http.HttpSession;

import org.apache.chemistry.opencmis.client.api.*;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.util.URIUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.http.RequestContext;
import org.craftercms.studio.api.v1.ebus.RepositoryEventContext;
import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.exception.ServiceException;
import org.craftercms.studio.api.v1.job.CronJobContext;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.repository.RepositoryItem;
import org.craftercms.studio.api.v1.service.security.SecurityProvider;
import org.craftercms.studio.api.v1.to.VersionTO;
import org.craftercms.studio.impl.v1.repository.AbstractContentRepository;
import org.springframework.http.MediaType;

/**
 * Alfresco repository implementation.  This is the only point of contact with Alfresco's API in
 * the entire system under the org.craftercms.cstudio.impl package structure
 * @author russdanner
 *
 */
public class AlfrescoContentRepository extends AbstractContentRepository implements SecurityProvider {

    public static final String SSO_TICKET_PREFIX = "sso_user:";

    private static final Logger logger = LoggerFactory.getLogger(AlfrescoContentRepository.class);

    private static final TypeReference ALFRESCO_RESPONSE_TYPE = new TypeReference<Map<String, Object>>() {};

    protected HttpClient httpClient;
    protected ObjectMapper objectMapper;

    public AlfrescoContentRepository() {
        httpClient = new HttpClient(new MultiThreadedHttpConnectionManager());
        objectMapper = new ObjectMapper();
    }

    @PreDestroy
    public void destroy() {
        ((MultiThreadedHttpConnectionManager)httpClient.getHttpConnectionManager()).shutdown();
    }

    @Override
    public InputStream getContent(String path) throws ContentNotFoundException {
        return getContentStreamCMIS(path);
    }

    @Override
    public boolean contentExists(String path) {
        String cleanPath = path.replaceAll("//", "/"); // sometimes sent bad paths
        if (cleanPath.endsWith("/")) {
            cleanPath = cleanPath.substring(0, cleanPath.length() - 1);
        }
        try {
            Session session = getCMISSession();
            CmisObject cmisObject = session.getObjectByPath(cleanPath);
            return cmisObject != null;
        } catch (CmisBaseException e) {
            logger.debug("Content not found exception for path: " + path, e);
            return false;
        }
    }


    @Override
    public boolean writeContent(String path, InputStream content) throws ServiceException {
        logger.debug("writing content to " + path);
        return writeContentCMIS(path, content);
    }

    @Override
    public boolean createFolder(String path, String name) {
        String folderRef = this.createFolderInternal(path, name);
        return folderRef != null;
    }

    @Override
    public boolean deleteContent(String path) {
        logger.debug("deleting content at " + path);
        return deleteContentCMIS(path);
    }

    @Override
    public boolean copyContent(String fromPath, String toPath) {
        return this.copyContentInternal(fromPath, toPath, null, false);
    }

    @Override
    public boolean moveContent(String fromPath, String toPath) {
        return moveContent(fromPath, toPath, null);
    }

    /**
     * why is this method public?
     */
    @Override
    public boolean moveContent(String fromPath, String toPath, String newName) {
        long startTime = System.currentTimeMillis();
        boolean result = false;

        // service layer assumes it is giving the name where the item should land
        // alfresco implementaiton wants to look up the parent folder as the target
        String targetPath = toPath.substring(0, toPath.lastIndexOf("/"));
        newName = (newName != null) ? newName : toPath.substring(toPath.lastIndexOf("/")+1);

        logger.info("ALFRESCO MOVE, CLEAN ME UP BEFORE PR: Use Copy/Delete model {0} -> {1}", fromPath, targetPath);
        // DO NOT COMMENT OUT OLD CODE :-/
        //return this.copyContentInternal(fromPath, toPath, newName, true);

        // This code should be lifed out of this layer or removed all-together
        // Content service should be the only thing that calls this method so the repo
        // should not have to worry about bad input.

        String cleanFromPath = fromPath.replaceAll("//", "/"); // sometimes sent bad paths
        if (cleanFromPath.endsWith("/")) {
            cleanFromPath = cleanFromPath.substring(0, cleanFromPath.length() - 1);
        }
        String cleanToPath = targetPath.replaceAll("//", "/"); // sometimes sent bad paths
        if (cleanToPath.endsWith("/")) {
            cleanToPath = cleanToPath.substring(0, cleanToPath.length() - 1);
        }


        try {
            Session session = getCMISSession();
            CmisObject sourceCmisObject = session.getObjectByPath(cleanFromPath);
            CmisObject targetCmisObject = session.getObjectByPath(cleanToPath);

            if (sourceCmisObject != null && targetCmisObject != null) {
                
                ObjectType sourceType = sourceCmisObject.getType();
                ObjectType targetType = targetCmisObject.getType();

                if (BaseTypeId.CMIS_FOLDER.value().equals(targetType.getId())) {
                    
                    Folder targetFolder = (Folder)targetCmisObject;
                    
                    if ("cmis:document".equals(sourceType.getId())) {
                        org.apache.chemistry.opencmis.client.api.Document sourceDocument =
                                (org.apache.chemistry.opencmis.client.api.Document)sourceCmisObject;
                        logger.debug("Moving document {0} to {1}", sourceDocument.getPaths().get(0), targetFolder.getPath());
                        List<Folder> sourceParents = sourceDocument.getParents();
                        Folder sourceParent = (CollectionUtils.isEmpty(sourceParents) ? null : sourceParents.get(0));
                        sourceDocument.move(sourceParent, targetFolder);
                    
                    } 
                    else if ("cmis:folder".equals(sourceType.getId())) {
                        Folder sourceFolder = (Folder)sourceCmisObject;
                        Folder sourceParentFolder = sourceFolder.getFolderParent();
                        logger.debug("Moving folder {0} to {1}", sourceFolder.getPath(), targetFolder.getPath());

                        if (newName != null) {
                            sourceCmisObject.rename(newName);
                            sourceFolder.move(sourceParentFolder, targetFolder);
                        } 
                        else {
                            Iterable<CmisObject> children = sourceFolder.getChildren();

                            for (CmisObject child : children) {
                                FileableCmisObject fileableChild = (FileableCmisObject)child;
                                
                                fileableChild.move(sourceFolder, targetFolder);
                            }

                            sourceFolder.delete();
                        }
                    }
                    session.clear();
                    return true;
                } else {
                    logger.error("Move failed since target path " + toPath + " is not folder.");
                }
            } else {
                if (sourceCmisObject == null) {
                    logger.error("Move failed since source path " + fromPath + " does not exist.");
                }
                if (targetCmisObject == null) {
                    logger.error("Move failed since target path " + toPath + " does not exist.");
                }
            }
        } catch (CmisBaseException err) {
            logger.error("Error while moving content from " + fromPath + " to " + toPath, err);
        }
        return result;
    }

    /**
     * get immediate children for path
     * @param path path to content
     */
    public RepositoryItem[] getContentChildren(String path) {
        RepositoryItem[] items = getContentChildrenCMIS(path);
        return items;
    }

    @Override
    public RepositoryItem[] getContentChildren(String path, boolean ignoreCache) {
        return getContentChildren(path);
    }

    /**
     * create a version
     * @param path location of content
     * @param majorVersion true if major
     * @return the created version ID or null on failure
     */
    @Override
    public String createVersion(String path, boolean majorVersion) {
        return createVersion(path, null, majorVersion);
    }

    /**
     * create a version
     * @param path location of content
     * @param majorVersion true if major
     * @return the created version ID or null on failure
     */
    @Override
    public String createVersion(String path, String comment, boolean majorVersion) {
        long startTime = System.currentTimeMillis();
        String versionLabel = null;
        if (majorVersion) {
            // only major version will be created on demand. minor version is created on updates

            Map<String, String> params = new HashMap<String, String>();
            String cleanPath = path.replaceAll("//", "/"); // sometimes sent bad paths
            if (cleanPath.endsWith("/")) {
                cleanPath = cleanPath.substring(0, cleanPath.length() - 1);
            }
            try {
                Session session = getCMISSession();
                CmisObject cmisObject = session.getObjectByPath(cleanPath);
                if (cmisObject != null) {
                    ObjectType type = cmisObject.getBaseType();
                    if ("cmis:document".equals(type.getId())) {
                        org.apache.chemistry.opencmis.client.api.Document document =
                            (org.apache.chemistry.opencmis.client.api.Document) cmisObject;
                        ObjectId objId = null;
                        try {
                             objId = document.checkOut();
                        } catch (CmisVersioningException ex) {
                            String pwcId = document.getVersionSeriesCheckedOutId();
                            objId = session.getObject(pwcId);
                        }
                        org.apache.chemistry.opencmis.client.api.Document workingCopy =
                            (org.apache.chemistry.opencmis.client.api.Document) session.getObject(objId);
                        ContentStream contentStream = workingCopy.getContentStream();
                        objId = workingCopy.checkIn(majorVersion, null, contentStream, comment);
                        session.removeObjectFromCache(document.getId());
                        session.removeObjectFromCache(objId);
                    }
                }
            } catch (CmisBaseException err) {
                logger.error("Error while creating new " + (majorVersion ? "major" : "minor") + " version for path " +
                             path, err);
            }
        }
        long duration = System.currentTimeMillis() - startTime;
        logger.debug("createVersion(path = {0}, majorVersion = {1}) ({2} ms)", path, majorVersion, duration);
        return versionLabel;

    }

    /** 
     * get the version history for an item
     * @param path - the path of the item
     */
    public VersionTO[] getContentVersionHistory(String path) {
        return getContentVersionHistoryCMIS(path);
    }



    /**
     * Get a specific content version
     * @param path - the path of the item
     * @param version - the path of the item
     */
    public InputStream getContentVersion(String path, String version)
    throws ContentNotFoundException {

        long startTime = System.currentTimeMillis();
        InputStream versionedContentStream = null;
        String cleanPath = cleanPath(path);

        try {
            Session session = getCMISSession();

            CmisObject cmisObject = session.getObjectByPath(cleanPath);

            if(cmisObject != null) {
                Document document = (Document)cmisObject;
                List<Document> versions = document.getAllVersions();

                if (versions != null && versions.size() > 0) {
                    for (Document documentVersion : versions) {
                        if (version.equals(documentVersion.getVersionLabel())) {
                            ContentStream contentStream = documentVersion.getContentStream();
                            versionedContentStream = contentStream.getStream();
                            break;
                        }
                    }
                }
                else {
                    logger.warn("no versions associated with '{0};{1}'", path, version);
                }
            }
            else {
                logger.warn("Unable to get content at version '{0};{1}'", path, version);
            }

        } catch (CmisBaseException err) {
            logger.error("err getting content version: ", err);
        }
        long duration = System.currentTimeMillis() - startTime;
        logger.debug("getContentVersion(path = {0}, version = {1}) ({4} ms) ", path, version, duration);

        return versionedContentStream;
    }

    /** 
     * revert a version (create a new version based on an old version)
     * @param path - the path of the item to "revert"
     * @param version - old version ID to base to version on
     */
    public boolean revertContent(String path, String version, boolean major, String comment) {
        return revertContentCMIS(path, version, major, comment);
    }

    /**
     * create a folder at the given path
     *
     * @param path
     *          a path to create a folder in
     * @param name
     *          a folder name to create
     * @return a node reference string of the new folder
     */
    protected String createFolderInternal(String path, String name) {
        logger.debug("creating a folder at " + path + " with name: " + name);
        if (createMissingFoldersCMIS(path + "/" + name)) {
            try {
                String nodeRef = getNodeRefForPathCMIS(path + "/" + name);
                return nodeRef;
            } catch (ContentNotFoundException e) {
                logger.info("Error while creating folder {1} in path {0}", e, path, name);
            }
        }
        return "";
    }

    /**
     * copy content from pathA to pathB
     * @param fromPath
     *             the source path
     * @param toPath
     *              the target path
     * @param newName
     *@param isCut
     *              true for move  @return  true if successful
     */
    protected boolean copyContentInternal(String fromPath, String toPath, String newName, boolean isCut) {
        logger.debug((isCut ? "Move" : "Copy") + " content from " + fromPath + " to " + toPath);
        return copyContentInternalCMIS(fromPath, toPath, newName, isCut);
    }

    /**
     * fire GET request to Alfresco with proper security
     */
    protected Map<String, Object> alfrescoGetRequest(String uri, Map<String, String> params) throws Exception {
        long startTime = System.currentTimeMillis();
        String serviceURL = buildAlfrescoRequestURL(uri, params);
        GetMethod method = new GetMethod(serviceURL);

        String ssoUsername = getSsoUsername();
        if (StringUtils.isNotEmpty(ssoUsername)) {
            method.addRequestHeader(alfrescoExternalAuthHeaderName, ssoUsername);
        }

        logger.debug("Executing get to {0}", serviceURL);

        try {
            int status = httpClient.executeMethod(method);

            logger.debug("Response status back from the server: {0}", status);

            return objectMapper.readValue(method.getResponseBodyAsStream(), ALFRESCO_RESPONSE_TYPE);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            logger.debug("alfrescoGetRequest(uri = {0}, params = {1}) ({2} ms)", uri, params, duration);

            method.releaseConnection();
        }
    }

    /**
     * fire POST request to Alfresco with proper security
     */
    protected Map<String, Object> alfrescoPostRequest(String uri, Map<String, String> params, InputStream body,
                                                      String bodyMimeType) throws Exception {
        long startTime = System.currentTimeMillis();
        String serviceURL = buildAlfrescoRequestURL(uri, params);

        PostMethod method = new PostMethod(serviceURL);
        method.setRequestEntity(new InputStreamRequestEntity(body, bodyMimeType));

        String ssoUsername = getSsoUsername();
        if (StringUtils.isNotEmpty(ssoUsername)) {
            method.addRequestHeader(alfrescoExternalAuthHeaderName, ssoUsername);
        }

        logger.debug("Executing post to {0}", serviceURL);

        try {
            int status = httpClient.executeMethod(method);

            logger.debug("Response status back from the server: {0}", status);

            return objectMapper.readValue(method.getResponseBodyAsStream(), ALFRESCO_RESPONSE_TYPE);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            logger.debug("alfrescoPostRequest(uri = {0}, params = {1}, body = {2}, bodyMimeType = {3}) ({4} ms)",
                         uri, params, "stream", bodyMimeType, duration);

            method.releaseConnection();
        }
    }

    /**
     * build request URLs 
     */
    protected String buildAlfrescoRequestURL(String uri, Map<String, String> params) throws Exception {
        String url = "";
        String serviceUrlBase = alfrescoUrl;

        if (StringUtils.isNotEmpty(getSsoUsername())) {
            serviceUrlBase += ssoServicePath;
        } else {
            serviceUrlBase += servicePath;
        }

        if (params != null) {
            for(Map.Entry<String, String> entry : params.entrySet()) {
                if (entry.getValue() != null) {
                    uri = uri.replace("{" + entry.getKey() + "}", URIUtil.encodeQuery(entry.getValue(), "utf-8"));
                }
            }
        }

        url = serviceUrlBase + uri;

        String ticket = getAlfTicket();
        if (StringUtils.isNotEmpty(ticket)) {
            url += (url.contains("?"))? "&alf_ticket=" + ticket: "?alf_ticket=" + ticket;
        }
        
        return url;
    }

    /**
     * Get the alfresco ticket from the URL or the cookie or from an authorinization
     */
    public String getAlfTicket() {
        return this.getCurrentTicket();
    }

    @Override
    public Map<String, String> getUserProfile(String username) {
        InputStream retStream;
        Map<String, String> toRet = new HashMap<String,String>();
        if (StringUtils.isEmpty(username)) {
            return toRet;
        }
        try {
            // construct and execute url to download result
            // TODO: use alfresco/service/api/sites/craftercms250/memberships/admin instead
            String downloadURI = "/api/people/{username}";
            Map<String, String> lookupContentParams = new HashMap<String, String>();
            lookupContentParams.put("username", username);

            Map<String, Object> result = alfrescoGetRequest(downloadURI, lookupContentParams);

            toRet.put("userName", (String)result.get("userName"));
            toRet.put("firstName", (String)result.get("firstName"));
            toRet.put("lastName", (String)result.get("lastName"));
            toRet.put("email", (String)result.get("email"));
        }
        catch(Exception err) {
            logger.error("err getting user profile: ", err);
        }

        return toRet;
    }

    @Override
    public Set<String> getUserGroups(String username) {
        InputStream retStream = null;
        Set<String> toRet = new HashSet<String>();
        try {
            // construct and execute url to download result
            // TODO: use alfresco/service/api/sites/craftercms250/memberships/admin instead
            String downloadURI = "/api/people/{username}?groups=true";
            Map<String, String> lookupContentParams = new HashMap<String, String>();
            lookupContentParams.put("username", username);

            Map<String, Object> result = alfrescoGetRequest(downloadURI, lookupContentParams);

            List<Map<String, String>> groups = (List<Map<String, String>>)result.get("groups");
            for (Map<String, String> group : groups) {
                toRet.add(group.get("displayName"));
            }
        }
        catch(Exception err) {
            logger.error("err getting content: ", err);
        }

        return toRet;
    }

    @Override
    public String getCurrentUser() {
        String username = getSsoUsername();
        if (StringUtils.isEmpty(username)) {
            username = getSessionUsername();
        }

        return username;
    }

    @Override
    public String authenticate(String username, String password) {
        String toRet = null;
        try {
            // construct and execute url to download result
            String downloadURI = "/api/login";

            String loginRequestBody = "{ \"username\" : \"" + username + "\", \"password\" : \"" + password + "\" }";
            InputStream bodyStream = IOUtils.toInputStream(loginRequestBody, "UTF-8");

            Map<String, Object> result = alfrescoPostRequest(downloadURI, null, bodyStream,
                                                             MediaType.APPLICATION_JSON_VALUE);
            if (MapUtils.isNotEmpty(result)) {
                Map<String, String> data = (Map<String, String>)result.get("data");
                toRet = data.get("ticket");

                this.storeSessionTicket(toRet);
                this.storeSessionUsername(username);
            } else {
                this.storeSessionTicket(null);
                this.storeSessionUsername(null);
            }
        }
        catch(Exception err) {
            logger.error("err authenticating: ", err);
        }
        return toRet;
    }

    @Override
    public boolean validateTicket(String ticket) {
        if (StringUtils.isEmpty(getSsoUsername())) {
            ticket = (ticket != null)? ticket: getCurrentTicket();

            if (StringUtils.isNotEmpty(ticket)) {
                long startTime = System.currentTimeMillis();

                logger.debug("Validating ticket {0}", ticket);

                Map<String, String> params = new HashMap<>();
                params.put("ticket", ticket);

                GetMethod method = null;
                String serviceURL;
                try {
                    serviceURL = buildAlfrescoRequestURL("/api/login/ticket/{ticket}", params);
                    method = new GetMethod(serviceURL);

                    int status = httpClient.executeMethod(method);
                    if (status == HttpStatus.SC_OK) {
                        long duration = System.currentTimeMillis() - startTime;

                        logger.debug("validateTicket(ticket = {0}) ({1} ms)", ticket, duration);

                        return true;
                    }
                } catch (Exception e) {
                    logger.error("Error while validating authentication token", e);
                } finally {
                    if (method != null) {
                        method.releaseConnection();
                    }
                }

                long duration = System.currentTimeMillis() - startTime;

                logger.debug("validateTicket(ticket = {0}) ({1} ms)", ticket, duration);
            }

            return false;
        } else {
            return true;
        }
    }

    protected String getNodeRefForPathCMIS(String fullPath) throws ContentNotFoundException {
        long startTime = System.currentTimeMillis();
        Map<String, String> params = new HashMap<String, String>();
        String cleanPath = fullPath.replaceAll("//", "/"); // sometimes sent bad paths
        if (cleanPath.endsWith("/")) {
            cleanPath = cleanPath.substring(0, cleanPath.length() - 1);
        }
        String nodeRef = null;
        try {
            Session session = getCMISSession();
            CmisObject cmisObject = session.getObjectByPath(cleanPath);
            if (cmisObject != null) {
                Property property = cmisObject.getProperty("alfcmis:nodeRef");
                nodeRef = property.getValueAsString();
            }
        } catch (CmisBaseException e) {
            logger.warn("Object not found in CMIS repository for path: {0}", e, fullPath);
            throw new ContentNotFoundException(e);
        }
        long duration = System.currentTimeMillis() - startTime;
        logger.debug("getNodeRefForPathCMIS(fullPath = {0}) ({1} ms)", fullPath, duration);
        return nodeRef;
    }

    protected InputStream getContentStreamCMIS(String fullPath) throws ContentNotFoundException {
        long startTime = System.currentTimeMillis();
        Map<String, String> params = new HashMap<String, String>();
        String cleanPath = fullPath.replaceAll("//", "/"); // sometimes sent bad paths
        if (cleanPath.endsWith("/")) {
            cleanPath = cleanPath.substring(0, cleanPath.length() - 1);
        }
        InputStream inputStream = null;
        try {
            Session session = getCMISSession();
            CmisObject cmisObject = session.getObjectByPath(cleanPath);
            String nodeRef = null;
            if (cmisObject != null) {
                ObjectType type = cmisObject.getType();
                if ("cmis:document".equals(type.getId())) {
                    org.apache.chemistry.opencmis.client.api.Document document =
                        (org.apache.chemistry.opencmis.client.api.Document) cmisObject;
                    ContentStream contentStream = document.getContentStream();
                    inputStream = contentStream.getStream();
                }
            }
        } catch (CmisBaseException e) {
            logger.error("Error getting content from CMIS repository for path: ", e, fullPath);
            throw new ContentNotFoundException(e);
        }
        long duration = System.currentTimeMillis() - startTime;
        logger.debug("getContentStreamCMIS(fullPath = {0}) ({1} ms)", fullPath, duration);
        return inputStream;
    }

    protected RepositoryItem[] getContentChildrenCMIS(String fullPath) {
        long startTime = System.currentTimeMillis();
        Map<String, String> params = new HashMap<String, String>();
        String cleanPath = fullPath.replaceAll("//", "/"); // sometimes sent bad paths
        if (cleanPath.endsWith("/")) {
            cleanPath = cleanPath.substring(0, cleanPath.length() - 1);
        }
        Session session = getCMISSession();
        CmisObject cmisObject = session.getObjectByPath(cleanPath);
        String nodeRef = null;
        RepositoryItem[] items = null;
        if (cmisObject != null) {
            ObjectType type = cmisObject.getBaseType();
            if ("cmis:folder".equals(type.getId())) {
                Folder folder = (Folder) cmisObject;
                ItemIterable<CmisObject> children = folder.getChildren();
                List<RepositoryItem> tempList = new ArrayList<RepositoryItem>();
                Iterator<CmisObject> iterator = children.iterator();
                while (iterator.hasNext()) {
                    CmisObject child = iterator.next();
                    boolean isWorkingCopy = false;
                    boolean isFolder = "cmis:folder".equals(child.getBaseType().getId());
                    RepositoryItem item = new RepositoryItem();
                    item.name = child.getName();

                    if (BaseTypeId.CMIS_DOCUMENT.equals(child.getBaseTypeId())) {
                        org.apache.chemistry.opencmis.client.api.Document document =
                            (org.apache.chemistry.opencmis.client.api.Document) child;
                        item.path = document.getPaths().get(0);
                        Property<?> secundaryTypes = document.getProperty(PropertyIds.SECONDARY_OBJECT_TYPE_IDS);
                        if (secundaryTypes != null) {
                            List<String> aspects = secundaryTypes.getValue();
                            if (aspects.contains("P:cm:workingcopy")) {
                                isWorkingCopy = true;
                            }
                        }
                    } else if (BaseTypeId.CMIS_FOLDER.equals(child.getBaseTypeId())) {
                        Folder childFolder = (Folder)child;
                        item.path = childFolder.getPath();
                    } else {
                        item.path = fullPath;
                    }
                    item.path = StringUtils.removeEnd(item.path,"/" + item.name);
                    item.isFolder = isFolder;
                    if (!isWorkingCopy) {
                        tempList.add(item);
                    }
                }
                items = new RepositoryItem[tempList.size()];
                items = tempList.toArray(items);
            }
        }
        long duration = System.currentTimeMillis() - startTime;
        logger.debug("getContentChildrenCMIS(fullPath = {0}) ({1} ms)", fullPath, duration);
        return items;
    }

    protected boolean writeContentCMIS(String fullPath, InputStream content) throws ServiceException {
        long startTime = System.currentTimeMillis();
        Map<String, String> params = new HashMap<String, String>();
        String cleanPath = fullPath.replaceAll("//", "/"); // sometimes sent bad paths
        if (cleanPath.endsWith("/")) {
            cleanPath = cleanPath.substring(0, cleanPath.length() - 1);
        }
        int splitIndex = cleanPath.lastIndexOf("/");
        String filename = cleanPath.substring(splitIndex + 1);
        MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
        String mimeType = mimeTypesMap.getContentType(filename);
        Session session = getCMISSession();
        try {
            ContentStream contentStream = session.getObjectFactory().createContentStream(filename, -1, mimeType, content);
            CmisObject cmisObject = null;
            if (contentExists(cleanPath)) {
                cmisObject = session.getObjectByPath(cleanPath);
            }
            if (cmisObject != null) {
                ObjectType type = cmisObject.getBaseType();
                if ("cmis:document".equals(type.getId())) {
                    org.apache.chemistry.opencmis.client.api.Document document =
                            (org.apache.chemistry.opencmis.client.api.Document) cmisObject;
                    String pwcId = document.getVersionSeriesCheckedOutId();
                    if (pwcId != null) {
                        org.apache.chemistry.opencmis.client.api.Document pwcDocument =
                                (org.apache.chemistry.opencmis.client.api.Document) session.getObject(pwcId);
                        pwcDocument.checkIn(false, null, contentStream, null);
                    } else {
                        document.setContentStream(contentStream, true);
                    }
                    session.removeObjectFromCache(document.getId());
                    session.clear();
                }
            } else {
                String folderPath = cleanPath.substring(0, splitIndex);
                if (StringUtils.isEmpty(folderPath)) {
                    folderPath = "/";
                }
                CmisObject folderCmisObject = null;
                if (contentExists(folderPath)) {
                    folderCmisObject = session.getObjectByPath(folderPath);
                }
                Folder folder = null;
                if (folderCmisObject == null) {
                    // if not, create the folder first
                    boolean created = createMissingFoldersCMIS(folderPath);
                    if (created) {
                        folderCmisObject = session.getObjectByPath(folderPath);
                        folder = (Folder) folderCmisObject;
                    } else {
                        return false;
                    }
                } else {
                    folder = (Folder) folderCmisObject;
                }
                Map<String, Object> properties = new HashMap<String, Object>();
                properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
                properties.put(PropertyIds.NAME, filename);
                org.apache.chemistry.opencmis.client.api.Document newDoc = folder.createDocument(
                        properties, contentStream, VersioningState.MINOR);
                session.removeObjectFromCache(newDoc.getId());
                session.clear();
            }
            long duration = System.currentTimeMillis() - startTime;
            logger.debug("writeContentCMIS(fullPath = {0}, content = {1}); ({2} ms)", fullPath, "stream", duration);
            return true;
        } catch (CmisUnauthorizedException err) {
            logger.error("Error writing content to a path {0}", err, fullPath);
            throw new ServiceException(err);
        } catch (CmisBaseException e) {
            logger.error("Error writing content to a path {0}", e, fullPath);
        } catch (NullPointerException e) {
            logger.error("Error writing content to a path {0}", e, fullPath);
        } catch (Throwable t) {
            logger.error("Error writing content to a path {0}", t, fullPath);
        }
        long duration = System.currentTimeMillis() - startTime;
        logger.debug("writeContentCMIS(fullPath = {0}, content = {1}); ({2} ms)", fullPath, "stream", duration);
        return false;
    }

    protected boolean deleteContentCMIS(String fullPath) {
        long startTime = System.currentTimeMillis();
        boolean result = false;
        String cleanPath = fullPath.replaceAll("//", "/"); // sometimes sent bad paths
        if (cleanPath.endsWith("/")) {
            cleanPath = cleanPath.substring(0, cleanPath.length() - 1);
        }
        try {
            Session session = getCMISSession();
            CmisObject cmisObject = session.getObjectByPath(cleanPath);
            if (cmisObject == null) {
                // if no cmisObject, it's already deleted so just return true
                logger.debug("No content found at " + fullPath);
                result = true;
            } else {
                ObjectType type = cmisObject.getBaseType();
                if (BaseTypeId.CMIS_DOCUMENT.value().equals(type.getId())) {
                    org.apache.chemistry.opencmis.client.api.Document doc =
                        (org.apache.chemistry.opencmis.client.api.Document)cmisObject;
                    if (doc.isVersionSeriesCheckedOut()) {
                        doc.cancelCheckOut();
                    }
                }
                org.apache.chemistry.opencmis.client.util.FileUtils.delete(cmisObject.getId(), session);
                session.removeObjectFromCache(cmisObject.getId());
                result = true;
            }
        } catch (CmisBaseException e) {
            logger.error("Could not find content for path {0}", fullPath);
        }
        long duration = System.currentTimeMillis() - startTime;
        logger.debug("deleteContentCMIS(fullPath = {0}) ({1} ms)", fullPath, duration);
        return result;
    }

    protected VersionTO[] getContentVersionHistoryCMIS(String fullPath) {
        long startTime = System.currentTimeMillis();
        VersionTO[] versions = new VersionTO[0];
        String cleanPath = fullPath.replaceAll("//", "/"); // sometimes sent bad paths
        if (cleanPath.endsWith("/")) {
            cleanPath = cleanPath.substring(0, cleanPath.length() - 1);
        }
        try {

            Session session = getCMISSession();
            CmisObject cmisObject = session.getObjectByPath(cleanPath);
            if(cmisObject != null) {
                ObjectType type = cmisObject.getType();
                if ("cmis:document".equals(type.getId())) {
                    org.apache.chemistry.opencmis.client.api.Document doc =
                        (org.apache.chemistry.opencmis.client.api.Document)cmisObject;
                    List<org.apache.chemistry.opencmis.client.api.Document> versionsCMIS = doc.getAllVersions();
                    String currentVersion = doc.getVersionLabel();
                    int temp = currentVersion.indexOf(".");
                    String currentMajorVersion = currentVersion.substring(0, temp);
                    if (versionsCMIS != null && versionsCMIS.size() > 0) {
                        versions = new VersionTO[versionsCMIS.size()];
                    }
                    int idx = 0;
                    for (org.apache.chemistry.opencmis.client.api.Document version : versionsCMIS) {
                        VersionTO versionTO = new VersionTO();
                        String versionId = version.getVersionLabel();
                        boolean condition = !versionId.startsWith(currentMajorVersion) && !versionId.endsWith(".0");
                        if (condition) continue;
                        versionTO.setVersionNumber(version.getVersionLabel());
                        versionTO.setLastModifier(version.getLastModifiedBy());
                        versionTO.setLastModifiedDate(version.getLastModificationDate().getTime());
                        versionTO.setComment(version.getCheckinComment());

                        versions[idx++] = versionTO;
                    }
                }
            } else {
                logger.info("Content not found for path: [" + fullPath + "]");
            }
        } catch(CmisBaseException err) {
            logger.error("err getting content: ", err);
        }
        long duration = System.currentTimeMillis() - startTime;
        logger.debug("getContentVersionHistoryCMIS(fullPath = {0}) ({1} ms)", fullPath, duration);
        return versions;
    }

    protected boolean revertContentCMIS(String fullPath, String version, boolean major, String comment) {
        long startTime = System.currentTimeMillis();
        boolean success = false;
        String cleanPath = fullPath.replaceAll("//", "/"); // sometimes sent bad paths
        if (cleanPath.endsWith("/")) {
            cleanPath = cleanPath.substring(0, cleanPath.length() - 1);
        }
        try {
            Session session = getCMISSession();
            CmisObject cmisObject = session.getObjectByPath(cleanPath);
            if(cmisObject != null) {
                ObjectType type = cmisObject.getType();
                if ("cmis:document".equals(type.getId())) {
                    org.apache.chemistry.opencmis.client.api.Document doc =
                        (org.apache.chemistry.opencmis.client.api.Document)cmisObject;
                    List<org.apache.chemistry.opencmis.client.api.Document> versionsCMIS = doc.getAllVersions();
                    if (versionsCMIS != null && versionsCMIS.size() > 0) {
                        for (org.apache.chemistry.opencmis.client.api.Document documentVersion : versionsCMIS) {
                            if (version.equals(documentVersion.getVersionLabel())) {
                                ContentStream contentStream = documentVersion.getContentStream();
                                ObjectId checkoutId = doc.checkOut();
                                org.apache.chemistry.opencmis.client.api.Document checkedOutDoc =
                                    (org.apache.chemistry.opencmis.client.api.Document)session.getObject(checkoutId);

                                checkedOutDoc.checkIn(false, null, contentStream, comment);
                                success = true;
                                break;
                            }
                        }
                    }
                }
            }
        } catch (CmisBaseException err) {
            logger.error("err reverting content content: ", err);
        }
        long duration = System.currentTimeMillis() - startTime;
        logger.debug("revertContentCMIS(fullPath = {0}, version = {1}, major = {2}, comment = {3}) ({4} ms) ",
                     fullPath, version, major, comment, duration);
        return success;
    }

    protected boolean createMissingFoldersCMIS(String fullPath) {
        String newFolderRef = null;
        String cleanPath = fullPath.replaceAll("//", "/"); // sometimes sent bad paths
        if (cleanPath.length() > 1 && cleanPath.endsWith("/")) {
            cleanPath = cleanPath.substring(0, cleanPath.length() - 1);
        }
        try {
            int idx = cleanPath.lastIndexOf("/");
            String parentPath = "/";
            String name = "";
            if (idx >= 0) {
                parentPath = cleanPath.substring(0, idx);
                if (StringUtils.isEmpty(parentPath)) {
                    parentPath = "/";
                }
                name = cleanPath.substring(idx + 1);
            }
            if (org.apache.commons.lang3.StringUtils.isEmpty(name)) {
                return true;
            }
            Session session = getCMISSession();
            CmisObject parentCmisOBject = null;
            try {
                parentCmisOBject = session.getObjectByPath(parentPath);
            } catch (CmisObjectNotFoundException ex) {
                logger.info("Parent folder [{0}] not found, creating it.", cleanPath);
                createMissingFoldersCMIS(parentPath);
                session.clear();
                parentCmisOBject = session.getObjectByPath(parentPath);
            }
            if (parentCmisOBject != null) {
                ObjectType type = parentCmisOBject.getType();
                if ("cmis:folder".equals(type.getId())) {
                    Folder folder = (Folder)parentCmisOBject;
                    Map<String, String> newFolderProps = new HashMap<String, String>();
                    newFolderProps.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
                    newFolderProps.put(PropertyIds.NAME, name);
                    try {
                        Folder newFolder = folder.createFolder(newFolderProps);
                        Property property = newFolder.getProperty("alfcmis:nodeRef");
                        newFolderRef = property.getValueAsString();
                    } catch (CmisContentAlreadyExistsException exc) {
                        logger.info("Folder " + cleanPath + " already exists");
                    }
                }
            } else {
                logger.error("Failed to create " + name + " folder since " + fullPath + " does not exist.");
            }
            session.clear();
        } catch (CmisBaseException err) {
            logger.error("Failed to create  folder in {0}", err, fullPath);
        }
        return true;
    }

    protected String createFolderInternalCMIS(String fullPath, String name) {
        long startTime = System.currentTimeMillis();
        String newFolderRef = null;
        String cleanPath = fullPath.replaceAll("//", "/"); // sometimes sent bad paths
        if (cleanPath.length() > 1 && cleanPath.endsWith("/")) {
            cleanPath = cleanPath.substring(0, cleanPath.length() - 1);
        }
        try {
            Session session = getCMISSession();
            CmisObject parentCmisOBject = null;
            try {
                parentCmisOBject = session.getObjectByPath(cleanPath);
            } catch (CmisObjectNotFoundException ex) {
                logger.info("Parent folder [{0}] not found, creating it.", cleanPath);
                int idx = cleanPath.lastIndexOf("/");
                if (idx >= 0) {
                    String ancestorPath = cleanPath.substring(0, idx);
                    if (StringUtils.isEmpty(ancestorPath)) {
                        ancestorPath = "/";
                    }
                    String parentName = cleanPath.substring(idx + 1);
                    String nodeRef = createFolderInternalCMIS(ancestorPath, parentName);
                    if (StringUtils.isEmpty(nodeRef)) {
                        logger.error("Failed to create " + name + " folder since " + fullPath + " does not exist.");
                        session.clear();
                        return newFolderRef;
                    } else {
                        parentCmisOBject = session.getObjectByPath(cleanPath);
                    }
                }
            }
            if (parentCmisOBject != null) {
                ObjectType type = parentCmisOBject.getType();
                if ("cmis:folder".equals(type.getId())) {
                    Folder folder = (Folder)parentCmisOBject;
                    Map<String, String> newFolderProps = new HashMap<String, String>();
                    newFolderProps.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
                    newFolderProps.put(PropertyIds.NAME, name);
                    Folder newFolder = folder.createFolder(newFolderProps);
                    Property property = newFolder.getProperty("alfcmis:nodeRef");
                    newFolderRef = property.getValueAsString();
                    session.removeObjectFromCache(newFolder.getId());
                }
            } else {
                logger.error("Failed to create " + name + " folder since " + fullPath + " does not exist.");
            }
            session.clear();
        } catch (CmisBaseException err) {
            logger.error("Failed to create " + name + " folder in {0}", err, fullPath);
        }
        long duration = System.currentTimeMillis() - startTime;
        logger.debug("createFolderInternalCMIS(fullPath = {0}, name = {1}) ({2} ms)", fullPath, name, duration);
        return newFolderRef;
    }

    protected boolean copyContentInternalCMIS(String fromFullPath, String toFullPath, String newName, boolean isCut) {
        long startTime = System.currentTimeMillis();
        boolean result = false;
        String cleanFromPath = fromFullPath.replaceAll("//", "/"); // sometimes sent bad paths
        if (cleanFromPath.endsWith("/")) {
            cleanFromPath = cleanFromPath.substring(0, cleanFromPath.length() - 1);
        }
        String cleanToPath = toFullPath.replaceAll("//", "/"); // sometimes sent bad paths
        if (cleanToPath.endsWith("/")) {
            cleanToPath = cleanToPath.substring(0, cleanToPath.length() - 1);
        }

        try {
            Session session = getCMISSession();
            CmisObject sourceCmisObject = session.getObjectByPath(cleanFromPath);
            CmisObject targetCmisObject = session.getObjectByPath(cleanToPath);

            if (sourceCmisObject != null && targetCmisObject != null) {
                ObjectType sourceType = sourceCmisObject.getType();
                ObjectType targetType = targetCmisObject.getType();
                if (BaseTypeId.CMIS_FOLDER.value().equals(targetType.getId())) {
                    Folder targetFolder = (Folder)targetCmisObject;
                    if ("cmis:document".equals(sourceType.getId())) {
                        org.apache.chemistry.opencmis.client.api.Document sourceDocument =
                            (org.apache.chemistry.opencmis.client.api.Document)sourceCmisObject;
                        logger.debug("Coping document {0} to {1}", sourceDocument.getPaths().get(0), targetFolder.getPath());
                        copyDocument(targetFolder, newName, sourceDocument);
                    } else if ("cmis:folder".equals(sourceType.getId())) {
                        Folder sourceFolder = (Folder)sourceCmisObject;
                        logger.debug("Coping folder {0} to {1}", sourceFolder.getPath(), targetFolder.getPath());
                        if (newName != null) {
                            copyFolder(targetFolder, sourceFolder);
                        } else {
                            copyChildren(targetFolder, sourceFolder);
                        }
                    }
                    if (isCut) {
                        deleteContentCMIS(cleanFromPath);
                    }
                    session.clear();
                    long duration = System.currentTimeMillis() - startTime;
                    logger.debug("copyContentInternalCMIS(fromFullPath = {0}, toFullPath = {1}, isCut = {2}) ({3} ms)",
                                 fromFullPath, toFullPath, isCut, duration);
                    return true;
                } else {
                    logger.error((isCut ? "Move" : "Copy") + " failed since target path " + toFullPath +
                                 " is not folder.");
                }
            } else {
                if (sourceCmisObject == null) {
                    logger.error((isCut ? "Move" : "Copy") + " failed since source path " + fromFullPath +
                                 " does not exist.");
                }
                if (targetCmisObject == null) {
                    logger.error((isCut ? "Move" : "Copy") + " failed since target path " + toFullPath +
                                 " does not exist.");
                }
            }
        } catch (CmisBaseException err) {
            logger.error("Error while " + (isCut ? "moving" : "copying") + " content from " + fromFullPath + " to " +
                         toFullPath, err);
        }
        long duration = System.currentTimeMillis() - startTime;
        logger.debug("copyContentInternalCMIS(fromFullPath = {0}, toFullPath = {1}, isCut = {2}) ({3} ms)",
                     fromFullPath, toFullPath, isCut, duration);
        return result;
    }

    private void copyFolder(Folder parentFolder, Folder toCopyFolder) {
        Map<String, Object> folderProperties = new HashMap<String, Object>(2);
        folderProperties.put(PropertyIds.NAME, toCopyFolder.getName());
        folderProperties.put(PropertyIds.OBJECT_TYPE_ID, toCopyFolder.getBaseTypeId().value());
        Folder newFolder = parentFolder.createFolder(folderProperties);
        copyChildren(newFolder, toCopyFolder);
    }

    private void copyChildren(Folder parentFolder, Folder toCopyFolder) {
        ItemIterable<CmisObject> immediateChildren = toCopyFolder.getChildren();
        for (CmisObject child : immediateChildren) {
            if (BaseTypeId.CMIS_DOCUMENT.value().equals(child.getBaseTypeId().value())) {
                copyDocument(parentFolder, null, (org.apache.chemistry.opencmis.client.api.Document) child);
            } else if (BaseTypeId.CMIS_FOLDER.value().equals(child.getBaseTypeId().value())) {
                copyFolder(parentFolder, (Folder) child);
            }
        }
    }

    private void copyDocument(Folder parentFolder, String newName,
                              org.apache.chemistry.opencmis.client.api.Document sourceDocument) {
        Map<String, Object> documentProperties = new HashMap<String, Object>(2);
        if (StringUtils.isEmpty(newName)) {
            documentProperties.put(PropertyIds.NAME, sourceDocument.getName());
        } else {
            documentProperties.put(PropertyIds.NAME, newName);
        }
        documentProperties.put(PropertyIds.OBJECT_TYPE_ID, sourceDocument.getBaseTypeId().value());
        parentFolder.createDocument(documentProperties, sourceDocument.getContentStream(), VersioningState.MINOR);
    }

    protected Session getCMISSession() {
        return createCMISSession(true);
    }

    protected Session createCMISSession(boolean alfrescoCMIS) {
        // Create a SessionFactory and set up the SessionParameter map
        SessionFactory sessionFactory = SessionFactoryImpl.newInstance();
        Map<String, String> parameter = new HashMap<String, String>();

        String ssoUsername = getSsoUsername();
        if (StringUtils.isNotEmpty(ssoUsername)) {
            parameter.put(SessionParameter.HEADER + ".0", alfrescoExternalAuthHeaderName + ":" + ssoUsername);
        } else {
            parameter.put(SessionParameter.USER, "ROLE_TICKET");
            parameter.put(SessionParameter.PASSWORD, getAlfTicket());
        }

        // connection settings - we're connecting to a public cmis repo,
        // using the AtomPUB binding, but there are other options here,
        // or you can substitute your own URL
        parameter.put(SessionParameter.ATOMPUB_URL, alfrescoUrl + cmisPath);
        parameter.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());

        // find all the repositories at this URL - there should only be one.
        List<Repository> repositories = new ArrayList<Repository>();
        repositories = sessionFactory.getRepositories(parameter);

        // create session with the first (and only) repository
        Repository repository = repositories.get(0);
        parameter.put(SessionParameter.REPOSITORY_ID, repository.getId());
        Session session = sessionFactory.createSession(parameter);

        return session;
    }

    public void lockItem(String site, String path) {
        String fullPath = expandRelativeSitePath(site, path);
        lockItemCMIS(fullPath);
    }

    protected void lockItemCMIS(String fullPath) {
        long startTime = System.currentTimeMillis();
        Session session = getCMISSession();
        String cleanPath = fullPath.replaceAll("//", "/"); // sometimes sent bad paths
        if (cleanPath.endsWith("/")) {
            cleanPath = cleanPath.substring(0, cleanPath.length() - 1);
        }
        try {
            CmisObject cmisObject = session.getObjectByPath(cleanPath);
            ObjectType type = cmisObject.getBaseType();
            if (BaseTypeId.CMIS_DOCUMENT.value().equals(type.getId())) {
                org.apache.chemistry.opencmis.client.api.Document document =
                    (org.apache.chemistry.opencmis.client.api.Document) cmisObject;
                if (!document.isVersionSeriesCheckedOut()) {
                    document.checkOut();
                }
            }
        } catch (CmisBaseException err) {
            logger.error("Error while locking content at path " + cleanPath, err);
        } catch (Throwable err) {
            logger.error("Error while locking content at path " + cleanPath, err);

        }
        long duration = System.currentTimeMillis() - startTime;
        logger.debug("lockItemCMIS(fullPath = {0}) ({1} ms)", fullPath, duration);
    }

    protected String expandRelativeSitePath(String site, String relativePath) {
        return "/wem-projects/" + site + "/" + site + "/work-area" + relativePath;
    }

    public void unLockItem(String site, String path) {
        long startTime = System.currentTimeMillis();
        String fullPath = expandRelativeSitePath(site, path);
        Session session = getCMISSession();
        String cleanPath = fullPath.replaceAll("//", "/"); // sometimes sent bad paths
        if (cleanPath.endsWith("/")) {
            cleanPath = cleanPath.substring(0, cleanPath.length() - 1);
        }
        if (contentExists(cleanPath)) {
            try {
                CmisObject cmisObject = session.getObjectByPath(cleanPath);
                ObjectType type = cmisObject.getBaseType();
                if (BaseTypeId.CMIS_DOCUMENT.value().equals(type.getId())) {
                    org.apache.chemistry.opencmis.client.api.Document document =
                        (org.apache.chemistry.opencmis.client.api.Document) cmisObject;
                    String pwcId = document.getVersionSeriesCheckedOutId();
                    if (StringUtils.isNotEmpty(pwcId)) {
                        org.apache.chemistry.opencmis.client.api.Document pwcDocument =
                            (org.apache.chemistry.opencmis.client.api.Document) session.getObject(pwcId);
                        if (pwcDocument != null) {
                            pwcDocument.cancelCheckOut();
                        }
                    }
                }
            } catch (CmisBaseException err) {
                logger.error("Error while unlocking content at path " + cleanPath, err);
            } catch (Throwable err) {
                logger.error("Error while unlocking content at path " + cleanPath, err);
            }
        }
        long duration = System.currentTimeMillis() - startTime;
        logger.debug("unLockItem(site = {0}, path = {1}) ({2} ms)", site, path, duration);
    }

    @Override
    public void addUserGroup(String groupName) {
        String newGroupRequestBody = "{ \"displayName\":\""+groupName+"\"}";
        try {
            InputStream bodyStream = IOUtils.toInputStream(newGroupRequestBody, "UTF-8");
            alfrescoPostRequest("/api/rootgroups/" + groupName, null, bodyStream, "application/json");
        }
        catch(Exception err) {
            logger.error("err adding root group: " + groupName, err);
        }
    }

    @Override
    public void addUserGroup(String parentGroup, String groupName) {
        String newGroupRequestBody = "{ \"displayName\":\""+groupName+"\"}";
        try {
            InputStream bodyStream = IOUtils.toInputStream(newGroupRequestBody, "UTF-8");
            alfrescoPostRequest("/api/groups/" + parentGroup + "/children/GROUP_" + groupName, null, bodyStream,
                                "application/json");
        }
        catch(Exception err) {
            logger.error("err adding group: " + groupName + " to parent group: " + parentGroup, err);
        }
    }

    @Override
    public void addUserToGroup(String groupName, String user) {
        String addUserToGroupRequestBody = "{ \"displayName\":\""+user+"\"}";
        try {
            InputStream bodyStream = IOUtils.toInputStream(addUserToGroupRequestBody, "UTF-8");
            alfrescoPostRequest("/api/groups/" + groupName + "/children/" + user, null, bodyStream, "application/json");
        }
        catch(Exception err) {
            logger.error("err adding user: " + user + " to parent group: " + groupName, err);
        }
    }

    @Override
    public String getCurrentToken() {
        String ssoUsername = getSsoUsername();
        if (StringUtils.isNotEmpty(ssoUsername)) {
            return SSO_TICKET_PREFIX + ssoUsername;
        } else {
            return getCurrentTicket();
        }
    }

    protected String getCurrentTicket() {
        String ticket;
        RequestContext context = RequestContext.getCurrent();

        if (context != null) {
            HttpSession httpSession = context.getRequest().getSession();
            ticket = (String)httpSession.getAttribute("alf_ticket");
        } else {
            ticket = getJobOrEventTicket();
        }

        if (ticket == null) {
            ticket = "NOTICKET";
        }

        return ticket;
    }

    protected void storeSessionTicket(String ticket) {
        RequestContext context = RequestContext.getCurrent();

        if(context != null) {
            HttpSession httpSession = context.getRequest().getSession();
            httpSession.setAttribute("alf_ticket", ticket);
        }        
    }
    
    protected String getSessionUsername() {
        String username = null;
        RequestContext context = RequestContext.getCurrent();

        if(context != null) {
            HttpSession httpSession = context.getRequest().getSession();
            username = (String)httpSession.getAttribute("alf_user");
        }

        return username;
    }

    protected void storeSessionUsername(String username) {
        RequestContext context = RequestContext.getCurrent();

        if(context != null) {
            HttpSession httpSession = context.getRequest().getSession();
            httpSession.setAttribute("alf_user", username);
        }
    }

    protected String getSsoUsername() {
        if (ssoEnabled) {
            String username = null;
            RequestContext context = RequestContext.getCurrent();

            if (context != null) {
                username = context.getRequest().getHeader(ssoHeaderName);
            } else {
                String ticket = getJobOrEventTicket();
                if (StringUtils.isNotEmpty(ticket)) {
                    username = StringUtils.substringAfter(ticket, SSO_TICKET_PREFIX);
                }
            }

            if (StringUtils.isNotEmpty(username)) {
                if (ssoUsernamePattern != null) {
                    username = extractSsoUsername(username);
                }

                logger.debug("SSO username found: {0}", username);
            }

            return username;
        } else {
            return null;
        }
    }

    protected String extractSsoUsername(String username) {
        Matcher matcher = ssoUsernamePattern.matcher(username);
        if (matcher.matches()) {
            return matcher.group(1);
        } else {
            return username;
        }
    }

    protected String getJobOrEventTicket() {
        String ticket = null;
        CronJobContext cronJobContext = CronJobContext.getCurrent();

        if (cronJobContext != null) {
            ticket = cronJobContext.getAuthenticationToken();
        } else {
            RepositoryEventContext repositoryEventContext = RepositoryEventContext.getCurrent();
            if (repositoryEventContext != null) {
                ticket = repositoryEventContext.getAuthenticationToken();
            }
        }

        return ticket;
    }

    @Override
    public Date getModifiedDate(String fullPath) {
        long startTime = System.currentTimeMillis();
        String cleanPath = fullPath.replaceAll("//", "/"); // sometimes sent bad paths
        if (cleanPath.endsWith("/")) {
            cleanPath = cleanPath.substring(0, cleanPath.length() - 1);
        }
        Date modifiedDate = null;
        try {
            Session session = getCMISSession();
            CmisObject cmisObject = session.getObjectByPath(cleanPath);
            if (cmisObject != null) {
                modifiedDate = cmisObject.getLastModificationDate().getTime();
            }
        } catch (CmisBaseException e) {
            logger.error("Error getting content from CMIS repository for path: ", e, fullPath);
        }
        long duration = System.currentTimeMillis() - startTime;
        logger.debug("getModifiedDate(fullPath = {0}) ({1} ms)", fullPath, duration);
        return modifiedDate;
    }

    @Override
    public boolean logout() {
        if (StringUtils.isEmpty(getSsoUsername())) {
            long startTime = System.currentTimeMillis();

            //make me do something
            String ticket = getCurrentTicket();

            logger.debug("Invalidating ticket " + ticket);
            Map<String, String> params = new HashMap<>();
            params.put("ticket", ticket);

            DeleteMethod method = null;
            String serviceURL;

            try {
                serviceURL = buildAlfrescoRequestURL("/api/login/ticket/{ticket}", params);
                method = new DeleteMethod(serviceURL);

                int status = httpClient.executeMethod(method);
                if (status == HttpStatus.SC_OK) {
                    long duration = System.currentTimeMillis() - startTime;
                    logger.debug("logout() ({0} ms)", duration);

                    return true;
                }
            } catch (Exception e) {
                logger.error("Error while invalidating authentication token", e);
            } finally {
                if (method != null) {
                    method.releaseConnection();
                }
            }

            long duration = System.currentTimeMillis() - startTime;
            logger.debug("logout() ({0} ms)", duration);

            return false;
        } else {
            return true;
        }
    }

    /**
     * bootstrap the repository
     */
    public void bootstrap() throws Exception {
        if (bootstrapEnabled) {
            String ticket = authenticate(adminUser, adminPassword);
            RepositoryEventContext repositoryEventContext = new RepositoryEventContext(ticket);
            RepositoryEventContext.setCurrent(repositoryEventContext);

            if (!bootstrapCheck()) {
                logger.debug("Bootstrapping repository for Crafter CMS");

                String bootstrapFolderPath = getBootstrapFolderPath();
                bootstrapFolderPath = bootstrapFolderPath + (File.separator + "repo-bootstrap");
                File source = new File(bootstrapFolderPath);
                bootstrapDir(source, bootstrapFolderPath);
                addUserGroup("CRAFTER_CREATE_SITES");
                addUserToGroup("CRAFTER_CREATE_SITES", adminUser);
            }

            RepositoryEventContext.setCurrent(null);
        }
    }

    private void bootstrapDir(File dir, String rootPath) {
        Collection<File> children = FileUtils.listFilesAndDirs(dir, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
        for (File child : children) {
            String childPath = child.getAbsolutePath();
            logger.debug("BOOTSTRAP Processing path: {0}", childPath);
            if (!rootPath.equals(childPath)) {
                String relativePath = childPath.replace(rootPath, "");
                relativePath = relativePath.replace(File.separator, "/");
                String parentPath = child.getParent().replace(rootPath, "");
                parentPath = parentPath.replace(File.separator, "/");
                if (StringUtils.isEmpty(parentPath)) {
                    parentPath = "/";
                }
                if (child.isDirectory()) {
                    createFolderInternalCMIS(parentPath, child.getName());
                } else if (child.isFile()) {
                    try {
                        writeContentCMIS(relativePath, FileUtils.openInputStream(child));
                    } catch (IOException | ServiceException e) {
                        logger.error("Error while bootstrapping file: " + relativePath, e);
                    }
                }
            }
        }
    }

    private boolean bootstrapCheck() {
        boolean contentSpace = contentExists("/wem-projects");
        boolean blueprintsSpace = contentExists("/cstudio/blueprints");
        boolean configSpace = contentExists("/cstudio/config");
        return contentSpace && blueprintsSpace && configSpace;
    }

    private String getBootstrapFolderPath() {
        String path = this.getClass().getClassLoader().getResource("").getPath();
        String fullPath = null;
        try {
            fullPath = URLDecoder.decode(path, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return null;
        }
        String pathArr[] = fullPath.split("/WEB-INF/classes/");
        fullPath = pathArr[0];

        String responsePath = "";
        responsePath = new File(fullPath).getPath();
        return responsePath;
    }

    @Override
    public void addContentWritePermission(String path, String group) {
        setWritePermission(path, group);
    }

    private void setWritePermission(String path, String group) {
        Session session = getCMISSession();
        String cleanPath = path.replaceAll("//", "/"); // sometimes sent bad paths
        if (cleanPath.endsWith("/")) {
            cleanPath = cleanPath.substring(0, cleanPath.length() - 1);
        }
        if (contentExists(cleanPath)) {
            try {
                CmisObject cmisObject = session.getObjectByPath(cleanPath);

                List<String> permissions = new LinkedList<String>();
                permissions.add("{http://www.alfresco.org/model/content/1.0}cmobject.Coordinator");
                Ace addAce = session.getObjectFactory().createAce("GROUP_"+group, permissions);
                List<Ace> addAces = new LinkedList<Ace>();
                addAces.add(addAce);
                cmisObject.addAcl(addAces, AclPropagation.PROPAGATE);
            } catch (CmisBaseException err) {
                logger.error("Error while setting permissions for content at path " + cleanPath, err);
            } catch (Throwable err) {
                logger.error("Error while setting permissions for content at path " + cleanPath, err);
            }
        }
    }

    protected String cleanPath(String path) {
        String cleanPath = path.replaceAll("//", "/"); // sometimes sent bad paths

        if (cleanPath.endsWith("/")) {
            cleanPath = cleanPath.substring(0, cleanPath.length() - 1);
        }

        return cleanPath;
    }

    @Override
    public void addConfigWritePermission(String path, String group) {
        setWritePermission(path, group);
    }

    protected String alfrescoUrl;
    protected String servicePath = "/s";
    protected String ssoServicePath = "/wcs";
    protected String cmisPath = "/api/-default-/public/cmis/versions/1.1/atom/";
    protected String adminUser;
    protected String adminPassword;
    protected boolean ssoEnabled;
    protected String ssoHeaderName;
    protected Pattern ssoUsernamePattern;
    protected String alfrescoExternalAuthHeaderName;
    protected boolean bootstrapEnabled = false;

    public String getAlfrescoUrl() {
        return alfrescoUrl;
    }

    public void setAlfrescoUrl(String url) {
        alfrescoUrl = url;
    }

    public String getServicePath() {
        return servicePath;
    }

    public void setServicePath(String url) {
        servicePath = url;
    }

    public String getSsoServicePath() {
        return ssoServicePath;
    }

    public void setSsoServicePath(String ssoServicePath) {
        this.ssoServicePath = ssoServicePath;
    }

    public String getCmisPath() {
        return cmisPath;
    }

    public void setCmisPath(String url) {
        cmisPath = url;
    }

    public String getAdminUser() {
        return adminUser;
    }

    public void setAdminUser(String adminUser) {
        this.adminUser = adminUser;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }

    public boolean isSsoEnabled() {
        return ssoEnabled;
    }

    public void setSsoEnabled(boolean enabled) {
        this.ssoEnabled = enabled;
    }

    public String getSsoHeaderName() {
        return ssoHeaderName;
    }

    public void setSsoHeaderName(String headerName) {
        this.ssoHeaderName = headerName;
    }

    public String getSsoUsernamePattern() {
        return ssoUsernamePattern.pattern();
    }

    public void setSsoUsernamePattern(String ssoUsernamePattern) {
        if (StringUtils.isNotEmpty(ssoUsernamePattern)) {
            this.ssoUsernamePattern = Pattern.compile(ssoUsernamePattern);
        }
    }

    public String getAlfrescoExternalAuthHeaderName() {
        return alfrescoExternalAuthHeaderName;
    }

    public void setAlfrescoExternalAuthHeaderName(String alfrescoExternalAuthHeaderName) {
        this.alfrescoExternalAuthHeaderName = alfrescoExternalAuthHeaderName;
    }

    public boolean isBootstrapEnabled() {
        return bootstrapEnabled;
    }

    public void setBootstrapEnabled(boolean bootstrapEnabled) {
        this.bootstrapEnabled = bootstrapEnabled;
    }

}

