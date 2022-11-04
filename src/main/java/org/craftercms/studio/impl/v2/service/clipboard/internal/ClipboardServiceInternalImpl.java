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

import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v2.exception.content.ContentMoveInvalidLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.service.workflow.WorkflowService;
import org.craftercms.studio.api.v1.to.ContentItemTO;
import org.craftercms.studio.api.v2.exception.InvalidParametersException;
import org.craftercms.studio.api.v2.service.clipboard.internal.ClipboardServiceInternal;
import org.craftercms.studio.api.v2.utils.StudioUtils;
import org.craftercms.studio.model.clipboard.Operation;
import org.craftercms.studio.model.clipboard.PasteItem;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static java.lang.String.format;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.io.FilenameUtils.getFullPathNoEndSeparator;
import static org.apache.commons.lang3.StringUtils.removeEnd;
import static org.craftercms.studio.api.v1.constant.DmConstants.SLASH_INDEX_FILE;
import static org.craftercms.studio.model.clipboard.Operation.CUT;

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

    protected void validatePasteItemsAction(final String siteId, Operation operation, final String sourcePath, final String targetPath)
            throws ServiceLayerException {
        ContentItemTO targetContentItem = contentService.getContentItem(siteId, targetPath);
        if (targetContentItem.isDeleted()) {
            throw new ContentNotFoundException(targetPath, siteId, format("Target path '%s' does not exist. " +
                    "Unable to perform paste operation", targetPath));
        }
        if (!targetContentItem.isPage() && !targetContentItem.isFolder()) {
            throw new InvalidParametersException(format("Invalid paste target '%s' in site '%s'. " +
                    "Only pages and folders can contain children", targetPath, siteId));
        }
        if (!contentService.contentExists(siteId, sourcePath)) {
            throw new ContentNotFoundException(sourcePath, siteId, format("No content found at path '%s' " +
                    "Unable to perform paste operation", sourcePath));
        }
        String sourceTopLevel = StudioUtils.getTopLevelFolder(sourcePath);
        String targetTopLevel = StudioUtils.getTopLevelFolder(targetPath);

        if (!Objects.equals(sourceTopLevel, targetTopLevel)) {
            throw new InvalidParametersException(format("Cannot perform paste operation " +
                            "from '%s' (%s) into '%s' (%s) for site '%s'. " +
                            "Pasting across top level folders is not supported.",
                    sourcePath, sourceTopLevel, targetPath, targetTopLevel, siteId));
        }

        if (CUT == operation) {
            String sourceDirectory = getParentUrl(sourcePath);
            String targetDirectory = removeEnd(targetPath, SLASH_INDEX_FILE);
            if (sourceDirectory.equals(targetDirectory)) {
                throw new ContentMoveInvalidLocation(format("Cannot perform cut-paste operation from '%s' to the same location '%s' for site '%s'",
                        sourcePath, targetPath, siteId));
            }
        }
    }

    public List<String> pasteItems(String siteId, Operation operation, String targetPath, PasteItem item)
            throws ServiceLayerException, UserNotFoundException {
        validatePasteItemsAction(siteId, operation, item.getPath(), targetPath);
        var pastedItems = new LinkedList<String>();
        pasteItemsInternal(siteId, operation, targetPath, List.of(item), pastedItems);
        logger.trace("'{}' items pasted in site '{}' from '{}' to '{}'",
                pastedItems.size(), siteId, item.getPath(), targetPath);
        return pastedItems;
    }

    // Code based on the original clipboard service v1
    protected void pasteItemsInternal(String siteId, Operation operation, String targetPath, List<PasteItem> items,
                                      List<String> pastedItems) throws ServiceLayerException, UserNotFoundException {
        for (var item: items) {
            try {
                String newPath = null;
                switch (operation) {
                    case CUT:
                        // RDTMP_COPYPASTE
                        // CopyContent interface is able to send status and new path yet
                        workflowService.cleanWorkflow(item.getPath(), siteId);
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
                        logger.warn("Unsupported clipboard operation '{}' attempted in site '{}' item '{}' " +
                                        "target path '{}'",
                                operation, siteId, item.getPath(), targetPath);
                }

                pastedItems.add(newPath);
            } catch (Exception e) {
                logger.error("Paste operation '{}' failed in site '{}' item '{}' to target path '{}'",
                        operation, siteId, item.getPath(), targetPath, e);
                throw e;
            }
        }
    }

    public String duplicateItem(String siteId, String path) throws ServiceLayerException, UserNotFoundException {
        String parentUrl = getParentUrl(path);
        var item = contentService.getContentItem(siteId, parentUrl, 0);
        return contentService.copyContent(siteId, path, item.uri);
    }

    /**
     * Get the parent url: for folders & components it's just parent, for pages it's the parent of the parent.
     * e.g.:
     * /site/website/articles/page1/index.xml -> /site/website/articles
     * /site/components/posts/january/clickbait.xml -> /site/components/posts/january
     * /site/components/articles/health/ -> /site/components/articles
     *
     * @param path path of the content item
     * @return path of the parent item
     */
    protected String getParentUrl(String path) {
        return getFullPathNoEndSeparator(removeEnd(path, SLASH_INDEX_FILE));
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    public void setWorkflowService(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

}
