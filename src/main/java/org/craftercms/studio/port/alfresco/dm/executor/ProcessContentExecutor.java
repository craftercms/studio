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
package org.craftercms.cstudio.alfresco.dm.executor;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.craftercms.cstudio.alfresco.constant.CStudioConstants;
import org.craftercms.cstudio.alfresco.content.pipeline.api.ContentProcessorPipeline;
import org.craftercms.cstudio.alfresco.content.pipeline.api.PipelineContent;
import org.craftercms.cstudio.alfresco.dm.constant.DmConstants;
import org.craftercms.cstudio.alfresco.dm.content.pipeline.impl.PipelineContentImpl;
import org.craftercms.cstudio.alfresco.service.ServicesManager;
import org.craftercms.cstudio.alfresco.service.api.PersistenceManagerService;
import org.craftercms.cstudio.alfresco.service.exception.ContentProcessException;
import org.craftercms.cstudio.alfresco.service.exception.ServiceException;
import org.craftercms.cstudio.alfresco.to.ResultTO;
import org.craftercms.cstudio.alfresco.util.ContentUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class ProcessContentExecutor {

    private static final Logger logger = LoggerFactory.getLogger(ProcessContentExecutor.class);

    /** a chain of processors to be executed on wcm content */
    protected Map<String, ContentProcessorPipeline> _processorChains;
    public Map<String, ContentProcessorPipeline> getProcessorChains() {
        return _processorChains;
    }
    public void setProcessorChains(Map<String, ContentProcessorPipeline> processorChains) {
        this._processorChains = processorChains;
    }

    protected ServicesManager servicesManager;
    public ServicesManager getServicesManager() {
        return servicesManager;
    }
    public void setServicesManager(ServicesManager servicesManager) {
        this.servicesManager = servicesManager;
    }

    /**
     *
     * @param id
     * @param input
     * @param isXml
     * @param params
     * @param chainName
     * @return result
     * @throws org.craftercms.cstudio.alfresco.service.exception.ServiceException
     */
    public ResultTO processContent(final String id, final InputStream input, final boolean isXml, final Map<String, String> params, final String chainName) throws ServiceException {
        return processContentInTransaction(id, input, isXml, params, chainName);
    }

    /**
     * process content within transaction
     *
     * @param id
     * @param input
     * @param isXml
     * @param params
     * @param chainName
     * @return
     * @throws ServiceException
     */
    protected ResultTO processContentInTransaction(final String id, final InputStream input, final boolean isXml, final Map<String, String> params, final String chainName) throws ServiceException {
        final ContentProcessorPipeline chain = _processorChains.get(chainName);
        try{
            if (chain != null) {
                if (StringUtils.isEmpty(params.get(DmConstants.KEY_USER))) {
                    PersistenceManagerService persistenceManagerService = getServicesManager().getService(PersistenceManagerService.class);
                    String user = persistenceManagerService.getCurrentUserName();
                    params.put(DmConstants.KEY_USER, user);
                }

                byte[] inputBytes= null;
                try {
                    if (input != null) {
                        inputBytes = IOUtils.toByteArray(input);
                    }
                } catch (IOException e) {
                    throw new ServiceException("Error while creating byte array",e);
                }finally{
                    ContentUtils.release(input);
                }

                final ResultTO result = new ResultTO();
                final byte[] inputBytesFinal = inputBytes;

                InputStream newByteArrayStream=null;
                try {
                    newByteArrayStream = (inputBytesFinal != null) ? new ByteArrayInputStream(inputBytesFinal) : null;
                    final PipelineContent content = new PipelineContentImpl(id, newByteArrayStream, isXml, null, CStudioConstants.CONTENT_ENCODING, params);
                    chain.processContent(content, result);

                } catch (ContentProcessException e) {
                    logger.error("Error in chain for write content", e);
                    throw e;
                } catch (RuntimeException e) {
                    logger.error("Error in chain for write content", e);
                    throw e;
                }finally{
                    ContentUtils.release(newByteArrayStream);
                }
                return result;

            } else {
                ContentUtils.release(input);
                throw new ServiceException(chainName + " is not defined.");
            }
        }finally {
            String s = params.get(DmConstants.KEY_USER);
            AuthenticationUtil.setFullyAuthenticatedUser(s);
        }
    }
}
