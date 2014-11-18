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
package org.craftercms.cstudio.alfresco.service.impl;

// Java imports
import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import org.craftercms.cstudio.alfresco.cache.cstudioCacheManager;
import org.craftercms.cstudio.alfresco.constant.CStudioContentModel;
import org.craftercms.cstudio.alfresco.dm.constant.DmConstants;
import org.craftercms.cstudio.alfresco.dm.to.DmContentItemTO;
import org.craftercms.cstudio.alfresco.dm.to.DmVersionDetailTO;
import org.craftercms.cstudio.alfresco.service.AbstractRegistrableService;
import org.craftercms.cstudio.alfresco.service.api.DmVersionService;
import org.craftercms.cstudio.alfresco.service.api.ObjectStateService;
import org.craftercms.cstudio.alfresco.service.api.PersistenceManagerService;
import org.craftercms.cstudio.alfresco.service.api.ServicesConfig;
import org.craftercms.cstudio.alfresco.service.exception.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.*;

// 3rd party imports
// Internal imports


public class DmVersionServiceImpl extends AbstractRegistrableService implements DmVersionService {

	private static final Logger logger = LoggerFactory.getLogger(DmVersionServiceImpl.class);
    private static final String REVERT_COMMENT = "Reverted to version %s";

    protected cstudioCacheManager _cache;
    public cstudioCacheManager getCacheManager() {
        return this._cache;
    }
    public void setCacheManager(cstudioCacheManager cache) {
        this._cache = cache;
    }

