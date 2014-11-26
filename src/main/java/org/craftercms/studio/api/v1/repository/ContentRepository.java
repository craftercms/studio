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
package org.craftercms.studio.api.v1.repository;


import org.craftercms.studio.api.v1.service.deployment.CopyToEnvironmentItem;
import org.craftercms.studio.api.v1.service.deployment.DeploymentException;
import org.craftercms.studio.api.v1.service.deployment.PublishingTargetItem;
import org.craftercms.studio.api.v1.service.fsm.TransitionEvent;
import org.craftercms.studio.api.v1.to.VersionTO;

import javax.transaction.UserTransaction;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

/**
 * This interface represents the repository layer of Crafter Studio.  All interaction with the backend 
 * Store must go through this interface.
 * @author russdanner
 */
public interface ContentRepository {

    /**
     * Determine if content exists in the repository at a given path
     *
     * @param path
     * @return true if site has content object at path
     */
    boolean contentExists(String path);

    /**
     * get document from wcm content
     *
     * @param path
     * @return document
     */
    InputStream getContent(String path);

    /**
     * write content
     * @param path path to content
     * @param content stream of content to write
     * @return true if successful
     */
    boolean writeContent(String path, InputStream content);

    /**
     * create a folder
     *
     * @param path path to create a folder in
     * @param name a folder name to create
     * @return true if successful
     */
    boolean createFolder(String path, String name);

    /**
     * delete content
     * @param path path to content
     */
    boolean deleteContent(String path);

    /**
     * move content from PathA to pathB
     *
     * @param fromPath source content
     * @param toPath target path
     * @return true if successful
     */
    boolean moveContent(String fromPath, String toPath);

    /**
     * copy content from PathA to pathB
     *
     * @param fromPath paths to content
     * @param toPath target path
     * @return true if successful
     */
    boolean copyContent(String fromPath, String toPath);

    /**
     * get immediate children for path
     * @param path path to content
     * @return a list of children
     */
    RepositoryItem[] getContentChildren(String path);

    /** 
     * get the version history for an item
     * @param site - the project ID
     * @param path - the path of the item
     * @return a list of versions
     */
    VersionTO[] getContentVersionHistory(String path);

    /** 
     * revert a version (create a new version based on an old version)
     * @param path - the path of the item to "revert"
     * @param version - old version ID to base to version on
     * @return true if successful
     */
    boolean revertContent(String path, String version, boolean major, String comment);








/* ===  */
// Maybes
    void lockItem(String site, String path);

    void unLockItem(String site, String path);








/* ========================================================================== */
// everything below here is methods that will not be part of the interface

    /**
     * get content
     * @param site the site project id
     * @param variant variant is a variation of the site (like a translation for example)
     * @param store is an area to write to (live, stage, work-area, ...)
     * @param path is the file path to write
     */
    InputStream getContent(String site, String variant, String store, String path);


    /**
     * get transaction
     */
    UserTransaction getTransaction();
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

}
