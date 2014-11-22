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
package org.craftercms.cstudio.api.repository;

//import org.craftercms.cstudio.alfresco.deployment.DeploymentEventItem;
import org.craftercms.cstudio.api.service.deployment.CopyToEnvironmentItem;
import org.craftercms.cstudio.api.service.deployment.DeploymentException;
import org.craftercms.cstudio.api.service.deployment.PublishingTargetItem;
import org.craftercms.cstudio.api.service.fsm.TransitionEvent;
import org.dom4j.Document;
import org.dom4j.DocumentException;

import javax.transaction.UserTransaction;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

/**
 * This interface represents the repository layer of Crafter Studio.  All interaction with the backend 
 * Store must go through this interface.
 * @author russdanner
 */
public interface ContentRepository {


    /**
     * get transaction
     */
    UserTransaction getTransaction();

    /**
     * Determine if content exists in the repository at a given path
     * @param site name
     * @param path
     * @return true if site has content object at path
     */
    boolean contentExists(String site, String path);

    /**
     * get document from wcm content
     * @param path
     * @return document
     * @throws ServiceException
     */
    Document getContentAsDocument(String path)
            throws DocumentException;

    /**
     * get document from wcm content
     *
     * @param path
     * @return document
     * @throws ServiceException
     */
    InputStream getContent(String path);

    /**
     * get content
     * @param site the site project id
     * @param variant variant is a variation of the site (like a translation for example)
     * @param store is an area to write to (live, stage, work-area, ...)
     * @param path is the file path to write
     */
    InputStream getContent(String site, String variant, String store, String path);

    /**
     * write content
     * @param path path to content
     * @param content stream of content to write
     */
    void writeContent(String path, InputStream content);

    /**
     * write content
     * @param site the site project id
     * @param variant variant is a variation of the site (like a translation for example)
     * @param store is an area to write to (live, stage, work-area, ...)
     * @param path is the file path to write
     * @param content is the bits to write
     */
    void writeContent(String site, String variant, String store, String path, InputStream content);

    void stateTransition(String site, List<String> paths, TransitionEvent event);

    // are these repository interfaces?  I don't think they have much to do with the repository
    void stateTransition(String site, String path, TransitionEvent event);

    void setSystemProcessing(String site, List<String> paths, boolean isSystemProcessing);

    void setSystemProcessing(String site, String path, boolean isSystemProcessing);

    void createNewVersion(String site, String path, String submissionComment, boolean isMajorVersion);

    void copyToEnvironment(String site, String environment, String path) throws DeploymentException;

    Set<String> getAllAvailableSites();

    Set<PublishingTargetItem> getAllTargetsForSite(String site);

    String getCurrentUser();

    String getAdministratorUser();

    boolean isNew(String site, String path);

    String getFilename(String site, String path);

    boolean isRenamed(String site, String path);

    String getOldPath(String site, String path);

    InputStream getMetadataStream(String site, String path);

    //void publishDeployEvent(String endpoint, List<DeploymentEventItem> items);

    void deleteContent(CopyToEnvironmentItem item);

    void deleteContent(String site, String environment, String path);

    void clearRenamed(String site, String path);

    String getContentTypeClass(String site,  String path);

    void setLockBehaviourEnabled(boolean enabled);

    String getFullPath(String site, String path);

    List<String> getDependentPaths(String site, String path);

    boolean isFolder(String site, String path);

    boolean environmentRepoExists(String site, String environment);

    void createEnvironmentRepo(String site, String environment);

    String getLiveEnvironmentName(String site);

    Set<String> getAllPublishingEnvironments(String site);

    void lockRepository();

    void unlockRepository();

    void lockItem(String site, String path);

    void unLockItem(String site, String path);
}
