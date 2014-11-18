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
package org.craftercms.cstudio.alfresco.dm.service.impl;

import javolution.util.FastMap;
import org.alfresco.model.ContentModel;
import org.alfresco.model.WCMWorkflowModel;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import org.craftercms.cstudio.alfresco.cache.Scope;
import org.craftercms.cstudio.alfresco.cache.ThreadSafeCacheManager;
import org.craftercms.cstudio.alfresco.constant.CStudioContentModel;
import org.craftercms.cstudio.alfresco.dm.constant.DmConstants;
import org.craftercms.cstudio.alfresco.dm.service.api.DmContentService;
import org.craftercms.cstudio.alfresco.dm.service.api.DmStateManager;
import org.craftercms.cstudio.alfresco.dm.to.DmContentItemTO;
import org.craftercms.cstudio.alfresco.dm.util.DmUtils;
import org.craftercms.cstudio.alfresco.service.AbstractRegistrableService;
import org.craftercms.cstudio.alfresco.service.api.CStudioNodeService;
import org.craftercms.cstudio.alfresco.service.api.PersistenceManagerService;
import org.craftercms.cstudio.alfresco.service.api.ProfileService;
import org.craftercms.cstudio.alfresco.service.exception.ServiceException;
import org.craftercms.cstudio.alfresco.to.UserProfileTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * DmStateManager is a class to set and retrieve properties specific to workflow
 */
public class DmStateManagerImpl implements DmStateManager{

}
