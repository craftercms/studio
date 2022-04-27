/*
 * Copyright (C) 2007-2020 Crafter Software Corporation. All Rights Reserved.
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
package org.craftercms.studio.impl.v1.service.clipboard;

import org.craftercms.commons.validation.annotations.param.ValidateParams;
import org.craftercms.commons.validation.annotations.param.ValidateSecurePathParam;
import org.craftercms.commons.validation.annotations.param.ValidateStringParam;
import org.craftercms.studio.api.v1.ebus.PreviewEventContext;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;

import org.craftercms.studio.api.v1.exception.ServiceLayerException;

import org.craftercms.studio.api.v1.service.AbstractRegistrableService;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.service.clipboard.ClipboardService;
import org.craftercms.studio.api.v1.service.event.EventService;
import org.craftercms.studio.api.v1.service.workflow.WorkflowService;
import org.craftercms.studio.api.v1.to.DmDependencyTO;

import java.util.Collections;
import java.util.Set;
import java.util.HashSet;

import javax.servlet.http.HttpSession;

import static org.craftercms.studio.api.v1.ebus.EBusConstants.EVENT_PREVIEW_SYNC;

/**
 * Clipboard service that tracks items clipped (COPY/CUT) by the caller and executes the 
 * proper content services (COPY TO DEST, MOVE TO DEST) on paste operation. 
 */
public class ClipboardServiceImpl extends AbstractRegistrableService 
implements ClipboardService {

    protected static final Logger logger = LoggerFactory.getLogger(ClipboardServiceImpl.class);

    @Override
    public void register() {
        getServicesManager().registerService(ClipboardService.class, this);
    }

    @Override
    @ValidateParams
    public ClipboardItem getItems(@ValidateStringParam(name = "site") String site, HttpSession session)
    throws ServiceLayerException {
        return getClipboardStore(site, session).getOps();
    }


    @Override
    @ValidateParams
    public boolean cut(@ValidateStringParam(name = "site") String site, @ValidateSecurePathParam(name = "path") String path, HttpSession session)
    throws ServiceLayerException {
        ClipboardItem clipItem = new ClipboardItem(path, true);
        return clip(site, clipItem, true, session);
    }

    @Override
    @ValidateParams
    public boolean copy(@ValidateStringParam(name = "site") String site, @ValidateSecurePathParam(name = "path") String path, HttpSession session)
    throws ServiceLayerException {

        ClipboardItem clipItem = new ClipboardItem(path, false);

        return clip(site, clipItem, false, session);
    }

    @Override
    @ValidateParams
    public boolean copy(@ValidateStringParam(name = "site") String site, ClipboardItem clipItem, HttpSession session)
    throws ServiceLayerException {
        return clip(site, clipItem, false, session);
    }

    @Override
    @ValidateParams
    public Set<String> paste(@ValidateStringParam(name = "site") String site, @ValidateStringParam(name = "destinationPath") String destinationPath, HttpSession session)
    throws ServiceLayerException {
        Set<String> pastedItems = new HashSet<String>();

        ClipboardItem clipOp = getItems(site, session);

        if(clipOp != null) {
            Set<ClipboardItem> clipOps = new HashSet<ClipboardItem>();
            clipOps.add(clipOp);

            pasteItems(site, destinationPath, clipOps, pastedItems);
        }

        return pastedItems;
    }

    /**
     * Recursive paste operation on item and it's children
     * @param site site ID
     * @param destinationPath destination path for itme
     * @param clipOps ops to be pasted
     * @param pastedItems collection of (new) pasted paths
     */
    protected void pasteItems(String site, String destinationPath, Set<ClipboardItem> clipOps, Set<String> pastedItems) 
    throws ServiceLayerException {
        for(ClipboardItem op : clipOps) {
            try {
                String newPath = null;
                boolean cut = op.isCut;

                if (cut) {
                    // RDTMP_COPYPASTE
                    // CopyContent inteface is able to send status and new path yet
                    workflowService.cleanWorkflow(op.path, site, Collections.<DmDependencyTO>emptySet());
                    newPath = contentService.moveContent(site, op.path, destinationPath);
                }
                else {
                    // RDTMP_COPYPASTE
                    // CopyContent inteface is able to send status and new path yet
                    newPath = contentService.copyContent(site, op.path, destinationPath);

                    // recurse on copied children
                    pasteItems(site, newPath, op.children, pastedItems);
                }

                pastedItems.add(newPath);                
            }
            catch(Exception err) {
                logger.error("Paste operation failed for item '{0}' to dest path '{1}', isCut: '{2}'",
                             err, op.path, destinationPath, op.isCut);
            }
        }

        // trigger preview deploy
        PreviewEventContext context = new PreviewEventContext();
        context.setSite(site);
        eventService.publish(EVENT_PREVIEW_SYNC, context);
    }

    /**
     * Copy and paste operations on the clipboard are infact simply CLIP operations that are tracked.
     * store the item given into session
     *
     * @param site - the project ID
     * @param session - request session
     * @param item to be clipped - items in JSON
     * @param cut - cut?
     */
    protected boolean clip(String site, ClipboardItem item, boolean cut, HttpSession session) {
        ClipboardStore store = getClipboardStore(site, session);

        store.clear();

        store.addOp(item);

        return true;
    }

    /**
     * store containing clipped operations
     * currently this leverages session
     */
    @Override
    @ValidateParams
    public ClipboardStore getClipboardStore(@ValidateStringParam(name = "site") String site,  HttpSession session) {
        
        ClipboardStore clipboardStore = (ClipboardStore)session.getAttribute(site + "-clipboard-condition");

        if(clipboardStore == null) {
            clipboardStore = new ClipboardStore();  
            session.setAttribute(site + "-clipboard-condition", clipboardStore);
        }

        return clipboardStore; 
    }


    protected ContentService contentService;
    protected WorkflowService workflowService;
    protected EventService eventService;

    public ContentService getContentService() { return contentService; }
    public void setContentService(ContentService contentService) { this.contentService = contentService; }

    public WorkflowService getWorkflowService() { return workflowService; }
    public void setWorkflowService(WorkflowService workflowService) { this.workflowService = workflowService; }

    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

}
