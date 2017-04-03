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
 */
package org.craftercms.studio.impl.v1.service.clipboard;

import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;

import org.craftercms.studio.api.v1.exception.ServiceException;

import org.craftercms.studio.api.v1.service.AbstractRegistrableService;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.service.clipboard.ClipboardService;

import java.util.Map;
import java.util.Set;
import java.util.HashSet;

import javax.servlet.http.HttpSession;

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
    public ClipboardItem getItems(String site, HttpSession session) 
    throws ServiceException {
        return getClipboardStore(site, session).getOps();
    }


    @Override
    public boolean cut(String site, String path, HttpSession session) 
    throws ServiceException {
        ClipboardItem clipItem = new ClipboardItem(path, true);
        return clip(site, clipItem, true, session);
    }

    @Override
    public boolean copy(String site, String path, HttpSession session) 
    throws ServiceException {

        ClipboardItem clipItem = new ClipboardItem(path, false);

        return clip(site, clipItem, false, session);
    }

    @Override
    public boolean copy(String site, ClipboardItem clipItem, HttpSession session) 
    throws ServiceException {
        return clip(site, clipItem, false, session);
    }

    @Override
    public Set<String> paste(String site, String destinationPath, HttpSession session) 
    throws ServiceException {
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
    throws ServiceException {
        for(ClipboardItem op : clipOps) {
            try {
                String newPath = null;
                boolean cut = op.isCut;

                if(cut==true) {
                    // RDTMP_COPYPASTE
                    // CopyContent inteface is able to send status and new path yet
                    // newPath = contentService.moveContent(site, op.path, destinationPath);
                    newPath = "RDTMP_COPYPASTE Interface Needs Update " + contentService.moveContent(site, op.path, destinationPath);
                }
                else {
                    // RDTMP_COPYPASTE
                    // CopyContent inteface is able to send status and new path yet
                    // newPath = contentService.copyContent(site, op.path, destinationPath);
                    newPath = "RDTMP_COPYPASTE Interface Needs Update " + contentService.copyContent(site, op.path, destinationPath);

                    // recurse on copied children
                    pasteItems(site, newPath, op.children, pastedItems);
                };

                pastedItems.add(newPath);                
            }
            catch(Exception err) {
                logger.error("Paste operation failed for item '{0}' to dest path `{1}', isCut: '{2}'", err);
            }
        }
    }

    /**
     * Copy and paste operations on the clipboard are infact simply CLIP operations that are tracked.
     * store the item given into session
     *
     * @param site - the project ID
     * @param session - request session
     * @param path to be clipped - items in JSON
     * @param cut - cut?
     */
    protected boolean clip(String site, ClipboardItem item, boolean cut, HttpSession session) {
        ClipboardStore store = getClipboardStore(site, session);

        store.clear();

        store.addOp(item);

        return true;
    };

    /**
     * wrapper around a map used to store clipboard opearations
     */
    protected class ClipboardStore {

        public ClipboardStore() {
            this.op = null;
        };

        public void clear() {
            this.op = null;
        };

        public boolean addOp(ClipboardItem op) {
            this.op = op;
            return true;
        };

        public ClipboardItem getOps() {
            return this.op;
        };

        protected ClipboardItem op;
    } 

    /**
     * store containing clipped operations
     * currently this leverages session
     */
    public ClipboardStore getClipboardStore(String site,  HttpSession session) { 
        
        ClipboardStore clipboardStore = (ClipboardStore)session.getAttribute(site + "-clipboard-condition");

        if(clipboardStore == null) {
            clipboardStore = new ClipboardStore();  
            session.setAttribute(site + "-clipboard-condition", clipboardStore);
        }

        return clipboardStore; 
    }


    protected ContentService contentService;

    public ContentService getContentService() { return contentService; }
    public void setContentService(ContentService contentService) { this.contentService = contentService; }
}
