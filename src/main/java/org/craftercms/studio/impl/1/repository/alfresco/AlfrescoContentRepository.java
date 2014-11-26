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
package org.craftercms.cstudio.impl.repository.alfresco;

import org.apache.commons.httpclient.methods.*;
import org.apache.commons.httpclient.methods.multipart.*;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import javax.transaction.*;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.String;
import java.lang.reflect.Method;
import java.util.*;
import java.net.*;
import java.net.URI;
import java.util.HashMap;
import javax.servlet.http.*;
import javax.servlet.http.Cookie;

import org.apache.commons.httpclient.*;

import net.sf.json.*;

import org.apache.commons.io.IOUtils;

import org.craftercms.cstudio.api.log.Logger;
import org.craftercms.cstudio.api.log.LoggerFactory;
import org.craftercms.cstudio.api.repository.RepositoryItem;
import org.craftercms.cstudio.impl.repository.AbstractContentRepository;
import org.craftercms.cstudio.api.to.VersionTO;

import org.craftercms.commons.http.RequestContext;

/**
 * Alfresco repository implementation.  This is the only point of contact with Alfresco's API in
 * the entire system under the org.craftercms.cstudio.impl package structure
 * @author russdanner
 *
 */
// this class is abstract because I wont implement the dirty interface
// this class contains all that we do in alfresco and nothing more
public abstract class AlfrescoContentRepository extends AbstractContentRepository {

    private static final Logger logger = LoggerFactory.getLogger(AlfrescoContentRepository.class);

    @Override
    public InputStream getContent(String path) {
        InputStream retStream = null;
        String name = path.substring(path.lastIndexOf("/")+1);

        try {
            String nodeRef = getNodeRefForPath(path);

            if(nodeRef != null) {
                // construct and execute url to download result
                String downloadURI = "/api/node/content/workspace/SpacesStore/{nodeRef}/{name}?a=true";
                Map<String, String> lookupContentParams = new HashMap<String, String>();
                lookupContentParams.put("nodeRef", nodeRef.replace("workspace://SpacesStore/", ""));
                lookupContentParams.put("name", name);

                retStream = this.alfrescoGetRequest(downloadURI, lookupContentParams);
            }
            else {
                throw new Exception("nodeRef not found for path: [" + path + "]");
            }
        }
        catch(Exception err) {
            logger.error("err getting content: ", err);   
        }

        return retStream;
    }

    @Override
    public boolean contentExists(String path) {
       return (this.getNodeRefForPath(path) != null); 
    }


    @Override
    public boolean writeContent(String path, InputStream content) {
        logger.debug("writing content to " + path);
        String uploadURI = "/api/upload";
        String contentType = "cm:content";
        int splitIndex = path.lastIndexOf("/");
        String name = path.substring(splitIndex + 1);
        // find the existing node by path
        String nodeRef = getNodeRefForPath(path);
        // find the target folder node by its path
        String folderPath = path.substring(0, splitIndex);
        String folderRef = getNodeRefForPath(folderPath);
        if (folderRef == null) {
            // if not, create the folder first
            int folderSplitIndex = folderPath.lastIndexOf("/");
            String parentFolderPath = folderPath.substring(0, folderSplitIndex);
            String folderName = folderPath.substring(folderSplitIndex + 1);
            folderRef = this.createFolderInternal(parentFolderPath, folderName);
        }
        // TODO: might still need to check if the folderRef still exists

        // add parameters
        Map<String, String> params = new HashMap<String, String>();
        params.put("filename", name);
        // if it's a new content, check if the folder exists
        params.put("destination", folderRef);
        if (nodeRef != null) {
            params.put("updateNodeRef", nodeRef);
        }
        //params.put("uploaddirectory", folderRef);
        // TODO: add description for version update - do we need this?
        params.put("contenttype", contentType);
        params.put("majorversion", "false");
        params.put("overwrite", "true");
        // read back nodeRef
        logger.debug("request params\n" + params);

        try {
            String response = this.alfrescoMultipartPostRequest(uploadURI, params, content, "application/xml", "UTF-8");
            logger.debug("done writing content to " + path);
            logger.debug("response back from the server: " + response);
            return true;
        } catch (Exception e) {
            logger.error("error writing to " + path, e);
            return false;
        } finally {
            IOUtils.closeQuietly(content);
        }
    }

    @Override
    public boolean createFolder(String path, String name) {
        String folderRef = this.createFolderInternal(path, name);
        return folderRef != null;
    }

    @Override
    public boolean deleteContent(String path) {
        logger.debug("deleting content at " + path);
        boolean result = false;
        String deleteURI = "/slingshot/doclib/action/file/node/";
        String nodeRef = getNodeRefForPath(path);

        if (nodeRef == null) {
            // if no nodeRef, it's already deleted so just return true
            logger.debug("No content found at " + path);
            result = true;
        } else {
            Map<String, String> params = new HashMap<String, String>();
            try {
                deleteURI = deleteURI + nodeRef.replace("://", "/");
                result = this.alfrescoDeleteRequest(deleteURI, params);
            } catch (Exception e) {
                logger.error("Error while deleting " + path, e);
            }
        }
        return result;
    }

