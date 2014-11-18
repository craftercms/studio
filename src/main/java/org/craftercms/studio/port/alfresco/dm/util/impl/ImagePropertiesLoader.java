/*
 * Copyright (C) 2007-2013 Crafter Software Corporation.
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
package org.craftercms.cstudio.alfresco.dm.util.impl;

import java.io.Serializable;
import java.util.List;

import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.craftercms.cstudio.alfresco.constant.CStudioContentModel;
import org.craftercms.cstudio.alfresco.dm.to.DmContentItemTO;
import org.craftercms.cstudio.alfresco.dm.util.DmUtils;
import org.craftercms.cstudio.alfresco.dm.util.api.ContentPropertyLoader;
import org.craftercms.cstudio.alfresco.service.ServicesManager;
import org.craftercms.cstudio.alfresco.service.api.PersistenceManagerService;
import org.craftercms.cstudio.alfresco.service.api.SearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImagePropertiesLoader implements ContentPropertyLoader {

	private static final Logger logger = LoggerFactory
			.getLogger(ImagePropertiesLoader.class);

	public static final String NAME = "ImagePropertiesLoader";

    protected ServicesManager servicesManager;
    public ServicesManager getServicesManager() {
        return servicesManager;
    }
    public void setServicesManager(ServicesManager servicesManager) {
        this.servicesManager = servicesManager;
    }

    @Override
	public String getName() {
		return NAME;
	}

	@Override
	public boolean isEligible(NodeRef node, DmContentItemTO item) {
		// populate image properties if the file is an image
        PersistenceManagerService persistenceManagerService = getServicesManager().getService(PersistenceManagerService.class);
		String mimetype = persistenceManagerService
				.guessMimetype(persistenceManagerService.getFileInfo(node).getName());
		if (mimetype != null && mimetype.startsWith("image/")) {
			return true;
		}
		return false;
	}

	@Override
	public void loadProperties(NodeRef node, DmContentItemTO item) {
		String path = getNodePath(node);
		int width = -1, height = -1;
        PersistenceManagerService persistenceManagerService = getServicesManager().getService(PersistenceManagerService.class);
		if (persistenceManagerService.hasAspect(node,
				CStudioContentModel.ASPECT_IMAGE_METADATA)) {
			Serializable widthProperty = persistenceManagerService.getProperty(node,
					CStudioContentModel.PROP_IMAGE_WIDTH);
			width = (widthProperty != null) ? (Integer) widthProperty : -1;
			Serializable heightProperty = persistenceManagerService.getProperty(node,
					CStudioContentModel.PROP_IMAGE_HEIGHT);
			height = (heightProperty != null) ? (Integer) heightProperty : -1;
		}
		item.setWidth(width);
		item.setHeight(height);
	}

	protected String getNodePath(NodeRef node) {
		StringBuilder nodePath = new StringBuilder();
        PersistenceManagerService persistenceManagerService = getServicesManager().getService(PersistenceManagerService.class);
		try {
			List<FileInfo> paths = persistenceManagerService.getNamePath(
					DmUtils.getNodeRef(persistenceManagerService, ""), node);
			for (FileInfo path : paths) {
				nodePath.append("/").append(path.getName());
			}
		} catch (FileNotFoundException e) {
			logger.error(
					"Error while getting node path for node: " + node.getId(),
					e);
		}
		return nodePath.toString();
	}
}
