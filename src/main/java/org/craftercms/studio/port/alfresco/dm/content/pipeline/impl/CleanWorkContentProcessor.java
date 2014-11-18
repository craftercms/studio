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
package org.craftercms.cstudio.alfresco.dm.content.pipeline.impl;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.craftercms.cstudio.alfresco.content.pipeline.api.PipelineContent;
import org.craftercms.cstudio.alfresco.content.pipeline.impl.BaseContentProcessor;
import org.craftercms.cstudio.alfresco.dm.constant.DmConstants;
import org.craftercms.cstudio.alfresco.dm.service.api.DmContentService;
import org.craftercms.cstudio.alfresco.dm.service.api.DmDependencyService;
import org.craftercms.cstudio.alfresco.dm.service.api.DmTransactionService;
import org.craftercms.cstudio.alfresco.service.ServicesManager;
import org.craftercms.cstudio.alfresco.service.exception.ContentProcessException;
import org.craftercms.cstudio.alfresco.service.exception.ServiceException;
import org.craftercms.cstudio.alfresco.to.ResultTO;
import org.craftercms.cstudio.alfresco.util.TransactionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class CleanWorkContentProcessor extends BaseContentProcessor {

    private static final Logger logger = LoggerFactory.getLogger(CleanWorkContentProcessor.class);

    public static final String NAME = "CleanWorkContentProcessor";

    protected ServicesManager servicesManager;
    public ServicesManager getServicesManager() {
        return servicesManager;
    }
    public void setServicesManager(ServicesManager servicesManager) {
        this.servicesManager = servicesManager;
    }

    /**
     * default constructor
     */
    public CleanWorkContentProcessor() {
        super(NAME);
    }

    /**
     * constructor that sets the process name
     *
     * @param name
     */
    public CleanWorkContentProcessor(String name) {
        super(name);
    }

    @Override
    public void process(PipelineContent content, ResultTO result) throws ContentProcessException {
        String site = content.getProperty(DmConstants.KEY_SITE);
        String user = content.getProperty(DmConstants.KEY_USER);
        String parentPagePath = content.getProperty(DmConstants.KEY_FOLDER_PATH);
        String parentFileName = content.getProperty(DmConstants.KEY_FILE_NAME);
        String parentPath = parentPagePath + "/" + parentFileName;
        AuthenticationService authenticationService = getServicesManager().getService(AuthenticationService.class);
        String usr = authenticationService.getCurrentUserName();
        //if (sandBox != null) {
        //    AuthenticationUtil.setFullyAuthenticatedUser(sandBox);
        //}
        //try {
            /* if (!_dmContentService.isNew(site, parentPath)) {
                wcmContentService.cleanDraftDependencies(parentPath, site);
            }*/
            //deleteRemovedDependenices(content, site, sandBox, parentPath, user);

        //}catch (ServiceException e){
        //    logger.error("Unable to delete Removed dependenices",e);
        //}
        //finally {
        //    AuthenticationUtil.setFullyAuthenticatedUser(usr);
        //}
        //
    }

    /**
     * Delete removed dependenices
     *
     *  a) compare input content stream and earlier version in sandbox to get the removed dependenices
     *  b) delete them if they match the deleteDependnecyPattern
     *
     */
    /*protected void deleteRemovedDependenices(PipelineContent content,final String site, String sandbox, String relativePath, final String user) throws ServiceException {

        //dont have to calculate removedDependenices if the item is brand new
        if(_dmContentService.isItemInSandbox(site, sandbox, relativePath)){
            String draftSandbox = WcmUtils.getPreviewStore(sandbox);
            WcmDependencyDiffService.DiffRequest diffRequest = new WcmDependencyDiffService.DiffRequest(site,relativePath,null,draftSandbox,sandbox,true);
            diffRequest.setSourceDoc(content.getDocument()); //since the doc is only in the stream and not available in store
            final List<String> toDelete = _wcmDependencyService.getRemovedDependenices(diffRequest, true);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug ("Removed dependenices to delete for page : "+relativePath+":"+toDelete);
            }
            //TODO recheck if it works without transaction if not put requires=true
            TransactionHelper txnHelper = _wcmTransactionService.getTransactionHelper();
            RetryingTransactionHelper.RetryingTransactionCallback<String> getSandboxCallback = new RetryingTransactionHelper.RetryingTransactionCallback<String>() {
                public String execute() throws Throwable {
                    for(String dependencyPath:toDelete){
                        wcmContentService.deleteContent(site,dependencyPath,false, true, user);
                    }
                    return null;
                }
            };
            txnHelper.doInTransaction(getSandboxCallback);
        }
    } */
}
