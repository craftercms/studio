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
package org.craftercms.cstudio.alfresco.dm.util.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import javolution.util.FastList;
import javolution.util.FastMap;
import javolution.util.FastSet;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.craftercms.cstudio.alfresco.deployment.DeploymentEndpointConfigTO;
import org.craftercms.cstudio.alfresco.dm.constant.DmConstants;
import org.craftercms.cstudio.alfresco.dm.service.api.DmContentService;
import org.craftercms.cstudio.alfresco.dm.service.api.DmPublishService;
import org.craftercms.cstudio.alfresco.dm.service.api.DmTransactionService;
import org.craftercms.cstudio.alfresco.dm.service.api.DmWorkflowService;
import org.craftercms.cstudio.alfresco.dm.util.api.DmImportService;
import org.craftercms.cstudio.alfresco.dm.workflow.MultiChannelPublishingContext;
import org.craftercms.cstudio.alfresco.dm.workflow.WorkflowProcessor;
import org.craftercms.cstudio.alfresco.service.AbstractRegistrableService;
import org.craftercms.cstudio.alfresco.service.api.ObjectStateService;
import org.craftercms.cstudio.alfresco.service.api.PersistenceManagerService;
import org.craftercms.cstudio.alfresco.service.api.SiteService;
import org.craftercms.cstudio.alfresco.service.exception.ServiceException;
import org.craftercms.cstudio.alfresco.to.PublishingChannelConfigTO;
import org.craftercms.cstudio.alfresco.to.PublishingChannelGroupConfigTO;
import org.craftercms.cstudio.alfresco.util.ContentFormatUtils;
import org.craftercms.cstudio.alfresco.util.ContentUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DmImportServiceImpl extends AbstractRegistrableService implements DmImportService {

	private static final Logger LOGGER = LoggerFactory.getLogger(DmImportServiceImpl.class);
    
	protected PersistenceManagerService _persistenceManagerService = null;

	/**
	 * Authentication Service
	 */
	protected AuthenticationService _authenticationService = null;
	
	/**
	 * NodeService
	 */
	protected NodeService _nodeService = null;
	
	/**
	 * DmContentService
	 */
	protected DmContentService _dmContentService = null;

	/**
	 * DmWorkflowService
	 */
	protected DmWorkflowService _dmWorkflowService = null;

	/**
	 * DmTransactionService
	 */
	protected DmTransactionService _dmTransactionService = null;

	/**
	 * site service
	 */
	protected SiteService _siteService; 

	/**
	 * workflow name to use for submission
	 */
	protected String _workflowName;

	/**
	 * workflow assignee
	 */
	protected String _assignee;

	/**
	 * xml chain name
	 */
	protected String _xmlChainName;

	/**
	 * asset chain name
	 */
	protected String _assetChainName;

	/**
	 * workflow processor
	 */
	protected WorkflowProcessor _workflowProcessor;
	
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
	
	@Override
	public void register() {
		getServicesManager().registerService(DmImportService.class, this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.craftercms.cstudio.alfresco.dm.util.api.DmImportService#
	 * importFromConfigNode(java.lang.String, java.lang.String, org.dom4j.Node,
	 * java.lang.String, java.lang.String,
	 * org.alfresco.service.cmr.repository.NodeRef, boolean, int)
	 */
	@SuppressWarnings("unchecked")
	public void importFromConfigNode(final String site, String publishChannelGroup, final Node node,
			final String fileRoot, final String targetRoot, final NodeRef targetRef, 
			boolean publish, int chunkSize, int delayInterval, int delayLength) {
		if (!inProgress) {
			inProgress =  true;
			if (delayInterval > 0) pauseEanbeld = true;
			this.currentDelayInterval = delayInterval * 1000;
			this.currentDelayLength = delayLength * 1000;
			final Set<String> importedPaths = new FastSet<String>();
			final List<String> importedFullPaths = new FastList<String>();
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("[IMPORT] started importing in " + site 
						+ ", pause enabled: " + pauseEanbeld 
						+ ", delay interval: "  + this.currentDelayInterval 
						+ ", delay length: " + this.currentDelayLength);
			}
			boolean overWrite = ContentFormatUtils.getBooleanValue(node.valueOf("@over-write"));
			final List<Node> folderNodes = node.selectNodes("folder");
			if (publish) {
		        SiteService siteService = getService(SiteService.class);
	            PublishingChannelGroupConfigTO configTO = siteService.getPublishingChannelGroupConfigs(site).get(publishChannelGroup);
				String user = _authenticationService.getCurrentUserName();
				List<PublishingChannel> channels = getChannels(site, configTO);
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("[IMPORT] publishing user: " + user + ", publishing channel config: " + configTO.getName());
				}
				this.nextStop = System.currentTimeMillis() + this.currentDelayInterval;
				createFolders(site, importedPaths, importedFullPaths, folderNodes, fileRoot, targetRef, targetRoot, "",
					overWrite, channels, user);
				LOGGER.info("Starting Publish of Imported Files (Total "+importedFullPaths.size()+" On chunkSize of "+chunkSize+" )");
				publish(site, publishChannelGroup, targetRoot, importedFullPaths, chunkSize);
			} else {
				this.nextStop = System.currentTimeMillis() + this.currentDelayInterval;
				createFolders(site, importedPaths, importedFullPaths, folderNodes, fileRoot, targetRef, targetRoot, "",
					overWrite, null, null);
			}
			inProgress = false;
		} else {
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("[IMPORT] an import process is currently running.");
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.craftercms.cstudio.alfresco.dm.util.api.DmImportService#
	 * publishImprotedContents(java.lang.String, java.lang.String,
	 * java.lang.String, org.alfresco.service.cmr.repository.NodeRef,
	 * java.lang.String, int)
	 */
	@Override
	public void publishImprotedContents(String site, String publishChannelGroup, String targetRoot, NodeRef targetRef,
			String startPath, int chunkSize) {
		final Set<String> importedPaths = new FastSet<String>();
		final List<String> importedFullPaths = new FastList<String>();
		populateFiles(targetRoot, targetRef, startPath, importedPaths, importedFullPaths);
		this.publish(site, publishChannelGroup, targetRoot, importedFullPaths, chunkSize);
	}

	/**
	 * publish items
	 * 
	 * @param site
	 * @param publishChannelGroup
	 * @param targetRoot
	 * @param paths
	 * @param fullPaths
	 * @param chunkSize
	 */
	protected void publish(String site, String publishChannelGroup, String targetRoot, List<String> fullPaths, int chunkSize) {
		if (chunkSize < 1) {
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("[IMPORT] publising chunk size not defined. publishing all together.");
				submitToGoLive(site, publishChannelGroup, fullPaths);
			}
		} else {
			int total = fullPaths.size();
			int count = 0;
			// group pages in a small chucks
			Set<String> goLiveItemPaths = new FastSet<String>(chunkSize);
			List<String> goLiveItemFullPaths = new FastList<String>(chunkSize);
			for (String importedFullPath : fullPaths) {
				LOGGER.debug("		" + importedFullPath);
				if (goLiveItemFullPaths.size() < chunkSize) {
					goLiveItemFullPaths.add(importedFullPath);
					String goLiveItemPath = importedFullPath.replaceFirst(targetRoot, "");
					goLiveItemPaths.add(goLiveItemPath);
					count++;
				}
				if (goLiveItemPaths.size() == chunkSize) {
					if (LOGGER.isInfoEnabled()) {
						LOGGER.info("[IMPORT] submitting " + chunkSize + " imported files to " + publishChannelGroup
								+ " (" + count + "/" + total + ")");
					}
					submitToGoLive(site, publishChannelGroup, goLiveItemFullPaths);
					goLiveItemPaths = new FastSet<String>(chunkSize);
					goLiveItemFullPaths = new FastList<String>(chunkSize);
				}
			}
			// submit the last set
			if (goLiveItemPaths.size() < chunkSize) {
				if (LOGGER.isInfoEnabled()) {
					LOGGER.info("[IMPORT] submitting " + chunkSize + " imported files to " + publishChannelGroup + " ("
							+ count + "/" + total + ")");
				}
				submitToGoLive(site, publishChannelGroup, goLiveItemFullPaths);
				goLiveItemPaths = new FastSet<String>(chunkSize);
				goLiveItemFullPaths = new FastList<String>(chunkSize);
			}
		}
	}

	/**
	 * populate all child paths
	 * 
	 * @param targetRoot
	 * @param parentRef
	 * @param parentPath
	 */
	private void populateFiles(String targetRoot, NodeRef parentRef, String parentPath, Set<String> importedPaths,
			List<String> importedFullPaths) {
		List<FileInfo> items = java.util.Collections.unmodifiableList(this.getPersistenceManager().list(parentRef));
		if (items != null) {
			for (FileInfo item : items) {
 				String name = item.getName();
                String currentPath;
                if (parentPath.equals("/")) {
                    currentPath = parentPath + name;
                } else {
                    currentPath = parentPath + "/" + name;
                }
				String currentFullPath = targetRoot + currentPath;
				if (item.isFolder()) {
					populateFiles(targetRoot, item.getNodeRef(), currentPath, importedPaths, importedFullPaths);
				} else {
					importedPaths.add(currentPath);
					importedFullPaths.add(currentFullPath);
				}
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
		DmTransactionService transaction = getService(DmTransactionService.class);
		UserTransaction tnx = transaction.getNonPropagatingUserTransaction();
		try {
			tnx.begin();
			MultiChannelPublishingContext mcpContext = new MultiChannelPublishingContext(
					publishChannelGroup, "", "Import Service");
			DmPublishService publishService = getService(DmPublishService.class);
			publishService.publish(site, importedFullPaths, null, mcpContext);
			LOGGER.info("All files have been submitted to be publish");
			tnx.commit();
		} catch (Exception ex) {
			LOGGER.error("Unable to publish files due a error ",ex);
			try {
				tnx.rollback();
			} catch (IllegalStateException e) {
				LOGGER.error("Unable to rollback Transaction");
			} catch (SecurityException e) {
				LOGGER.error("Unable to rollback Transaction");
			} catch (SystemException e) {
				LOGGER.error("Unable to rollback Transaction");
			}
		}
	}

	/**
	 * create folders
	 * 
	 * @param name
	 *            site name
	 * @param importedFiles
	 *            a list of imported files
	 * @param importedFullPaths
	 * @param nodes
	 *            nodes representing folders
	 * @param fileRoot
	 *            the root location of files/folders being imported
	 * @param parentRef
	 *            the parent nodeRef
	 * @param targetRoot
	 *            the target location root
	 * @param parentPath
	 *            the target location to import to
	 * @param overWrite
	 *            overwrite contents?
	 * @param channels
	 * @param user
	 * 
	 */
	@SuppressWarnings("unchecked")
	private void createFolders(String site, Set<String> importedPaths, List<String> importedFullPaths,
			List<Node> nodes, String fileRoot, NodeRef parentRef, String targetRoot, String parentPath,
			boolean overWrite, List<PublishingChannel> channels, String user) {
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("[IMPORT] createFolders : site[" + site + "] " + "] fileRoot [" + fileRoot + "] targetRoot [ "
					+ targetRoot + "] parentPath [" + parentPath + "] overwrite[" + overWrite + "]");
		}
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
					NodeRef currentRef = findChildByName(parentRef, name);
					if (currentRef == null) {
						currentRef = createDirectory(parentRef, name);
					}
					boolean importAll = ContentFormatUtils.getBooleanValue(node.valueOf("@import-all"));
					if (importAll) {
						importRootFileList(site, importedPaths, importedFullPaths, fileRoot + "/" + name, currentRef,
								targetRoot, currentPath, folderOverWrite, channels, user);

					} else {
						// create child folders
						List<Node> childFolders = node.selectNodes("folder");
						createFolders(site, importedPaths, importedFullPaths, childFolders, currentFilePath,
								currentRef, targetRoot, currentPath, folderOverWrite, channels, user);
						// create child fiimportedPathsles
						List<Node> childFiles = node.selectNodes("file");
						createFiles(site, importedPaths, importedFullPaths, childFiles, currentFilePath, currentRef,
								targetRoot, currentPath, folderOverWrite, channels, user);
					}
				}
			}
		}
	}

	/**
	 * import all files from the given file root
	 * 
	 * @param site
	 * @param importedFiles
	 * @param fileRoot
	 * @param parentRef
	 * @param targetRoot
	 *            the target location root
	 * @param parentPath
	 *            the target location to import to
	 * @param overWrite
	 * @param channels
	 * @param user
	 */
	protected void importRootFileList(String site, Set<String> importedPaths, List<String> importedFullPaths,
			String fileRoot, NodeRef parentRef, String targetRoot, String parentPath, boolean overWrite, 
			List<PublishingChannel> channels, String user) {
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
							NodeRef currentRef = findChildByName(parentRef, childName);
							if (currentRef == null) {
								currentRef = createDirectory(parentRef, childName);
							}
							if (LOGGER.isInfoEnabled()) {
								LOGGER.info("[IMPORT] Importing " + parentPath + "/" + childName);
							}
							importFileList(site, importedPaths, importedFullPaths, fileRoot + "/" + childName,
									currentRef, targetRoot, parentPath + "/" + childName, overWrite, channels, user);
							if (LOGGER.isInfoEnabled()) {
								LOGGER.info("[IMPORT] Finished Importing " + parentPath + "/" + childName);
							}
						} else {
							writeContentInTransaction(site, importedPaths, importedFullPaths, fileRoot, parentRef,
									targetRoot, parentPath, childName, overWrite, channels, user);
						}
					}
				}

			}

		} else {
			LOGGER.error("[IMPORT] " + fileRoot + " is not found.");
		}
	}

	/**
	 * import files and folders at the given fileRoot path
	 * 
	 * @param site
	 * @param importedFiles
	 * @param fileRoot
	 * @param parentRef
	 * @param targetRoot
	 *            the target location root
	 * @param parentPath
	 *            the target location to import to
	 * @param overWrite
	 * @param channels
	 * @param user
	 */
	protected void importFileList(String site, Set<String> importedPaths, List<String> importedFullPaths,
			String fileRoot, NodeRef parentRef, String targetRoot, String parentPath, boolean overWrite, 
			List<PublishingChannel> channels, String user) {
		if (LOGGER.isInfoEnabled())
			LOGGER.info("[IMPORT] importFileList: fileRoot [" + fileRoot + "] name [" + targetRoot + "] overwrite["
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
							NodeRef currentRef = findChildByName(parentRef, childName);
							if (currentRef == null) {
								currentRef = createDirectory(parentRef, childName);
							}
							importFileList(site, importedPaths, importedFullPaths, fileRoot + "/" + childName,
									currentRef, targetRoot, parentPath + "/" + childName, overWrite, channels, user);
						} else {
							writeContentInTransaction(site, importedPaths, importedFullPaths, fileRoot, parentRef,
									targetRoot, parentPath, childName, overWrite, channels, user);
						}
					}
				}
			}
		} else {
			LOGGER.error("[IMPORT] " + fileRoot + " is not found.");
		}
	}

	/**
	 * create files from a list
	 * 
	 * @param site
	 * @param importedFiles
	 * @param nodes
	 * @param fileRoot
	 * @param parentRef
	 * @param targetRoot
	 *            the target location root
	 * @param parentPath
	 *            the target location to import to
	 * @param overWrite
	 * @param channels
	 * @param user
	 */
	protected void createFiles(String site, Set<String> importedPaths, List<String> importedFullPaths,
			List<Node> nodes, String fileRoot, NodeRef parentRef, String targetRoot, String parentPath,
			boolean overWrite, List<PublishingChannel> channels, String user) {
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("[IMPORT] createFiles: fileRoot [" + fileRoot + "] parentFullPath [" + parentPath
					+ "] overwrite[" + overWrite + "]");
		}
		if (nodes != null) {
			for (Node node : nodes) {
				String name = node.valueOf("@name");
				String value = node.valueOf("@over-write");
				boolean fileOverwrite = (StringUtils.isEmpty(value)) ? overWrite : ContentFormatUtils
						.getBooleanValue(value);
				if (!StringUtils.isEmpty(name)) {
					writeContentInTransaction(site, importedPaths, importedFullPaths, fileRoot, parentRef, targetRoot,
							parentPath, name, fileOverwrite, channels, user);
				}
			}
		}
	}

	/**
	 * write content
	 * 
	 * @param site
	 * @param importedFiles
	 * @param fileRoot
	 * @param parentRef
	 * @param parentFullPath
	 * @param name
	 * @param overWrite
	 * @param channels
	 * @param user
	 */
	protected void writeContentInTransaction(final String site, final Set<String> importedPaths,
			final List<String> importedFullPaths, final String fileRoot, final NodeRef parentRef,
			final String targetRoot, final String parentPath, final String name, final boolean overWrite,
			final List<PublishingChannel> channels, final String user) {
		long startTimeWrite = System.currentTimeMillis();
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[IMPORT] writing file in transaction: " + parentPath + "/" + name);
		}
		RetryingTransactionHelper helper = this.getDmTransactionService().getRetryingTransactionHelper();
		helper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<String>() {
			@Override
			public String execute() throws Throwable {
				writeContent(site, importedPaths, importedFullPaths, fileRoot, parentRef, targetRoot, parentPath, name,
						overWrite);
				return null;
			}
		}, false, true);
		if (LOGGER.isDebugEnabled()) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("[IMPORT] done writing file in transaction: " + parentPath + "/" + name 
						+ ", time: " + (System.currentTimeMillis() - startTimeWrite));
			}
		}
		pause();
	}

	/**
	 * pause the process if it reached the interval
	 */
	protected void pause() {
		if (this.pauseEanbeld) {
			if (System.currentTimeMillis() >= this.nextStop) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("[IMPORT] pausing import process.");
				}
				try {
					Thread.sleep(this.currentDelayLength);
					this.nextStop = System.currentTimeMillis() + this.currentDelayInterval;
				} catch (InterruptedException e) {
					if (LOGGER.isErrorEnabled()) {
						LOGGER.error("[IMPORT] error while pausing import process.", e);
					}
				}
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("[IMPORT] done pausing import process.");
				}
			}
		}
	}

	/**
	 * write content
	 * 
	 * @param site
	 * @param importedFiles
	 * @param fileRoot
	 * @param parentRef
	 * @param parentFullPath
	 * @param name
	 * @param overWrite
	 */
	protected void writeContent(String site, Set<String> importedPaths, List<String> importedFullPaths,
			String fileRoot, NodeRef parentRef, String targetRoot, String parentPath, String name, boolean overWrite) {
		boolean isXml = true;
		String processChain = this._xmlChainName;
		if (!name.endsWith(".xml")) {
			isXml = false;
			processChain = this._assetChainName;
		}
		InputStream in = null;
		String filePath = parentPath + "/" + name;
		String fileSystemPath = fileRoot + "/" + name;
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("[IMPORT] writeContent: fileRoot [" + fileRoot + "] fullPath [" + filePath + "] overwrite["
					+ overWrite + "] process chain [ " + processChain + "]");

		}
		long startTimeWrite = System.currentTimeMillis();
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[IMPORT] writing file: " + parentPath + "/" + name);
		}
		try {
			File file = new File(fileSystemPath);
			if (file.exists()) {
				in = new FileInputStream(file);
				NodeRef currentRef = findChildByName(parentRef, name);
				// create parameters
				Map<String, String> params = createParams(site, isXml, targetRoot, parentPath, name, currentRef);
				String id = site + ":" + filePath + ":" + name;
				// write content only it is new or overwrite is set to true for
				// existing
				if (currentRef == null || overWrite) {
					String fullPath = targetRoot + filePath;
					NodeRef nodeRef = this.getPersistenceManager().getNodeRef(fullPath);
					if (nodeRef != null) {
						this.getPersistenceManager().setSystemProcessing(fullPath, true);
					}
					// write the content
					this.getDmContentService().processContent(id, in, isXml, params, processChain);
					// update state
					if (nodeRef != null) {
						if (isXml) {
							this.getPersistenceManager().transition(fullPath, ObjectStateService.TransitionEvent.SAVE);
						} else {
							this.getPersistenceManager().transition(fullPath, ObjectStateService.TransitionEvent.SAVE);
						}
                        this.getPersistenceManager().setSystemProcessing(fullPath, false);
					} else {
                        ObjectStateService.State state = this.getPersistenceManager().getObjectState(fullPath);
                        if (state == null) {
						    this.getPersistenceManager().insertNewObjectEntry(fullPath);
                        } else {
                            this.getPersistenceManager().transition(fullPath, ObjectStateService.TransitionEvent.SAVE);
                        }
					}

					importedPaths.add(filePath);
					importedFullPaths.add(fullPath);
				} else {
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("[IMPORT] " + filePath
								+ " exists and set to not to overrwite. skipping this file.");
					}
				}
			}
		} catch (FileNotFoundException e) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("[IMPORT] " + filePath + " does not exist.");
			}
		} catch (ServiceException e) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("[IMPORT] failed to import " + filePath, e);
			}
		} finally {
			ContentUtils.release(in);
		}
		if (LOGGER.isDebugEnabled()) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("[IMPORT] done writing file: " + parentPath + "/" + name 
						+ ", time: " + (System.currentTimeMillis() - startTimeWrite));
			}
		}
	}

	/**
	 * create write process parameters
	 * 
	 * @param site
	 * @param isXml
	 * @param targetRoot
	 * @param parentPath
	 * @param name
	 * @param currentRef
	 * @return
	 */
	private Map<String, String> createParams(String site, boolean isXml, String targetRoot, String parentPath, String name,
			NodeRef currentRef) {
		Map<String, String> params = new FastMap<String, String>();
		String filePath = parentPath + "/" + name;
		String path = (isXml) ? filePath : parentPath;
		String fullPath = targetRoot + filePath;
		params.put(DmConstants.KEY_SITE, site);
		params.put(DmConstants.KEY_PATH, path);
		params.put(DmConstants.KEY_FULL_PATH, fullPath);
		params.put(DmConstants.KEY_FILE_NAME, name);
		params.put(DmConstants.KEY_USER, this._assignee);
		params.put(DmConstants.KEY_CREATE_FOLDERS, "true");
		params.put(DmConstants.KEY_UNLOCK, "true");
		if (currentRef != null) {
			params.put(DmConstants.KEY_EDIT, "true");
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("[IMPORT] updating " + filePath);
			}
		} else {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("[IMPORT] creating " + filePath);
			}
		}
		return params;
	}

	/**
	 * find a child contained under the parent
	 * 
	 * @param parentRef
	 * @param name
	 * @return childRef
	 */
	private NodeRef findChildByName(NodeRef parentRef, String name) {
		return getPersistenceManager().getChildByName(parentRef, ContentModel.ASSOC_CONTAINS, name);
	}

	/**
	 * create a directory
	 * 
	 * @param parentRef
	 * @param name
	 * @return
	 */
	private NodeRef createDirectory(final NodeRef parentRef, final String name) {
		RetryingTransactionHelper helper = this.getDmTransactionService().getRetryingTransactionHelper();
		NodeRef nodeRef = helper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {
				Map<QName, Serializable> nodeProperties = new FastMap<QName, Serializable>();
				nodeProperties.put(ContentModel.PROP_NAME, name);
				NodeRef node = getPersistenceManager().createNewFolder(parentRef, name, nodeProperties);
                getPersistenceManager().insertNewObjectEntry(node);
                return node;
			}
		}, false, true);
		return nodeRef;
	}

	
	/**
	 * populate channel information
	 * 
	 * @param configTO
	 */
	private List<PublishingChannel> getChannels(String site, PublishingChannelGroupConfigTO configTO) {
        SiteService siteService = getSiteService();
		if (configTO.getChannels() != null) {
			List<PublishingChannel> channels = new FastList<PublishingChannel>();
			for (PublishingChannelConfigTO channelConfig : configTO.getChannels()) {
                DeploymentEndpointConfigTO endpointConfigTO = siteService.getDeploymentEndpoint(site, channelConfig.getName());
				if (endpointConfigTO != null) {
					PublishingChannel channel = new PublishingChannel();
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("[IMPORT] populating channel: " + channelConfig.getName() + ", id: " + channel.getId());
					}
					channel.setName(channelConfig.getName());
	                String server = endpointConfigTO.getServerUrl();
	                channel.setPassword(endpointConfigTO.getPassword());
	                channel.setTarget(endpointConfigTO.getTarget());
	                channel.setPublishMetadata(endpointConfigTO.isSendMetadata());
					try {
						URL channelUrl = new URL(server);
						channel.setUrl(channelUrl.toString());
					} catch (MalformedURLException e) {
						if (LOGGER.isErrorEnabled()) {
							LOGGER.error("[IMPORT] " + channelConfig.getName() + " has an invalid target URL.", e);
						}
					}
					channels.add(channel);
				}
			}
			return channels;
		}  else {
			return null;
		}
	}

	/**
	 * get PersistenceManagerService
	 * 
	 * @return
	 */
	private PersistenceManagerService getPersistenceManager() {
		if (this._persistenceManagerService == null) {
			this._persistenceManagerService = getServicesManager().getService(PersistenceManagerService.class);
		}
		return this._persistenceManagerService;
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
	 * load import configuration
	 * 
	 * @param configLocation
	 *            configuration file location in class path
	 */
	protected Document loadConfiguration(String configLocation) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[IMPORT] loading " + configLocation);
		}
		InputStream in = null;
		try {
			in = new FileInputStream(configLocation);
			if (in != null) {
				return ContentUtils.convertStreamToXml(in);
			}
		} catch (FileNotFoundException e) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("[IMPORT] failed to load configuration.", e);
			}
		} catch (DocumentException e) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("[IMPORT] failed to load configuration.", e);
			}
		} finally {
			ContentUtils.release(in);
		}
		return null;
	}

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
	
	/**
	 * @return the workflowName
	 */
	public String getWorkflowName() {
		return _workflowName;
	}

	/**
	 * @param workflowName
	 *            the workflowName to set
	 */
	public void setWorkflowName(String workflowName) {
		this._workflowName = workflowName;
	}

	/**
	 * @return the dmContentService
	 */
	public DmContentService getDmContentService() {
		if (this._dmContentService == null) {
			this._dmContentService = getServicesManager().getService(DmContentService.class);
		}
		return _dmContentService;
	}

	/**
	 * @param dmContentService
	 *            the dmContentService to set
	 */
	public void setDmContentService(DmContentService dmContentService) {
		this._dmContentService = dmContentService;
	}

	/**
	 * @return the dmWorkflowService
	 */
	public DmWorkflowService getDmWorkflowService() {
		if (this._dmWorkflowService == null) {
			this._dmWorkflowService = getServicesManager().getService(DmWorkflowService.class);
		}
		return _dmWorkflowService;
	}

	/**
	 * @param dmWorkflowService
	 *            the dmWorkflowService to set
	 */
	public void setDmWorkflowService(DmWorkflowService dmWorkflowService) {
		this._dmWorkflowService = dmWorkflowService;
	}

	/**
	 * @return the dmTransactionService
	 */
	public DmTransactionService getDmTransactionService() {
		if (this._dmTransactionService == null) {
			this._dmTransactionService = getServicesManager().getService(DmTransactionService.class);
		}
		return _dmTransactionService;
	}

	/**
	 * @param dmTransactionService
	 *            the dmTransactionService to set
	 */
	public void setDmTransactionService(DmTransactionService dmTransactionService) {
		this._dmTransactionService = dmTransactionService;
	}

	/**
	 * @return the assignee
	 */
	public String getAssignee() {
		return _assignee;
	}

	/**
	 * @param assignee
	 *            the assignee to set
	 */
	public void setAssignee(String assignee) {
		this._assignee = assignee;
	}

	/**
	 * @return the xmlChainName
	 */
	public String getXmlChainName() {
		return _xmlChainName;
	}

	/**
	 * @param xmlChainName
	 *            the xmlChainName to set
	 */
	public void setXmlChainName(String xmlChainName) {
		this._xmlChainName = xmlChainName;
	}

	/**
	 * @return the assetChainName
	 */
	public String getAssetChainName() {
		return _assetChainName;
	}

	/**
	 * @param assetChainName
	 *            the assetChainName to set
	 */
	public void setAssetChainName(String assetChainName) {
		this._assetChainName = assetChainName;
	}

	/**
	 * 
	 * @return
	 */
	public WorkflowProcessor getWorkflowProcessor() {
		return _workflowProcessor;
	}

	/**
	 * 
	 * @param workflowProcessor
	 */
	public void setWorkflowProcessor(WorkflowProcessor workflowProcessor) {
		this._workflowProcessor = workflowProcessor;
	}

	/**
	 * @return the authenticationService
	 */
	public AuthenticationService getAuthenticationService() {
		return _authenticationService;
	}

	/**
	 * @param authenticationService the authenticationService to set
	 */
	public void setAuthenticationService(AuthenticationService authenticationService) {
		this._authenticationService = authenticationService;
	}

	/**
	 * @return the nodeService
	 */
	public NodeService getNodeService() {
		return _nodeService;
	}

	/**
	 * @param nodeService the nodeService to set
	 */
	public void setNodeService(NodeService nodeService) {
		this._nodeService = nodeService;
	}


	/**
	 * get site service
	 * 
	 * @return
	 */
	public SiteService getSiteService() {
		if (this._siteService == null) {
			this._siteService = this.getServicesManager().getService(SiteService.class);
		}
		return this._siteService;
	}

	/**
	 * @return the inProgress
	 */
	public boolean isInProgress() {
		return inProgress;
	}

}
