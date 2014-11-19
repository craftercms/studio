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

import javolution.util.FastMap;
import org.craftercms.cstudio.alfresco.content.pipeline.api.PipelineContent;
import org.craftercms.cstudio.alfresco.dm.constant.DmConstants;
import org.craftercms.cstudio.alfresco.dm.service.api.DmDependencyService;
import org.craftercms.cstudio.alfresco.service.ServicesManager;
import org.craftercms.cstudio.alfresco.service.api.ServicesConfig;
import org.craftercms.cstudio.alfresco.service.exception.ContentProcessException;
import org.craftercms.cstudio.alfresco.service.exception.ServiceException;
import org.craftercms.cstudio.alfresco.to.ResultTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExtractAssetDependencyProcessor extends PathMatchProcessor {

    private static final Logger logger = LoggerFactory.getLogger(ExtractAssetDependencyProcessor.class);

    public static final String NAME = "ExtractAssetDependencyProcessor";

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
    public ExtractAssetDependencyProcessor() {
        super(NAME);
    }

    /**
     * constructor that sets the process name
     *
     * @param name
     */
    public ExtractAssetDependencyProcessor(String name) {
        super(name);
    }

    public void process(PipelineContent content, ResultTO result) throws ContentProcessException {
        ServicesConfig servicesConfig = getServicesManager().getService(ServicesConfig.class);
        String site = content.getProperty(DmConstants.KEY_SITE);
        String folderPath = content.getProperty(DmConstants.KEY_FOLDER_PATH);
        String fileName = content.getProperty(DmConstants.KEY_FILE_NAME);
        String path = (folderPath.endsWith("/")) ? folderPath + fileName : folderPath + "/" + fileName;
        StringWriter sw = new StringWriter();
        boolean isCss = path.endsWith(DmConstants.CSS_PATTERN);
        boolean isJs = path.endsWith(DmConstants.JS_PATTERN);
        List<String> templatePatterns = servicesConfig.getRenderingTemplatePatterns(site);
        boolean isTemplate = false;
        for (String templatePattern : templatePatterns) {
            Pattern pattern = Pattern.compile(templatePattern);
            Matcher matcher = pattern.matcher(path);
            if (matcher.matches()) {
                isTemplate = true;
                break;
            }
        }
        try {
            if (isCss || isJs || isTemplate) {
                InputStream is = content.getContentStream();
                is.reset();
                int size = is.available();
                char[] theChars = new char[size];
                byte[] bytes    = new byte[size];

                is.read(bytes, 0, size);
                for (int i = 0; i < size;) {
                    theChars[i] = (char)(bytes[i++]&0xff);
                }

                StringBuffer assetContent = new StringBuffer(new String(theChars));
                DmDependencyService dmDependencyService = getServicesManager().getService(DmDependencyService.class);
                Map<String, Set<String>> globalDeps = new FastMap<String, Set<String>>();
                if (isCss) {
                    dmDependencyService.extractDependenciesStyle(site, path, assetContent, globalDeps);
                } else if (isJs) {
                    dmDependencyService.extractDependenciesJavascript(site, path, assetContent, globalDeps);
                } else if (isTemplate) {
                    dmDependencyService.extractDependenciesTemplate(site, path, assetContent, globalDeps);
                }
                content.getContentStream().reset();
            }
        } catch (ServiceException e) {
            throw new ContentProcessException(e);
        } catch (IOException e) {
            throw new ContentProcessException(e);
        }
    }
}