    @Override
    public boolean moveContent(String fromPath, String toPath) {
        return false;
        //POST
         //"/api/path/{store_type}/{store_id}/{nodepath}/children?sourceFolderId={sourceFolderId}&versioningState={versioningState?}""
    };

    @Override
    public boolean copyContent(String fromPath, String toPath, boolean deep) {
        return false;
        //POST /alfresco/service/slingshot/doclib/action/copy-to/node/{store_type}/{store_id}
    }

    /**
     * get immediate children for path
     * @param path path to content
     */
    public RepositoryItem[] getContentChildren(String path) {

        RepositoryItem[] items = new RepositoryItem[0];

        String cleanPath = path.replaceAll("//", "/"); // sometimes sent bad paths
               cleanPath = cleanPath.replace("/index.xml", "");
               cleanPath = (cleanPath.endsWith("/")) ? cleanPath.substring(0, cleanPath.length()-1) : cleanPath;


        Map<String, String> params = new HashMap<String, String>();
        String namespacedPath = cleanPath.replaceAll("/", "/cm:");
        String query = "PATH:\"/app:company_home" + namespacedPath + "/*" +  "\"";

        String lookupNodeRefURI = "/slingshot/node/search?q={q}&lang={lang}&store={store}&maxResults={maxResults}";
        params.put("q", query);
        params.put("lang","fts-alfresco");
        params.put("store", "workspace://SpacesStore");
        params.put("maxResults", "10000");

        try{
            InputStream responseStream = this.alfrescoGetRequest(lookupNodeRefURI, params);
            String jsonResponse = IOUtils.toString(responseStream, "utf-8");

            JsonConfig cfg = new JsonConfig();
            JSONObject root = JSONObject.fromObject(jsonResponse, cfg);
            int resultCount = root.getInt("numResults");
            JSONArray results = root.getJSONArray("results");

            items = new RepositoryItem[resultCount];
            for(int i=0; i<resultCount; i++) {
                JSONObject result = results.getJSONObject(i);
                JSONObject resultName = result.getJSONObject("name");
                RepositoryItem item = new RepositoryItem();

                item.path = path;
                item.name = resultName.getString("prefixedName").replace("cm:", "");
                item.isFolder = (item.name.contains(".")==false); // weak sauce

                items[i] = item;
            }

        }
        catch(Exception err) {
            logger.error("error getting children for path (" + path + "): ", err);   
        }

        return items;
    }

    /** 
     * get the version history for an item
     * @param path - the path of the item
     */
    public VersionTO[] getContentVersionHistory(String path) {
        VersionTO[] versions = new VersionTO[0];

        try {
            String nodeRef = getNodeRefForPath(path);

            if(nodeRef != null) {
                // construct and execute url to download result
                String historyURI = "/api/version?nodeRef={nodeRef}";
                Map<String, String> params = new HashMap<String, String>();
                params.put("nodeRef", nodeRef);

                InputStream response = this.alfrescoGetRequest(historyURI, params);

                try{
                    String jsonResponse = IOUtils.toString(response, "utf-8");
                    JsonConfig cfg = new JsonConfig();
                    JSONArray results = JSONArray.fromObject(jsonResponse, cfg);
                    int resultCount = results.size();

                    if(resultCount > 0) {
                        versions = new VersionTO[resultCount];
                        for(int i=0; i<resultCount; i++) {
                            JSONObject result = results.getJSONObject(i);
                            JSONObject creator = result.getJSONObject("creator");
                            VersionTO version = new VersionTO();

                            version.setVersionNumber(result.getString("label"));
                            version.setLastModifier(creator.getString("userName"));
                            version.setLastModifiedDate(new Date()); //result.getString("createdDate"));
                            //       "createdDate": "16 Nov 2014 21:28:09 GMT-0500 (EST)",
                            //       "createdDateISO": "2014-11-16T21:28:09.141-05:00",
                            version.setComment(result.getString("description"));

                            versions[i] = version;
                        }
                    }
                }
                catch(Exception err) {
                    logger.error("err getting noderef for path (" + path + "): ", err);   
                }
            }
            else {
                throw new Exception("nodeRef not found for path: [" + path + "]");
            }
        }
        catch(Exception err) {
            logger.error("err getting content: ", err);   
        }

        return versions;
    }

