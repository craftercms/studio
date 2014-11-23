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

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import javax.transaction.*;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.*;
import java.net.*;
import java.net.URI;
import javax.servlet.http.*;
import javax.servlet.http.Cookie;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;

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

    /**
     * get document from wcm content
     *
     * @param path
     * @return document
     * @throws ServiceException
     */
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
            System.out.println("err getting content: " + err);   
        }

        return retStream;
    }

    /**
     * @return true if site has content object at path
     */
    public boolean contentExists(String path) {
       return (this.getNodeRefForPath(path) != null); 
    }


    public boolean writeContent(String path, InputStream content) {
        return false;
    }

    /**
     * delete content
     * @param path path to content
     */
    public boolean deleteContent(String path) {
        return false;
    }

    /**
     * get immediate children for path
     * @param path path to content
     */
    public RepositoryItem[] getContentChildren(String path) {
        return null;
    }

    /** 
     * get the version history for an item
     * @param site - the project ID
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
                    System.out.println("err getting noderef for path (" + path + "): "+err);   
                }
            }
            else {
                throw new Exception("nodeRef not found for path: [" + path + "]");
            }
        }
        catch(Exception err) {
            System.out.println("err getting content: " + err);   
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
                System.out.println("err reverting content content: " + err);   
            }
        }

        return success;
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

    protected InputStream alfrescoGetRequest(String uri, Map<String, String> params) throws Exception {
        InputStream retResponse = null;

        URI serviceURI = new URI(buildAlfrescoRequestURL(uri, params));        

        retResponse = serviceURI.toURL().openStream();
     
        return retResponse;
    }

    protected String alfrescoPostRequest(String uri, Map<String, String> params, InputStream body, String bodyMimeType) throws Exception {
        String serviceURL = buildAlfrescoRequestURL(uri, params);        
        PostMethod postMethod = new PostMethod(serviceURL);
        postMethod.setRequestEntity(new InputStreamRequestEntity(body, bodyMimeType));
        //postMethod.setRequestHeader("Cookie", "alf_ticket="+getAlfTicket());

        HttpClient httpClient = new HttpClient(new MultiThreadedHttpConnectionManager());
        int status = httpClient.executeMethod(postMethod);
        
        return postMethod.getResponseBodyAsString();
    }


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
            System.out.println("err getting noderef for path (" + path + "): "+err);   
        }

        return nodeRef;
    }
}