    protected BehaviourFilter policyBehaviourFilter;
    public BehaviourFilter getPolicyBehaviourFilter() {
        return policyBehaviourFilter;
    }
    public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
        this.policyBehaviourFilter = policyBehaviourFilter;
    }

    @Override
    public void register() {
        getServicesManager().registerService(DmVersionService.class, this);
    }

	/**
	 * 
	 * @param site
	 * @param path
	 * @param maxHistory -1 if unlimited
	 * @return
	 * @throws ServiceException
	 */
	public List<DmVersionDetailTO> getVersionHistory(String site, String path, int maxHistory, boolean showMinor) throws ServiceException {
        ServicesConfig servicesConfig = getService(ServicesConfig.class);
        String fullPath = servicesConfig.getRepositoryRootPath(site) + path;
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
		NodeRef orgNodeRef = persistenceManagerService.getNodeRef(fullPath);
        
		
		/**
		 * get Version history and populate domain object
		 */
		VersionHistory history = persistenceManagerService.getVersionHistory(orgNodeRef);
		List<DmVersionDetailTO> versions = new ArrayList();
		
		if(history != null)
		{
			Map<QName, Serializable> props = persistenceManagerService.getProperties(orgNodeRef);
	        String currentVersion = DefaultTypeConverter.INSTANCE.convert(String.class, props.get(ContentModel.PROP_VERSION_LABEL));
	        int idx = currentVersion.indexOf(".");
	        String currentMajorVerssion = currentVersion.substring(0, idx);
			Collection<Version> versionList = history.getAllVersions();
			Iterator versionListIt = versionList.iterator();
			DmVersionDetailTO versionDetail = new DmVersionDetailTO();
			while(versionListIt.hasNext())       {
		        Version version = (Version)versionListIt.next();
                String versionId = version.getVersionLabel();
                boolean condition = !versionId.startsWith(currentMajorVerssion) && (!showMinor && !versionId.endsWith(".0"));
                if (condition) continue;
                versionDetail = new DmVersionDetailTO();
				versionDetail.setLastModifier(version.getFrozenModifier());
				Date lastModifiedDate = version.getFrozenModifiedDate();
                versionDetail.setTimeZone(Calendar.getInstance().getTimeZone().getID());
				versionDetail.setLastModifiedDate(lastModifiedDate);
				versionDetail.setVersionNumber(versionId.toString());
                DmContentItemTO contentItem = persistenceManagerService.getContentItem(fullPath);
                versionDetail.setContentItem(contentItem);
                versionDetail.setComment(version.getDescription());
				versions.add(versionDetail);
			}
		}
		
		return versions;
	}

    public void restore(String site, String path, String versionLabel) throws ServiceException {
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);

        ServicesConfig servicesConfig = getService(ServicesConfig.class);
        String fullPath = servicesConfig.getRepositoryRootPath(site) + path;
        persistenceManagerService.setSystemProcessing(fullPath, true);

        NodeRef orgNodeRef = persistenceManagerService.getNodeRef(fullPath);
        Set<QName> aspects = persistenceManagerService.getAspects(orgNodeRef);
        FileInfo nodeInfo = persistenceManagerService.getFileInfo(orgNodeRef);
        if (nodeInfo.isFolder()) {
            List<FileInfo> children = persistenceManagerService.list(orgNodeRef);
            for (FileInfo child : children) {
                if (child.getName().equals(DmConstants.INDEX_FILE)) {
                    orgNodeRef = child.getNodeRef();
                    break;
                }
            }
        }
		
		/**
		 * Setting up version object
		 */
		VersionHistory history = persistenceManagerService.getVersionHistory(orgNodeRef);
		Version versionTo = null;
		if(history != null) {
			versionTo = history.getVersion(versionLabel);
		}
		persistenceManagerService.revert(orgNodeRef, versionTo);
        persistenceManagerService.setProperty(orgNodeRef, CStudioContentModel.PROP_STATUS, DmConstants.DM_STATUS_IN_PROGRESS);
		restoreAspects(orgNodeRef, aspects);

        createNextMinorVersion(site, path, String.format(REVERT_COMMENT, versionLabel));

        persistenceManagerService.transition(orgNodeRef, ObjectStateService.TransitionEvent.REVERT);
        persistenceManagerService.setSystemProcessing(fullPath, false);
	}

    @Override
    public void createNextMajorVersion(String site, String path) {
        createNewVersion(site, path, true, null);
    }

    @Override
    public void createNextMajorVersion(String site, String path, String comment) {
        createNewVersion(site, path, true, comment);
    }

    @Override
    public void createNextMajorVersion(String site, List<String> pathList) {
        for (String path : pathList) {
            createNewVersion(site, path, true, null);
        }
    }

    @Override
    public void createNextMajorVersion(String site, List<String> pathList, String comment) {
        for (String path : pathList) {
            createNewVersion(site, path, true, comment);
        }
    }

    @Override
    public void createNextMinorVersion(String site, String path) {
        createNewVersion(site, path, false, null);
    }

    @Override
    public void createNextMinorVersion(String site, String path, String comment) {
        createNewVersion(site, path, false, comment);
    }

    @Override
    public void createNextMinorVersion(String site, List<String> pathList) {
        for (String path : pathList) {
            createNewVersion(site, path, false, null);
        }
    }

    @Override
    public void createNextMinorVersion(String site, List<String> pathList, String comment) {
        for (String path : pathList) {
            createNewVersion(site, path, false, comment);
        }
    }
    
    protected Version createNewVersion(String site, String path, boolean majorVersion, String comment) {
        ServicesConfig servicesConfig = getService(ServicesConfig.class);
        String repoPath = servicesConfig.getRepositoryRootPath(site);
        String fullPath = path;
        if (!fullPath.startsWith(repoPath)) {
            fullPath = repoPath + path;
        }
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        NodeRef nodeRef = persistenceManagerService.getNodeRef(fullPath);
        if (nodeRef != null) {
            FileInfo nodeInf = persistenceManagerService.getFileInfo(nodeRef);
            if (nodeInf.isFolder()) {
                List<FileInfo> children = persistenceManagerService.list(nodeRef);
                for (FileInfo child : children) {
                    if (child.getName().equals(DmConstants.INDEX_FILE)) {
                        nodeRef = child.getNodeRef();
                        break;
                    }
                }
            }
            Map<String, Serializable> versionProps = new HashMap<String, Serializable>();
            if (majorVersion) {
                versionProps.put(VersionModel.PROP_VERSION_TYPE, VersionType.MAJOR);
            } else {
                versionProps.put(VersionModel.PROP_VERSION_TYPE, VersionType.MINOR);
            }
            if (StringUtils.isNotEmpty(comment)) {
                versionProps.put(VersionModel.PROP_DESCRIPTION, comment);
            }
            return persistenceManagerService.createVersion(nodeRef, versionProps);
        } else {
            return null;
        }
    }
    
    protected void restoreAspects(NodeRef node, Set<QName> aspects) {
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        for (QName aspect : aspects) {
            if (!persistenceManagerService.hasAspect(node, aspect)) {
                persistenceManagerService.addAspect(node, aspect, null);
            }
        }
    }

    @Override
    public void disableVersionable() {
        this.policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_VERSIONABLE);
    }

    @Override
    public void enableVersionable() {
        this.policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_VERSIONABLE);
    }
}