    /** 
     * revert a version (create a new version based on an old version)
     * @param path - the path of the item to "revert"
     * @param version - old version ID to base to version on
     */
    public boolean revertContent(String path, String version, boolean major, String comment) {
        boolean success = false;
        String nodeRef = getNodeRefForPath(path);
        
        if(nodeRef != null) {
            String revertCommand = "{" +
               "\"nodeRef\":\"" + nodeRef + "\"," + 
               "\"version\":\"" + version + "\"," +
               "\"majorVersion\":\"" + major + "\"," +
               "\"description\":\"" + comment+ "\"" +
               "}";

            try {
                InputStream bodyStream = IOUtils.toInputStream(revertCommand, "UTF-8");
                String result = alfrescoPostRequest("/api/revert", null, bodyStream, "application/json");
                
                success = result.contains("true");
             }
            catch(Exception err) {
                logger.error("err reverting content content: ", err);   
            }
        }

        return success;
    }

    /**
     * given a path, get an alfresco node ref
     */
    protected String getNodeRefForPath(String path) { 
        String nodeRef = null;
        Map<String, String> params = new HashMap<String, String>();
        String cleanPath = path.replaceAll("//", "/"); // sometimes sent bad paths
        String namespacedPath = cleanPath.replaceAll("/", "/cm:");
        String query = "PATH:\"/app:company_home" + namespacedPath + "\"";

        String lookupNodeRefURI = "/slingshot/node/search?q={q}&lang={lang}&store={store}&maxResults={maxResults}";
        params.put("q", query);
        params.put("lang","fts-alfresco");
        params.put("store", "workspace://SpacesStore");
        params.put("maxResults", "100");

        try{
            InputStream responseStream = this.alfrescoGetRequest(lookupNodeRefURI, params);
            String jsonResponse = IOUtils.toString(responseStream, "utf-8");
            JsonConfig cfg = new JsonConfig();
            JSONObject root = JSONObject.fromObject(jsonResponse, cfg);
            int resultCount = root.getInt("numResults");

            if(resultCount == 1) {
                JSONObject result = root.getJSONArray("results").getJSONObject(0);
                nodeRef = result.getString("nodeRef");
            }
            else if(resultCount == 0) {
                throw new Exception("no results for query (" + query + ")");
            }
            else {
                throw new Exception("too many results (" + resultCount + ") for query (" + query + ")");
            }

        }
        catch(Exception err) {
            logger.error("err getting noderef for path (" + path + "): ", err);   
        }

        return nodeRef;
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
        String newFolderRef = null;
        String createFolderURI = "/api/type/cm%3afolder/formprocessor";
        Map<String, String> params = new HashMap<String, String>();
        String folderRef = getNodeRefForPath(path);
        if (StringUtils.isEmpty(folderRef)) {
            logger.error("Failed to create " + name + " folder since " + path + " does not exist.");
        } else {
            JSONObject requestObj = new JSONObject();
            requestObj.put("alf_destination", folderRef);
            requestObj.put("prop_cm_name", name);
            requestObj.put("prop_cm_title", name);
            InputStream is = IOUtils.toInputStream(requestObj.toString());
            try {
                String responseBody = this.alfrescoPostRequest(createFolderURI, params, is, "application/json");
                JsonConfig cfg = new JsonConfig();
                JSONObject respObj = JSONObject.fromObject(responseBody, cfg);
                newFolderRef = (String) respObj.get("persistedObject");
            } catch (Exception e) {
                logger.error("Error while creating " + name + " at " + path, e);
            } finally {
                IOUtils.closeQuietly(is);
            }
        }
        return newFolderRef;
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
     * fire DELETE request to Alfresco with proper security
     */
    protected boolean alfrescoDeleteRequest(String uri, Map<String, String> params) throws Exception {
        String serviceURL = buildAlfrescoRequestURL(uri, params);
        DeleteMethod deleteMethod = new DeleteMethod(serviceURL);
        HttpClient httpClient = new HttpClient(new MultiThreadedHttpConnectionManager());
        int status = httpClient.executeMethod(deleteMethod);
        if (status == 200 || status == 204) {
            return true;
        } else {
            // TODO: we might need to return response stream
            logger.error("Delete failed with " + status);
            return false;
        }
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
        String serviceUrlBase = "http://127.0.0.1:8080/alfresco/service";
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
    protected String getAlfTicket() {
        String ticket = "UNSET";
        RequestContext context = RequestContext.getCurrent();
        HttpServletRequest request = context.getRequest();

        if(request != null) {
            ticket = request.getParameter("alf_ticket");

            if(ticket == null) {
                // check the cookies
                Cookie[] cookies = request.getCookies();
                for(int i=0; i<cookies.length; i++) {
                    Cookie cookie = cookies[i];
                    if(cookie.getName().equals("alf_ticket")) {
                        ticket = cookie.getValue();
                        break;
                    }
                    else if(cookie.getName().equals("ccticket")) {
                        ticket = cookie.getValue();
                        break;
                    }
                }
            }
        }
        else {
            // maybe inside a chron trigger on other not request context
        }

        return ticket;
    }
}