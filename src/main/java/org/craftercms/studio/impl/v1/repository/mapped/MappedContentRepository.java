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
package org.craftercms.studio.impl.v1.repository.mapped;

import java.io.IOException;
import java.io.InputStream;
import java.lang.String;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;

import org.craftercms.studio.api.v1.to.VersionTO;
import org.craftercms.studio.api.v1.repository.RepositoryItem;
import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.impl.v1.repository.AbstractContentRepository;
import org.craftercms.studio.api.v1.repository.ContentRepository;


/**
 * Map repository calls to the repository name that has been configured 
 * @author russdanner
 *
 */
public class MappedContentRepository extends AbstractContentRepository {

    private static final Logger logger = LoggerFactory.getLogger(MappedContentRepository.class);
    private Map<String, ContentRepository> repositoryMap;
    private String repositoryType;

    public Map<String, ContentRepository> getRepositoryMap() {
        return repositoryMap;
    }

    public void setRepositoryMap(Map<String, ContentRepository> map) {
        repositoryMap = map;
    }

    public String getRepositoryType() {
        return repositoryType;
    }

    public void setRepositoryType(String type) {
        repositoryType = type;
    }

    public void registerRepository(String type, ContentRepository repo) {
        repositoryMap.put(type, repo);
    }

    protected ContentRepository lookupRepo(String key) {
        ContentRepository repo = repositoryMap.get(key);

        if(repo == null) {
            throw new java.lang.RuntimeException("respository type '"+key+"' not found.  Check server config.");
        }

        return repo;
    }

    /**
     * default constructor 
     */
    public MappedContentRepository() {
        repositoryMap = new HashMap<String, ContentRepository>();
        repositoryType = "default";
    }

    @Override
    public InputStream getContent(String path) throws ContentNotFoundException {
        ContentRepository repo = lookupRepo(repositoryType);
        return repo.getContent(path);
    }

    @Override
    public boolean contentExists(String path) {
       ContentRepository repo = lookupRepo(repositoryType);
        return repo.contentExists(path);
     }

    @Override
    public boolean writeContent(String path, InputStream content) {
       ContentRepository repo = lookupRepo(repositoryType);
        return repo.writeContent(path, content);
    }

    @Override
    public boolean createFolder(String path, String name) {
       ContentRepository repo = lookupRepo(repositoryType);
        return repo.createFolder(path, name);
    }

    @Override
    public boolean deleteContent(String path) {
       ContentRepository repo = lookupRepo(repositoryType);
        return repo.deleteContent(path);
    }

    @Override
    public boolean copyContent(String fromPath, String toPath) {
       ContentRepository repo = lookupRepo(repositoryType);
        return repo.copyContent(fromPath, toPath);
    }

    @Override
    public boolean moveContent(String fromPath, String toPath) {
        ContentRepository repo = lookupRepo(repositoryType);
        return repo.moveContent(fromPath, toPath);
    }

    @Override
    public boolean moveContent(String fromPath, String toPath, String newName) {
        ContentRepository repo = lookupRepo(repositoryType);
        return repo.moveContent(fromPath, toPath, newName);
    }

    @Override
    public RepositoryItem[] getContentChildren(String path) {
       ContentRepository repo = lookupRepo(repositoryType);
        return repo.getContentChildren(path);
    }

    @Override
    public VersionTO[] getContentVersionHistory(String path) {
       ContentRepository repo = lookupRepo(repositoryType);
        return repo.getContentVersionHistory(path);
    }

    @Override
    public String createVersion(String path, boolean majorVersion) {
        ContentRepository repo = lookupRepo(repositoryType);
        return repo.createVersion(path, majorVersion);
    }

    @Override
    public String createVersion(String path, String comment, boolean majorVersion) {
        ContentRepository repo = lookupRepo(repositoryType);
        return repo.createVersion(path, comment, majorVersion);
    }

    @Override
    public boolean revertContent(String path, String label, boolean major, String comment) {
        ContentRepository repo = lookupRepo(repositoryType);
        return repo.revertContent(path, label, major, comment);
    }

    @Override
    public InputStream getContentVersion(String path, String version) 
	throws ContentNotFoundException {    
    	ContentRepository repo = lookupRepo(repositoryType);
       	return repo.getContentVersion(path, version);
    }
    
    @Override
    public void lockItem(String site, String path) {
       ContentRepository repo = lookupRepo(repositoryType);
       repo.lockItem(site, path);
    }

    @Override
    public void unLockItem(String site, String path) {
       ContentRepository repo = lookupRepo(repositoryType);
        repo.unLockItem(site, path);
    }

    @Override
    public Date getModifiedDate(String path) {
        ContentRepository repo = lookupRepo(repositoryType);
        return repo.getModifiedDate(path);
    }
}