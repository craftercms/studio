/*
 * Crafter Studio Web-content authoring solution
 * Copyright (C) 2007-2016 Crafter Software Corporation.
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

package org.craftercms.studio.impl.v1.service.content;

import org.apache.commons.lang.StringUtils;
import org.craftercms.studio.api.v1.constant.DmConstants;
import org.craftercms.studio.api.v1.dal.ItemState;
import org.craftercms.studio.api.v1.exception.ServiceException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.service.content.ImportService;
import org.craftercms.studio.api.v1.service.deployment.DmPublishService;
import org.craftercms.studio.api.v1.service.objectstate.ObjectStateService;
import org.craftercms.studio.api.v1.service.objectstate.TransitionEvent;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v1.service.workflow.context.MultiChannelPublishingContext;
import org.craftercms.studio.api.v1.to.ContentItemTO;
import org.craftercms.studio.api.v1.util.StudioConfiguration;
import org.craftercms.studio.impl.v1.util.ContentFormatUtils;
import org.craftercms.studio.impl.v1.util.ContentUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import static org.craftercms.studio.api.v1.util.StudioConfiguration.IMPORT_ASSET_CHAIN_NAME;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.IMPORT_ASSIGNEE;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.IMPORT_XML_CHAIN_NAME;

public class ImportServiceImpl implements ImportService {

    private final static Logger logger = LoggerFactory.getLogger(ImportServiceImpl.class);

    @Override
    public void importSite(String configLocation) {
        Document document = loadConfiguration(configLocation);
        if (document != null) {
            Element root = document.getRootElement();
            List<Node> siteNodes = root.selectNodes("site");
            if (siteNodes != null) {
                for (Node siteNode : siteNodes) {
                    String name = siteNode.valueOf("name");
                    String buildDataLocation = siteNode.valueOf("build-data-location");
                    String publishingChannelGroup = siteNode.valueOf("publish-channel-group");
                    String publishStr = siteNode.valueOf("publish");
                    boolean publish = (!StringUtils.isEmpty(publishStr) && publishStr.equalsIgnoreCase("true"));
                    String publishSize = siteNode.valueOf("publish-chunk-size");
                    int chunkSize = (!StringUtils.isEmpty(publishSize) && StringUtils.isNumeric(publishSize))
                            ? Integer.valueOf(publishSize) : -1;
                    Node foldersNode = siteNode.selectSingleNode("folders");
                    String sourceLocation = buildDataLocation + "/" + name;
                    String delayIntervalStr = siteNode.valueOf("delay-interval");
                    int delayInterval = (!StringUtils.isEmpty(delayIntervalStr) && StringUtils.isNumeric(delayIntervalStr))
                            ? Integer.valueOf(delayIntervalStr) : -1;
                    String delayLengthStr = siteNode.valueOf("delay-length");
                    int delayLength = (!StringUtils.isEmpty(delayLengthStr) && StringUtils.isNumeric(delayLengthStr))
                            ? Integer.valueOf(delayLengthStr) : -1;

                    importFromConfigNode(name, publishingChannelGroup, foldersNode, sourceLocation, "/", publish, chunkSize, delayInterval, delayLength);
                }
            }
        }
    }

    protected Document loadConfiguration(String configLocation) {
        logger.debug("[IMPORT] loading " + configLocation);
        InputStream in = null;
        try {
            in = new FileInputStream(configLocation);
            if (in != null) {
                return ContentUtils.convertStreamToXml(in);
            }
        } catch (FileNotFoundException e) {
            logger.error("[IMPORT] failed to load configuration.", e);

        } catch (DocumentException e) {
            logger.error("[IMPORT] failed to load configuration.", e);

        } finally {
            ContentUtils.release(in);
        }
        return null;
    }

    private void importFromConfigNode(final String site, String publishChannelGroup, final Node node,
                                      final String fileRoot, final String targetRoot,
                                      boolean publish, int chunkSize, int delayInterval, int delayLength) {
        if (!inProgress) {
            inProgress = true;
            if (delayInterval > 0) pauseEanbeld = true;
            this.currentDelayInterval = delayInterval * 1000;
            this.currentDelayLength = delayLength * 1000;
            final Set<String> importedPaths = new HashSet<String>();
            final List<String> importedFullPaths = new ArrayList<String>();
            logger.info("[IMPORT] started importing in " + site
                    + ", pause enabled: " + pauseEanbeld
                    + ", delay interval: " + this.currentDelayInterval
                    + ", delay length: " + this.currentDelayLength);

            boolean overWrite = ContentFormatUtils.getBooleanValue(node.valueOf("@over-write"));
            final List<Node> folderNodes = node.selectNodes("folder");
            if (publish) {
                String user = securityService.getCurrentUser();
                logger.debug("[IMPORT] publishing user: " + user);

                this.nextStop = System.currentTimeMillis() + this.currentDelayInterval;
                createFolders(site, importedPaths, importedFullPaths, folderNodes, fileRoot, targetRoot, "",
                        overWrite, user);
                logger.info("Starting Publish of Imported Files (Total " + importedFullPaths.size() + " On chunkSize of " + chunkSize + " )");
                publish(site, publishChannelGroup, targetRoot, importedFullPaths, chunkSize);
            } else {
                this.nextStop = System.currentTimeMillis() + this.currentDelayInterval;
                createFolders(site, importedPaths, importedFullPaths, folderNodes, fileRoot, targetRoot, "",
                        overWrite, null);
            }
            inProgress = false;
        } else {
            logger.info("[IMPORT] an import process is currently running.");
        }
    }


    /**
     * create folders
     *
     * @param site
     *            site name
     * @param importedPaths
     *            a list of imported files
     * @param importedFullPaths
     * @param nodes
     *            nodes representing folders
     * @param fileRoot
     *            the root location of files/folders being imported
     * @param targetRoot
     *            the target location root
     * @param parentPath
     *            the target location to import to
     * @param overWrite
     *            overwrite contents?
     * @param user
     *
     */
    @SuppressWarnings("unchecked")
    private void createFolders(String site, Set<String> importedPaths, List<String> importedFullPaths,
                               List<Node> nodes, String fileRoot, String targetRoot, String parentPath,
                               boolean overWrite, String user) {
        logger.info("[IMPORT] createFolders : site[" + site + "] " + "] fileRoot [" + fileRoot + "] targetRoot [ "
                + targetRoot + "] parentPath [" + parentPath + "] overwrite[" + overWrite + "]");

        if (nodes != null) {
            for (Node node : nodes) {
                String name = node.valueOf("@name");
                String value = node.valueOf("@over-write");
                boolean folderOverWrite = (StringUtils.isEmpty(value)) ? overWrite : ContentFormatUtils
                        .getBooleanValue(value);
                if (!StringUtils.isEmpty(name)) {
                    String currentFilePath = fileRoot + "/" + name;
                    String currentPath = parentPath + "/" + name;
                    // check if the parent node exists and create the folder if
                    // not
                    boolean folderExists = contentService.contentExists(site, currentPath);
                    if (!folderExists) {
                        contentService.createFolder(site, parentPath, name);
                    }
                    boolean importAll = ContentFormatUtils.getBooleanValue(node.valueOf("@import-all"));
                    if (importAll) {
                        importRootFileList(site, importedPaths, importedFullPaths, fileRoot + "/" + name,
                                targetRoot, currentPath, folderOverWrite, user);

                    } else {
                        // create child folders
                        List<Node> childFolders = node.selectNodes("folder");
                        createFolders(site, importedPaths, importedFullPaths, childFolders, currentFilePath,
                                targetRoot, currentPath, folderOverWrite, user);
                        // create child fiimportedPathsles
                        List<Node> childFiles = node.selectNodes("file");
                        createFiles(site, importedPaths, importedFullPaths, childFiles, currentFilePath,
                                targetRoot, currentPath, folderOverWrite, user);
                    }
                }
            }
        }
    }

    /**
     * import all files from the given file root
     *
     * @param site
     * @param importedPaths
     * @param importedFullPaths
     * @param fileRoot
     * @param targetRoot
     *            the target location root
     * @param parentPath
     *            the target location to import to
     * @param overWrite
     * @param user
     */
    protected void importRootFileList(String site, Set<String> importedPaths, List<String> importedFullPaths,
                                      String fileRoot, String targetRoot, String parentPath, boolean overWrite, String user) {
        URL resourceUrl = getResourceUrl(fileRoot);
        if (resourceUrl != null) {
            String resourcePath = resourceUrl.getFile();
            File file = new File(resourcePath);
            if (file.isDirectory()) {
                String[] children = file.list();
                if (children != null && children.length > 0) {
                    for (String childName : children) {
                        File childFile = new File(resourcePath + "/" + childName);
                        if (childFile.isDirectory()) {
                            String currentPath = parentPath + "/" + childName;
                            boolean folderExists = contentService.contentExists(site, currentPath);
                            if (!folderExists) {
                                contentService.createFolder(site, parentPath, childName);
                            }
                            logger.info("[IMPORT] Importing " + parentPath + "/" + childName);

                            importFileList(site, importedPaths, importedFullPaths, fileRoot + "/" + childName,
                                    targetRoot, parentPath + "/" + childName, overWrite, user);
                            logger.info("[IMPORT] Finished Importing " + parentPath + "/" + childName);
                        } else {
                            writeContentInTransaction(site, importedPaths, importedFullPaths, fileRoot,
                                    targetRoot, parentPath, childName, overWrite, user);
                        }
                    }
                }

            }

        } else {
            logger.error("[IMPORT] " + fileRoot + " is not found.");
        }
    }

    /**
     * get the resource url for import
     *
     * @param filePath
     * @return
     */
    private URL getResourceUrl(String filePath) {
        try {
            return new File(filePath).toURI().toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException("Not able to find " + filePath);
        }
    }

    /**
     * import files and folders at the given fileRoot path
     *
     * @param site
     * @param importedPaths
     * @param importedFullPaths
     * @param fileRoot
     * @param targetRoot
     *            the target location root
     * @param parentPath
     *            the target location to import to
     * @param overWrite
     * @param user
     */
    protected void importFileList(String site, Set<String> importedPaths, List<String> importedFullPaths,
                                  String fileRoot, String targetRoot, String parentPath, boolean overWrite, String user) {
        logger.info("[IMPORT] importFileList: fileRoot [" + fileRoot + "] name [" + targetRoot + "] overwrite["
                + overWrite + "]");
        URL resourceUrl = getResourceUrl(fileRoot);
        if (resourceUrl != null) {
            String resourcePath = resourceUrl.getFile();
            File file = new File(resourcePath);
            if (file.isDirectory()) {
                String[] children = file.list();
                if (children != null && children.length > 0) {
                    for (String childName : children) {
                        File childFile = new File(resourcePath + "/" + childName);
                        if (childFile.isDirectory()) {
                            String currentPath = parentPath + "/" + childName;
                            boolean folderExists = contentService.contentExists(site, currentPath);
                            if (!folderExists) {
                                contentService.createFolder(site, parentPath, childName);
                            }
                            importFileList(site, importedPaths, importedFullPaths, fileRoot + "/" + childName,
                                    targetRoot, parentPath + "/" + childName, overWrite, user);
                        } else {
                            writeContentInTransaction(site, importedPaths, importedFullPaths, fileRoot,
                                    targetRoot, parentPath, childName, overWrite, user);
                        }
                    }
                }
            }
        } else {
            logger.error("[IMPORT] " + fileRoot + " is not found.");
        }
    }

    /**
     * write content
     *
     * @param site
     * @param importedPaths
     * @param importedFullPaths
     * @param fileRoot
     * @param parentPath
     * @param name
     * @param overWrite
     * @param user
     */
    protected void writeContentInTransaction(final String site, final Set<String> importedPaths,
                                             final List<String> importedFullPaths, final String fileRoot,
                                             final String targetRoot, final String parentPath, final String name, final boolean overWrite, final String user) {
        long startTimeWrite = System.currentTimeMillis();
        logger.debug("[IMPORT] writing file in transaction: " + parentPath + "/" + name);
        writeContent(site, importedPaths, importedFullPaths, fileRoot, targetRoot, parentPath, name,
                        overWrite);
        logger.debug("[IMPORT] done writing file in transaction: " + parentPath + "/" + name
                        + ", time: " + (System.currentTimeMillis() - startTimeWrite));
        pause();
    }

    /**
     * write content
     *
     * @param site
     * @param importedPaths
     * @param importedFullPaths
     * @param fileRoot
     * @param parentPath
     * @param name
     * @param overWrite
     */
    protected void writeContent(String site, Set<String> importedPaths, List<String> importedFullPaths,
                                String fileRoot, String targetRoot, String parentPath, String name, boolean overWrite) {
        boolean isXml = true;
        String processChain = getXmlChainName();
        if (!name.endsWith(".xml")) {
            isXml = false;
            processChain = getAssetChainName();
        }
        InputStream in = null;
        String filePath = parentPath + "/" + name;
        String fileSystemPath = fileRoot + "/" + name;
        logger.info("[IMPORT] writeContent: fileRoot [" + fileRoot + "] fullPath [" + filePath + "] overwrite["
                + overWrite + "] process chain [ " + processChain + "]");
        long startTimeWrite = System.currentTimeMillis();
        logger.debug("[IMPORT] writing file: " + parentPath + "/" + name);

        try {
            File file = new File(fileSystemPath);
            if (file.exists()) {
                in = new FileInputStream(file);
                String currentPath = parentPath + "/" + name;
                boolean contentExists = contentService.contentExists(site, currentPath);
                // create parameters
                Map<String, String> params = createParams(site, isXml, targetRoot, parentPath, name);
                String id = site + ":" + filePath + ":" + name;
                // write content only it is new or overwrite is set to true for
                // existing
                if (!contentExists || overWrite) {
                    String fullPath = targetRoot + filePath;
                    objectStateService.setSystemProcessing(site, currentPath, true);
                    // write the content
                    contentService.processContent(id, in, isXml, params, processChain);
                    ContentItemTO item = contentService.getContentItem(site, currentPath);
                    // update state
                    if (item != null) {
                        objectStateService.transition(site, item, TransitionEvent.SAVE);
                        objectStateService.setSystemProcessing(site, currentPath, false);
                    } else {
                        ItemState state = objectStateService.getObjectState(site, currentPath);
                        if (state == null) {
                            objectStateService.insertNewEntry(site, currentPath);
                        }
                    }

                    importedPaths.add(filePath);
                    importedFullPaths.add(fullPath);
                } else {
                    logger.debug("[IMPORT] " + filePath
                                + " exists and set to not to overrwite. skipping this file.");

                }
            }
        } catch (FileNotFoundException e) {
            logger.warn("[IMPORT] " + filePath + " does not exist.");

        } catch (ServiceException e) {
            logger.error("[IMPORT] failed to import " + filePath, e);
        } finally {
            ContentUtils.release(in);
        }
        logger.debug("[IMPORT] done writing file: " + parentPath + "/" + name
                + ", time: " + (System.currentTimeMillis() - startTimeWrite));
    }

    /**
     * create write process parameters
     *
     * @param site
     * @param isXml
     * @param targetRoot
     * @param parentPath
     * @param name
     * @return
     */
    private Map<String, String> createParams(String site, boolean isXml, String targetRoot, String parentPath, String name) {
        Map<String, String> params = new HashMap<String, String>();
        String filePath = parentPath + "/" + name;
        String path = (isXml) ? filePath : parentPath;
        String fullPath = targetRoot + filePath;
        params.put(DmConstants.KEY_SITE, site);
        params.put(DmConstants.KEY_PATH, path);
        params.put(DmConstants.KEY_FULL_PATH, fullPath);
        params.put(DmConstants.KEY_FILE_NAME, name);
        params.put(DmConstants.KEY_USER, getAssignee());
        params.put(DmConstants.KEY_CREATE_FOLDERS, "true");
        params.put(DmConstants.KEY_UNLOCK, "true");
        logger.debug("[IMPORT] creating/updating " + filePath);
        return params;
    }

    /**
     * pause the process if it reached the interval
     */
    protected void pause() {
        if (this.pauseEanbeld) {
            if (System.currentTimeMillis() >= this.nextStop) {
                logger.debug("[IMPORT] pausing import process.");
                try {
                    Thread.sleep(this.currentDelayLength);
                    this.nextStop = System.currentTimeMillis() + this.currentDelayInterval;
                } catch (InterruptedException e) {
                    logger.error("[IMPORT] error while pausing import process.", e);
                }
                logger.debug("[IMPORT] done pausing import process.");
            }
        }
    }

    /**
     * create files from a list
     *
     * @param site
     * @param importedPaths
     * @param importedFullPaths
     * @param nodes
     * @param fileRoot
     * @param targetRoot
     *            the target location root
     * @param parentPath
     *            the target location to import to
     * @param overWrite
     * @param user
     */
    protected void createFiles(String site, Set<String> importedPaths, List<String> importedFullPaths,
                               List<Node> nodes, String fileRoot, String targetRoot, String parentPath,
                               boolean overWrite, String user) {
        logger.info("[IMPORT] createFiles: fileRoot [" + fileRoot + "] parentFullPath [" + parentPath
                    + "] overwrite[" + overWrite + "]");
        if (nodes != null) {
            for (Node node : nodes) {
                String name = node.valueOf("@name");
                String value = node.valueOf("@over-write");
                boolean fileOverwrite = (StringUtils.isEmpty(value)) ? overWrite : ContentFormatUtils
                        .getBooleanValue(value);
                if (!StringUtils.isEmpty(name)) {
                    writeContentInTransaction(site, importedPaths, importedFullPaths, fileRoot, targetRoot,
                            parentPath, name, fileOverwrite, user);
                }
            }
        }
    }

    /**
     * publish items
     *
     * @param site
     * @param publishChannelGroup
     * @param targetRoot
     * @param fullPaths
     * @param chunkSize
     */
    protected void publish(String site, String publishChannelGroup, String targetRoot, List<String> fullPaths, int chunkSize) {
        if (chunkSize < 1) {
            logger.info("[IMPORT] publising chunk size not defined. publishing all together.");
            submitToGoLive(site, publishChannelGroup, fullPaths);
        } else {
            int total = fullPaths.size();
            int count = 0;
            // group pages in a small chucks
            Set<String> goLiveItemPaths = new HashSet<String>(chunkSize);
            List<String> goLiveItemFullPaths = new ArrayList<String>(chunkSize);
            for (String importedFullPath : fullPaths) {
                logger.debug("		" + importedFullPath);
                if (goLiveItemFullPaths.size() < chunkSize) {
                    goLiveItemFullPaths.add(importedFullPath);
                    String goLiveItemPath = importedFullPath.replaceFirst(targetRoot, "");
                    goLiveItemPaths.add(goLiveItemPath);
                    count++;
                }
                if (goLiveItemPaths.size() == chunkSize) {
                    logger.info("[IMPORT] submitting " + chunkSize + " imported files to " + publishChannelGroup
                            + " (" + count + "/" + total + ")");

                    submitToGoLive(site, publishChannelGroup, goLiveItemFullPaths);
                    goLiveItemPaths = new HashSet<String>(chunkSize);
                    goLiveItemFullPaths = new ArrayList<String>(chunkSize);
                }
            }
            // submit the last set
            if (goLiveItemPaths.size() < chunkSize) {
                logger.info("[IMPORT] submitting " + chunkSize + " imported files to " + publishChannelGroup + " ("
                            + count + "/" + total + ")");
                submitToGoLive(site, publishChannelGroup, goLiveItemFullPaths);
                goLiveItemPaths = new HashSet<String>(chunkSize);
                goLiveItemFullPaths = new ArrayList<String>(chunkSize);
            }
        }
    }

    /**
     * submit imported items to go live
     *
     * @param site
     * @param publishChannelGroup
     * @param importedFullPaths
     */
    protected void submitToGoLive(String site, String publishChannelGroup, List<String> importedFullPaths) {
        MultiChannelPublishingContext mcpContext = new MultiChannelPublishingContext(
                publishChannelGroup, "", "Import Service");
        dmPublishService.publish(site, importedFullPaths, null, mcpContext);
        logger.info("All files have been submitted to be publish");
    }

    public SiteService getSiteService() { return siteService; }
    public void setSiteService(SiteService siteService) { this.siteService = siteService; }

    public SecurityService getSecurityService() { return securityService; }
    public void setSecurityService(SecurityService securityService) { this.securityService = securityService; }

    public ContentRepository getContentRepository() { return contentRepository; }
    public void setContentRepository(ContentRepository contentRepository) { this.contentRepository = contentRepository; }

    public ContentService getContentService() { return contentService; }
    public void setContentService(ContentService contentService) { this.contentService = contentService; }

    public ObjectStateService getObjectStateService() { return objectStateService; }
    public void setObjectStateService(ObjectStateService objectStateService) { this.objectStateService = objectStateService; }

    public DmPublishService getDmPublishService() { return dmPublishService; }
    public void setDmPublishService(DmPublishService dmPublishService) { this.dmPublishService = dmPublishService; }

    public StudioConfiguration getStudioConfiguration() { return studioConfiguration; }
    public void setStudioConfiguration(StudioConfiguration studioConfiguration) { this.studioConfiguration = studioConfiguration; }

    public String getAssignee() {
        return studioConfiguration.getProperty(IMPORT_ASSIGNEE);
    }

    public String getXmlChainName() {
        return studioConfiguration.getProperty(IMPORT_XML_CHAIN_NAME);
    }

    public String getAssetChainName() {
        return studioConfiguration.getProperty(IMPORT_ASSET_CHAIN_NAME);
    }

    protected SiteService siteService;
    protected SecurityService securityService;
    protected ContentRepository contentRepository;
    protected ContentService contentService;
    protected ObjectStateService objectStateService;
    protected DmPublishService dmPublishService;
    protected StudioConfiguration studioConfiguration;

    /**
     * is import in progress?
     */
    private boolean inProgress = false;

    /** going to pause import process? **/
    private boolean pauseEanbeld = false;
    /** next pause time **/
    private long nextStop;
    /** import delay interval **/
    private long currentDelayInterval;
    /** import delay time **/
    private long currentDelayLength;

    /**
     * publishing channel
     * @author hyanghee
     *
     */
    public class PublishingChannel {

        private String id;
        private String name;
        private String url;
        private String password;
        private String target;
        private boolean publishMetadata;

        public PublishingChannel() {}

        public PublishingChannel(String id, String name, String url, String password, String target, boolean publishMetadata) {
            this.id = id;
            this.name = name;
            this.url = url;
            this.password = password;
            this.target = target;
            this.publishMetadata = publishMetadata;
        }

        /**
         * @return the url
         */
        public String getUrl() {
            return url;
        }
        /**
         * @param url the url to set
         */
        public void setUrl(String url) {
            this.url = url;
        }
        /**
         * @return the password
         */
        public String getPassword() {
            return password;
        }
        /**
         * @param password the password to set
         */
        public void setPassword(String password) {
            this.password = password;
        }
        /**
         * @return the target
         */
        public String getTarget() {
            return target;
        }
        /**
         * @param target the target to set
         */
        public void setTarget(String target) {
            this.target = target;
        }
        /**
         * @return the publishMetadata
         */
        public boolean isPublishMetadata() {
            return publishMetadata;
        }
        /**
         * @param publishMetadata the publishMetadata to set
         */
        public void setPublishMetadata(boolean publishMetadata) {
            this.publishMetadata = publishMetadata;
        }

        /**
         * @return the id
         */
        public String getId() {
            return id;
        }

        /**
         * @param id the id to set
         */
        public void setId(String id) {
            this.id = id;
        }

        /**
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * @param name the name to set
         */
        public void setName(String name) {
            this.name = name;
        }

    }
}
