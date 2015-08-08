/*******************************************************************************
 * Crafter Studio Web-content authoring solution
 *     Copyright (C) 2007-2013 Crafter Software Corporation.
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

import com.fasterxml.jackson.databind.ObjectMapper;
import net.sf.json.JSONObject;
import org.apache.chemistry.opencmis.client.api.*;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.String;
import java.nio.file.Files;
import java.util.*;
import java.net.*;
import java.net.URI;
import java.util.HashMap;
import javax.activation.MimetypesFileTypeMap;
import javax.servlet.http.*;

import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.craftercms.commons.http.*;
import org.craftercms.studio.api.v1.ebus.RepositoryEventContext;
import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.job.CronJobContext;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.repository.RepositoryItem;
import org.craftercms.studio.api.v1.service.security.SecurityProvider;
import org.craftercms.studio.api.v1.to.VersionTO;
import org.craftercms.studio.impl.v1.repository.AbstractContentRepository;
import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import reactor.core.Reactor;


/**
 * Alfresco repository implementation.  This is the only point of contact with Alfresco's API in
 * the entire system under the org.craftercms.cstudio.impl package structure
 * @author russdanner
 *
 */
public class AlfrescoContentRepository extends AbstractContentRepository 
implements SecurityProvider {

    private static final Logger logger = LoggerFactory.getLogger(AlfrescoContentRepository.class);

    @Override
    public InputStream getContent(String path) throws ContentNotFoundException {
        return getContentStreamCMIS(path);
    }

    @Override
    public boolean contentExists(String path) {
        try {
            return (this.getNodeRefForPathCMIS(path) != null);
        } catch (ContentNotFoundException e) {
            logger.info("Content not found exception for path: " + path, e);
            return false;
        }
    }


    @Override
    public boolean writeContent(String path, InputStream content) {
        logger.debug("writing content to " + path);
        addDebugStack();
        return writeContentCMIS(path, content);
    }

    @Override
    public boolean createFolder(String path, String name) {
        addDebugStack();
        String folderRef = this.createFolderInternal(path, name);
        return folderRef != null;
    }

    @Override
    public boolean deleteContent(String path) {
        logger.debug("deleting content at " + path);
        addDebugStack();
        return deleteContentCMIS(path);
    }

    @Override
    public boolean copyContent(String fromPath, String toPath) {
        addDebugStack();
        return this.copyContentInternal(fromPath, toPath, false);
    }


    @Override
    public boolean moveContent(String fromPath, String toPath) {
        addDebugStack();
        return this.copyContentInternal(fromPath, toPath, true);
    };

    /**
     * get immediate children for path
     * @param path path to content
     */
    public RepositoryItem[] getContentChildren(String path) {
        addDebugStack();
        RepositoryItem[] items = getContentChildrenCMIS(path);
        return items;
    }

    /**
     * create a version
     * @param path location of content
     * @param majorVersion true if major
     * @return the created version ID or null on failure
     */
    @Override
    public String createVersion(String path, boolean majorVersion) {

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
                        org.apache.chemistry.opencmis.client.api.Document document = (org.apache.chemistry.opencmis.client.api.Document) cmisObject;
                        ObjectId objId = document.checkOut();
                        org.apache.chemistry.opencmis.client.api.Document workingCopy = (org.apache.chemistry.opencmis.client.api.Document) session.getObject(objId);
                        ContentStream contentStream = workingCopy.getContentStream();
                        objId = workingCopy.checkIn(majorVersion, null, contentStream, null);
                        session.removeObjectFromCache(document.getId());
                        session.removeObjectFromCache(objId);
                    }
                }
            } catch (CmisBaseException err) {
                logger.error("Error while creating new " + (majorVersion ? "major" : "minor") + " version for path " + path, err);
            }
        }
        return versionLabel;

    }

    /** 
     * get the version history for an item
     * @param path - the path of the item
     */
    public VersionTO[] getContentVersionHistory(String path) {
        addDebugStack();
        return getContentVersionHistoryCMIS(path);
    }

    /** 
     * revert a version (create a new version based on an old version)
     * @param path - the path of the item to "revert"
     * @param version - old version ID to base to version on
     */
    public boolean revertContent(String path, String version, boolean major, String comment) {
        addDebugStack();
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
        return createFolderInternalCMIS(path, name);
    }

    /**
     * copy content from pathA to pathB
     * @param fromPath
     *             the source path
     * @param toPath
     *              the target path
     * @param isCut
     *              true for move
     * @return  true if successful
     */
    protected boolean copyContentInternal(String fromPath, String toPath, boolean isCut) {
        logger.debug((isCut ? "Move" : "Copy") + " content from " + fromPath + " to " + toPath);
        return copyContentInternalCMIS(fromPath, toPath, isCut);
    }


    /**
     * fire GET request to Alfresco with proper security
     */
    protected InputStream alfrescoGetRequest(String uri, Map<String, String> params) throws Exception {
        InputStream retResponse = null;

        URI serviceURI = new URI(buildAlfrescoRequestURL(uri, params));        

        retResponse = serviceURI.toURL().openStream();

        return retResponse;
    }

    /**
     * fire POST request to Alfresco with propert security
     */
    protected String alfrescoPostRequest(String uri, Map<String, String> params, InputStream body, String bodyMimeType) throws Exception {
        String serviceURL = buildAlfrescoRequestURL(uri, params);
        PostMethod postMethod = new PostMethod(serviceURL);
        postMethod.setRequestEntity(new InputStreamRequestEntity(body, bodyMimeType));

        HttpClient httpClient = new HttpClient(new MultiThreadedHttpConnectionManager());
        int status = httpClient.executeMethod(postMethod);

        return postMethod.getResponseBodyAsString();
    }

    /**
     * create a multipart post request and fire to Alfresco
     *
     * @param uri
     *          the target service URI
     * @param params
     *          request parameters
     * @param body
     *          post data
     * @param bodyMimeType
     *          post data mime type
     * @param charSet
     *          post data char set
     * @return response body
     * @throws Exception
     */
    protected String alfrescoMultipartPostRequest(String uri, Map<String, String> params, InputStream body, String bodyMimeType, String charSet) throws Exception {
        String serviceURL = buildAlfrescoRequestURL(uri, new HashMap<String, String>(0));
        PostMethod postMethod = new PostMethod(serviceURL);
        // create multipart request parts
        int partSize = params.size() + 1;
        Part[] parts = new Part[partSize];
        int index = 0;
        for (String key : params.keySet()) {
            parts[index] = new StringPart(key, params.get(key));
            index++;
        }
        byte[] bytes = IOUtils.toByteArray(body);
        String name = params.get("filename");
        PartSource partSource = new ByteArrayPartSource(name, bytes);
        parts[index] = new FilePart("filedata", partSource, bodyMimeType, charSet);

        postMethod.setRequestEntity(new MultipartRequestEntity(parts, postMethod.getParams()));

        // connect to alfresco and get response
        HttpClient httpClient = new HttpClient(new MultiThreadedHttpConnectionManager());
        logger.debug("Executing multipart post request to " + uri);
        int status = httpClient.executeMethod(postMethod);
        logger.debug("Response status back from the server: " + status);
        return postMethod.getResponseBodyAsString();
    }

    /**
     * build request URLs 
     */
    protected String buildAlfrescoRequestURL(String uri, Map<String, String> params) throws Exception {
        String url = "";
        String serviceUrlBase = alfrescoUrl+"/service";
        String ticket = getAlfTicket();

        if(params != null) {
            for(String key : params.keySet()) {
                uri = uri.replace("{"+key+"}", URLEncoder.encode(params.get(key), "utf-8"));
            }
        }

        url = serviceUrlBase + uri;
        url += (url.contains("?")) ? "&alf_ticket="+ticket : "?alf_ticket="+ticket;
        
        return url;
    }

    /**
     * Get the alfresco ticket from the URL or the cookie or from an authorinization
     */
    public String getAlfTicket() {
        return this.getSessionTicket();
    }

    @Override
    public Map<String, String> getUserProfile(String username) {
        addDebugStack();
        InputStream retStream = null;
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

            retStream = this.alfrescoGetRequest(downloadURI, lookupContentParams);
 
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> result = objectMapper.readValue(retStream, HashMap.class);

            toRet.put("userName", (String)result.get("userName"));
            toRet.put("firstName", (String)result.get("firstName"));
            toRet.put("lastName", (String)result.get("lastName"));
            toRet.put("email", (String)result.get("email"));
            //toRet.put("profileImage", result.get("NOTSET"));
        }
        catch(Exception err) {
            logger.error("err getting user profile: ", err);
        }

        return toRet;
    }

    @Override
    public Set<String> getUserGroups(String username) {
        addDebugStack();
        InputStream retStream = null;
        Set<String> toRet = new HashSet<String>();
        try {
            // construct and execute url to download result
            // TODO: use alfresco/service/api/sites/craftercms250/memberships/admin instead
            String downloadURI = "/api/people/{username}?groups=true";
            Map<String, String> lookupContentParams = new HashMap<String, String>();
            lookupContentParams.put("username", username);

            retStream = this.alfrescoGetRequest(downloadURI, lookupContentParams);

            ///JSONObject jsonObject =
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> result = objectMapper.readValue(retStream, HashMap.class);

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
        addDebugStack();
        String username = this.getSessionUsername();
        return username;
    }

    @Override
    public String authenticate(String username, String password) {
        InputStream retStream = null;
        String toRet = null;
        try {
            // construct and execute url to download result
            String downloadURI = "/api/login?u={u}&pw={pw}";
            Map<String, String> lookupContentParams = new HashMap<String, String>();
            lookupContentParams.put("u", username);
            lookupContentParams.put("pw", password);

            retStream = this.alfrescoGetRequest(downloadURI, lookupContentParams);

            SAXReader reader = new SAXReader();
            Document response = reader.read(retStream);
            Node ticketNode = response.selectSingleNode("//ticket");
            toRet = ticketNode.getText();   
     
            this.storeSessionTicket(toRet);
            this.storeSessionUsername(username);                 
        }
        catch(Exception err) {
            logger.error("err getting content: ", err);
        }
        return toRet;
    }

    @Override
    public boolean validateTicket(String ticket) {
        //make me do something
        ticket = (ticket!=null) ? ticket : getSessionTicket();
        logger.debug("Validating ticket " + ticket);
        Map<String, String> params = new HashMap<>();
        params.put("ticket", ticket);
        String serviceURL = null;
        try {
            serviceURL = buildAlfrescoRequestURL("/api/login/ticket/{ticket}", params);
            GetMethod getMethod = new GetMethod(serviceURL);
            HttpClient httpClient = new HttpClient(new MultiThreadedHttpConnectionManager());
            int status = httpClient.executeMethod(getMethod);
            if (status == HttpStatus.SC_OK) {
                return true;
            }
        } catch (Exception e) {
            logger.error("Error while validating authentication token", e);
        }
        return false;
    }

    private void addDebugStack() {
        if (logger.getLevel().equals(Logger.LEVEL_DEBUG)) {
            Thread thread = Thread.currentThread();
            String threadName = thread.getName();
            logger.debug("Thread: " + threadName);
            StackTraceElement[] stackTraceElements = thread.getStackTrace();
            StringBuilder sbStack = new StringBuilder();
            int stackSize = (10 < stackTraceElements.length-2) ? 10 : stackTraceElements.length;
            for (int i = 2; i < stackSize+2; i++){
                sbStack.append("\n\t").append(stackTraceElements[i].toString());
            }
            RequestContext context = RequestContext.getCurrent();
            CronJobContext cronJobContext = CronJobContext.getCurrent();
            if (context != null) {
                HttpServletRequest request = context.getRequest();
                String url = request.getRequestURI() + "?" + request.getQueryString();
                logger.debug("Http request: " + url);
            } else if (cronJobContext != null) {
                logger.debug("Cron Job");

            }
            logger.debug("Stack trace (depth 10): " + sbStack.toString());
        }
    }

    protected String getNodeRefForPathCMIS(String fullPath) throws ContentNotFoundException {
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
            logger.warn("Object not found in CMIS repository for path: ", fullPath);
            throw new ContentNotFoundException(e);
        }
        return nodeRef;
    }

    protected InputStream getContentStreamCMIS(String fullPath) throws ContentNotFoundException {
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
                    org.apache.chemistry.opencmis.client.api.Document document = (org.apache.chemistry.opencmis.client.api.Document) cmisObject;
                    ContentStream contentStream = document.getContentStream();
                    inputStream = contentStream.getStream();
                }
            }
        } catch (CmisBaseException e) {
            logger.error("Error getting content from CMIS repository for path: ", e, fullPath);
            throw new ContentNotFoundException(e);
        }
        return inputStream;
    }

    protected RepositoryItem[] getContentChildrenCMIS(String fullPath) {
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
                        org.apache.chemistry.opencmis.client.api.Document document = (org.apache.chemistry.opencmis.client.api.Document)child;
                        item.path = document.getPaths().get(0);
                        Property<?> secundaryTypes = document.getProperty(PropertyIds.SECONDARY_OBJECT_TYPE_IDS);
                        if (secundaryTypes != null) {
                            List<String> aspects = secundaryTypes.getValue();
                            if (aspects.contains("P:cm:workingcopy")) {
                                isWorkingCopy = true;
                            }
                        }
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
        return items;
    }

    protected boolean writeContentCMIS(String fullPath, InputStream content) {
        Map<String, String> params = new HashMap<String, String>();
        String cleanPath = fullPath.replaceAll("//", "/"); // sometimes sent bad paths
        if (cleanPath.endsWith("/")) {
            cleanPath = cleanPath.substring(0, cleanPath.length() - 1);
        }
        int splitIndex = cleanPath.lastIndexOf("/");
        String filename = cleanPath.substring(splitIndex + 1);
        MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
        String mimeType = mimeTypesMap.getContentType(filename);
        try {
            Session session = getCMISSession();
            ContentStream contentStream = session.getObjectFactory().createContentStream(filename, -1, mimeType, content);
            CmisObject cmisObject = null;
            if (contentExists(cleanPath)) {
                cmisObject = session.getObjectByPath(cleanPath);
            }
            if (cmisObject != null) {
                ObjectType type = cmisObject.getBaseType();
                if ("cmis:document".equals(type.getId())) {
                    org.apache.chemistry.opencmis.client.api.Document document = (org.apache.chemistry.opencmis.client.api.Document) cmisObject;
                    String pwcId = document.getVersionSeriesCheckedOutId();
                    if (pwcId != null) {
                        org.apache.chemistry.opencmis.client.api.Document pwcDocument = (org.apache.chemistry.opencmis.client.api.Document)session.getObject(pwcId);
                        pwcDocument.checkIn(false, null, contentStream, null);
                    } else {
                        document.setContentStream(contentStream, true);
                    }
                    session.removeObjectFromCache(document.getId());
                }
            } else {
                String folderPath = cleanPath.substring(0, splitIndex);
                CmisObject folderCmisObject = null;
                if (contentExists(folderPath)) {
                    folderCmisObject = session.getObjectByPath(folderPath);
                }
                Folder folder = null;
                if (folderCmisObject == null) {
                    // if not, create the folder first
                    int folderSplitIndex = folderPath.lastIndexOf("/");
                    String parentFolderPath = folderPath.substring(0, folderSplitIndex);
                    String folderName = folderPath.substring(folderSplitIndex + 1);
                    CmisObject parentFolderCmisObject = session.getObjectByPath(parentFolderPath);
                    ObjectType parentFolderType = parentFolderCmisObject.getType();
                    if ("cmis:folder".equals(parentFolderType.getId())) {
                        Folder parentFolder = (Folder)parentFolderCmisObject;
                        Map<String, String> newFolderProps = new HashMap<String, String>();
                        newFolderProps.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
                        newFolderProps.put(PropertyIds.NAME, folderName);
                        folder = parentFolder.createFolder(newFolderProps);
                    }
                } else {
                    folder = (Folder)folderCmisObject;
                }
                Map<String, Object> properties = new HashMap<String, Object>();
                properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
                properties.put(PropertyIds.NAME, filename);
                folder.createDocument(properties, contentStream, VersioningState.MINOR);
            }
            return true;
        } catch (CmisBaseException e) {
            logger.error("Error writing content to a path {0}", e, fullPath);
        } catch (NullPointerException e) {
            logger.error("Error writing content to a path {0}", e, fullPath);
        } catch (Throwable t) {
            logger.error("Error writing content to a path {0}", t, fullPath);
        }
        return false;
    }

    protected boolean deleteContentCMIS(String fullPath) {
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
                cmisObject.delete(true);
                session.removeObjectFromCache(cmisObject.getId());
                result = true;
            }
        } catch (CmisBaseException e) {
            logger.error("Could not find content for path {0}", fullPath);
        }
        return result;
    }

    protected VersionTO[] getContentVersionHistoryCMIS(String fullPath) {
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
                    org.apache.chemistry.opencmis.client.api.Document doc = (org.apache.chemistry.opencmis.client.api.Document)cmisObject;
                    List<org.apache.chemistry.opencmis.client.api.Document> versionsCMIS = doc.getAllVersions();
                    if (versionsCMIS != null && versionsCMIS.size() > 0) {
                        versions = new VersionTO[versionsCMIS.size()];
                    }
                    int idx = 0;
                    for (org.apache.chemistry.opencmis.client.api.Document version : versionsCMIS) {
                        VersionTO versionTO = new VersionTO();
                        versionTO.setVersionNumber(version.getVersionLabel());
                        versionTO.setLastModifier(version.getLastModifiedBy());
                        versionTO.setLastModifiedDate(version.getLastModificationDate().getTime());
                        versionTO.setComment(version.getDescription());

                        versions[idx++] = versionTO;
                    }
                }
            } else {
                logger.info("Content not found for path: [" + fullPath + "]");
            }
        } catch(CmisBaseException err) {
            logger.error("err getting content: ", err);
        }
        return versions;
    }

    protected boolean revertContentCMIS(String fullPath, String version, boolean major, String comment) {
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
                    org.apache.chemistry.opencmis.client.api.Document doc = (org.apache.chemistry.opencmis.client.api.Document)cmisObject;
                    List<org.apache.chemistry.opencmis.client.api.Document> versionsCMIS = doc.getAllVersions();
                    if (versionsCMIS != null && versionsCMIS.size() > 0) {
                        for (org.apache.chemistry.opencmis.client.api.Document documentVersion : versionsCMIS) {
                            if (version.equals(documentVersion.getVersionLabel())) {
                                ContentStream contentStream = documentVersion.getContentStream();
                                doc.setContentStream(contentStream, true);
                                Map<String, Object> props = new HashMap<String, Object>();
                                props.put("cmis:description", comment);
                                doc.updateProperties(props);
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
        return success;
    }

    protected String createFolderInternalCMIS(String fullPath, String name) {
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
                }
            } else {
                logger.error("Failed to create " + name + " folder since " + fullPath + " does not exist.");
            }
        } catch (CmisBaseException err) {
            logger.error("Failed to create " + name + " folder in {0}", err, fullPath);
        }
        return newFolderRef;
    }

    protected boolean copyContentInternalCMIS(String fromFullPath, String toFullPath, boolean isCut) {
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
                        org.apache.chemistry.opencmis.client.api.Document sourceDocument = (org.apache.chemistry.opencmis.client.api.Document)sourceCmisObject;
                        logger.debug("Coping document {0} to {1}", sourceDocument.getPaths().get(0), targetFolder.getPath());
                        copyDocument(targetFolder, sourceDocument);
                    } else if ("cmis:folder".equals(sourceType.getId())) {
                        Folder sourceFolder = (Folder)sourceCmisObject;
                        logger.debug("Coping folder {0} to {1}", sourceFolder.getPath(), targetFolder.getPath());
                        copyChildren(targetFolder, sourceFolder);
                    }
                    return true;
                } else {
                    logger.error((isCut ? "Move" : "Copy") + " failed since target path " + toFullPath + " is not folder.");
                }
            } else {
                if (sourceCmisObject == null) {
                    logger.error((isCut ? "Move" : "Copy") + " failed since source path " + fromFullPath + " does not exist.");
                }
                if (targetCmisObject == null) {
                    logger.error((isCut ? "Move" : "Copy") + " failed since target path " + toFullPath + " does not exist.");
                }
            }
        } catch (CmisBaseException err) {
            logger.error("Error while " + (isCut ? "moving" : "copying") + " content from " + fromFullPath + " to " + toFullPath, err);
        }

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
                copyDocument(parentFolder, (org.apache.chemistry.opencmis.client.api.Document) child);
            } else if (BaseTypeId.CMIS_FOLDER.value().equals(child.getBaseTypeId().value())) {
                copyFolder(parentFolder, (Folder) child);
            }
        }
    }

    private void copyDocument(Folder parentFolder, org.apache.chemistry.opencmis.client.api.Document sourceDocument) {
        Map<String, Object> documentProperties = new HashMap<String, Object>(2);
        documentProperties.put(PropertyIds.NAME, sourceDocument.getName());
        documentProperties.put(PropertyIds.OBJECT_TYPE_ID, sourceDocument.getBaseTypeId().value());
        //sourceDocument.copy(parentFolder);//.copy(parentFolder, documentProperties, null, null, null, null, null);
        parentFolder.createDocument(documentProperties, sourceDocument.getContentStream(), VersioningState.MINOR);
    }

    protected Session getCMISSession() {
        return getCMISSession(true);
    }

    protected Session getCMISSession(boolean alfrescoCMIS) {
        // Create a SessionFactory and set up the SessionParameter map
        SessionFactory sessionFactory = SessionFactoryImpl.newInstance();
        Map<String, String> parameter = new HashMap<String, String>();

        // user credentials - using the standard admin/admin
        String ticket = getAlfTicket();
        parameter.put(SessionParameter.USER, "ROLE_TICKET");
        parameter.put(SessionParameter.PASSWORD, ticket);

        // connection settings - we're connecting to a public cmis repo,
        // using the AtomPUB binding, but there are other options here,
        // or you can substitute your own URL
        parameter.put(SessionParameter.ATOMPUB_URL, alfrescoUrl + "/api/-default-/public/cmis/versions/1.1/atom/");
        //parameter.put(SessionParameter.ATOMPUB_URL, alfrescoUrl+"/cmisatom");
        parameter.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());

        // set the alfresco object factory

        if (alfrescoCMIS) {
            //parameter.put(SessionParameter.OBJECT_FACTORY_CLASS, "org.alfresco.cmis.client.impl.AlfrescoObjectFactoryImpl");
        }

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
        Session session = getCMISSession();
        String cleanPath = fullPath.replaceAll("//", "/"); // sometimes sent bad paths
        if (cleanPath.endsWith("/")) {
            cleanPath = cleanPath.substring(0, cleanPath.length() - 1);
        }
        try {
            CmisObject cmisObject = session.getObjectByPath(cleanPath);
            org.apache.chemistry.opencmis.client.api.Document document = (org.apache.chemistry.opencmis.client.api.Document) cmisObject;
            document.checkOut();
        } catch (CmisBaseException err) {
            logger.error("Error while locking content at path " + cleanPath, err);
        } catch (Throwable err) {
            logger.error("Error while locking content at path " + cleanPath, err);

        }
    }

    protected String expandRelativeSitePath(String site, String relativePath) {
        return "/wem-projects/" + site + "/" + site + "/work-area" + relativePath;
    }

    public void unLockItem(String site, String path) {
        String fullPath = expandRelativeSitePath(site, path);
        Session session = getCMISSession();
        String cleanPath = fullPath.replaceAll("//", "/"); // sometimes sent bad paths
        if (cleanPath.endsWith("/")) {
            cleanPath = cleanPath.substring(0, cleanPath.length() - 1);
        }
        try {
            CmisObject cmisObject = session.getObjectByPath(cleanPath);
            ObjectType type = cmisObject.getBaseType();
            if ("cmis:document".equals(type.getId())) {
                org.apache.chemistry.opencmis.client.api.Document document = (org.apache.chemistry.opencmis.client.api.Document) cmisObject;
                String pwcId = document.getVersionSeriesCheckedOutId();
                org.apache.chemistry.opencmis.client.api.Document pwcDocument = (org.apache.chemistry.opencmis.client.api.Document) session.getObject(pwcId);
                if (pwcDocument != null) {
                    pwcDocument.cancelCheckOut();
                }
            }
        } catch (CmisBaseException err) {
            logger.error("Error while locking content at path " + cleanPath, err);
        } catch (Throwable err) {
            logger.error("Error while locking content at path " + cleanPath, err);
        }
    }

    @Override
    public void addUserGroup(String groupName) {
        String newGroupRequestBody = "{ \"displayName\":\""+groupName+"\"}";

        try {
            InputStream bodyStream = IOUtils.toInputStream(newGroupRequestBody, "UTF-8");
            String result = alfrescoPostRequest("/api/rootgroups/" + groupName, null, bodyStream, "application/json");
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
            String result = alfrescoPostRequest("/api/groups/" + parentGroup + "/children/GROUP_" + groupName, null, bodyStream, "application/json");
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
            String result = alfrescoPostRequest("/api/groups/" + groupName + "/children/" + user, null, bodyStream, "application/json");
        }
        catch(Exception err) {
            logger.error("err adding user: " + user + " to parent group: " + groupName, err);
        }
    }

    @Override
    public String getCurrentToken() {
        return this.getSessionTicket();
    }


    protected String getSessionTicket() {
        String ticket = "UNSET";
        RequestContext context = RequestContext.getCurrent();

        if(context != null) {
            HttpSession httpSession = context.getRequest().getSession();
            ticket = (String)httpSession.getAttribute("alf_ticket");
        }
        else {
            CronJobContext cronJobContext = CronJobContext.getCurrent();
            if (cronJobContext != null) {
                ticket = cronJobContext.getAuthenticationToken();
            } else {
                RepositoryEventContext repositoryEventContext = RepositoryEventContext.getCurrent();
                if (repositoryEventContext != null) {
                    ticket = repositoryEventContext.getAuthenticationToken();
                }
            }
        }

        if(ticket==null) {
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

    @Override
    public Date getModifiedDate(String fullPath) {
        Map<String, String> params = new HashMap<String, String>();
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
        return modifiedDate;
    }

    @Override
    public boolean logout() {
        //make me do something
        String ticket = getSessionTicket();
        logger.debug("Invalidating ticket " + ticket);
        Map<String, String> params = new HashMap<>();
        params.put("ticket", ticket);
        String serviceURL = null;
        try {
            serviceURL = buildAlfrescoRequestURL("/api/login/ticket/{ticket}", params);
            DeleteMethod getMethod = new DeleteMethod(serviceURL);
            HttpClient httpClient = new HttpClient(new MultiThreadedHttpConnectionManager());
            int status = httpClient.executeMethod(getMethod);
            if (status == HttpStatus.SC_OK) {
                return true;
            }
        } catch (Exception e) {
            logger.error("Error while invalidating authentication token", e);
        }
        return false;
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
                String parentPath = child.getParent().replace(rootPath, "");
                if (StringUtils.isEmpty(parentPath)) {
                    parentPath = "/";
                }
                if (child.isDirectory()) {
                    createFolderInternalCMIS(parentPath, child.getName());
                } else if (child.isFile()) {
                    try {
                        writeContentCMIS(relativePath, FileUtils.openInputStream(child));
                    } catch (IOException e) {
                        logger.error("Error while bootstrapping file: " + relativePath, e);
                    }
                }
            }
        }
    }

    private boolean bootstrapCheck() {
        boolean contenSpace = contentExists("/wem-projects");
        boolean blueprintsSpace = contentExists("/cstudio/blueprints");
        boolean configSpace = contentExists("/cstudio/config");
        return contenSpace && blueprintsSpace && configSpace;
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

        String reponsePath = "";
        reponsePath = new File(fullPath).getPath();
        return reponsePath;
    }

    protected String alfrescoUrl;
    protected String adminUser;
    protected String adminPassword;
    protected boolean bootstrapEnabled = false;

    public String getAlfrescoUrl() { return alfrescoUrl; }
    public void setAlfrescoUrl(String url) { alfrescoUrl = url; }

    public String getAdminUser() { return adminUser; }
    public void setAdminUser(String adminUser) { this.adminUser = adminUser; }

    public String getAdminPassword() { return adminPassword; }
    public void setAdminPassword(String adminPassword) { this.adminPassword = adminPassword; }

    public boolean isBootstrapEnabled() { return bootstrapEnabled; }
    public void setBootstrapEnabled(boolean bootstrapEnabled) { this.bootstrapEnabled = bootstrapEnabled; }

}

