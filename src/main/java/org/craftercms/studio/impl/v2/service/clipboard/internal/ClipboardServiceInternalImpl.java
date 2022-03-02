/*
 * Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.craftercms.studio.impl.v2.service.clipboard.internal;

import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.service.workflow.WorkflowService;
import org.craftercms.studio.api.v2.event.content.ContentEvent;
import org.craftercms.studio.api.v2.service.clipboard.internal.ClipboardServiceInternal;
import org.craftercms.studio.model.clipboard.Operation;
import org.craftercms.studio.model.clipboard.PasteItem;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.io.FilenameUtils.getFullPathNoEndSeparator;
import static org.apache.commons.io.FilenameUtils.getName;

/**
 * Default implementation of {@link ClipboardServiceInternal}
 *
 * <p>Note: This class could be removed in the future if the logic is moved to the new content service</p>
 *
 * @author joseross
 * @since 3.2
 */
public class ClipboardServiceInternalImpl implements ClipboardServiceInternal, ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(ClipboardServiceInternalImpl.class);

    protected ContentService contentService;
    protected WorkflowService workflowService;
    protected ApplicationContext applicationContext;

    public List<String> pasteItems(String siteId, Operation operation, String targetPath, PasteItem item)
            throws ServiceLayerException, UserNotFoundException {
        var pastedItems = new LinkedList<String>();
        pasteItemsInternal(siteId, operation, targetPath, List.of(item), pastedItems);
        return pastedItems;
    }

    // Code based on the original clipboard service v1
    protected void pasteItemsInternal(String siteId, Operation operation, String targetPath, List<PasteItem> items,
                                      List<String> pastedItems) throws ServiceLayerException, UserNotFoundException {
        for(var item : items) {
            try {
                String newPath = null;
                switch (operation) {
                    case CUT:
                        // RDTMP_COPYPASTE
                        // CopyContent interface is able to send status and new path yet
                        workflowService.cleanWorkflow(item.getPath(), siteId, Collections.emptySet());
                        newPath = contentService.moveContent(siteId, item.getPath(), targetPath);
                        break;
                    case COPY:
                        // RDTMP_COPYPASTE
                        // CopyContent interface is able to send status and new path yet
                        newPath = contentService.copyContent(siteId, item.getPath(), targetPath);

                        // recurse on copied children
                        if (isNotEmpty(item.getChildren())) {
                            pasteItemsInternal(siteId, operation, newPath, item.getChildren(), pastedItems);
                        }
                        break;
                    default:
                        logger.warn("Unsupported clipboard operation {0}", operation);
                }

                pastedItems.add(newPath);
            } catch (Exception err) {
                logger.error("Paste operation {0} failed for item {1} to dest path {2}", err, operation,
                        item.getPath(), targetPath);
                throw err;
            }
        }

        // trigger preview deploy
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        applicationContext.publishEvent(new ContentEvent(auth, siteId, targetPath));
    }

    public String duplicateItem(String siteId, String path) throws ServiceLayerException, UserNotFoundException {
        // Get the parent url: for folders & components it's just parent, for pages it's the parent of the parent
        var filename = getName(path);
        // TODO: Find a better way to do this?
        var parentUrl = "index.xml".equals(filename)? getFullPathNoEndSeparator(getFullPathNoEndSeparator(path))
                                                            : getFullPathNoEndSeparator(path);
        var item = contentService.getContentItem(siteId, parentUrl, 0);
        return contentService.copyContent(siteId, path, item.uri);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public ContentService getContentService() {
        return contentService;
    }

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    public WorkflowService getWorkflowService() {
        return workflowService;
    }

    public void setWorkflowService(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

}
