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
package org.craftercms.studio.impl.v1.repository.path;

import java.io.IOException;
import java.io.InputStream;
import java.lang.String;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;

import org.craftercms.studio.api.v1.to.VersionTO;
import org.craftercms.studio.api.v1.repository.RepositoryItem;
import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.impl.v1.repository.AbstractContentRepository;
import org.craftercms.studio.api.v1.repository.ContentRepository;

/**
 * Map repository paths to specific repositories
 * @author russdanner
 * 
 * From RD: We may want to change the data stucture of the map so that you can more predictably 
 *          order the patterns which may help in some cases.  The basic assumption of this 
 *          implementation is that patterns are basic and the repositories are neatly split
 */
public class PathMappedContentRepository extends AbstractContentRepository {

    private static final Logger logger = LoggerFactory.getLogger(PathMappedContentRepository.class);
    private ContentRepository defaultRepo;
    private Map<String, ContentRepository> pathRepoMap;

    public ContentRepository getDefaultRepository() {
        return defaultRepo;
    }

    public void setDefaultRepository(ContentRepository repo) {
        defaultRepo = repo;
    }

    public Map<String, ContentRepository> getPathRepositoryMap() {
        return pathRepoMap;
    }

    public void setPathRepositoryMap(Map<String, ContentRepository> repoMap) {
        pathRepoMap = repoMap;
    }

    /**
     * default constructor 
     */
    public PathMappedContentRepository() {
        setPathRepositoryMap(new HashMap<String, ContentRepository>());
    }

    /**
     * given a path look up the repository that content should be directed
     * @param path the path for the content request
     */
    protected ContentRepository lookupRepo(String path) {
        ContentRepository retRepo = defaultRepo;
        Set<String> pathRegexs = pathRepoMap.keySet();

        for(String pathRegex : pathRegexs){
            try {
                // note this can be optimized by compiling at startup
                Matcher m = Pattern.compile(pathRegex).matcher(path);
                if(m.find()) {
                    // path must match first group. Sub groups don't make sense in this context
                    retRepo = pathRepoMap.get(pathRegex);
                    break;
                }
            }
            catch(Exception regexErr) {
                logger.error("error looking up repo with regex '{0}' and path '{1}'", regexErr, pathRegex, path);
            }
        }
        

        if(retRepo == null) {
            logger.error("Repo is null, cannot complete operation.  No default set and '{0}' does not match any mapped repository,", path);
            throw new RuntimeException("Repo is null, cannot complete operation.");
        }

        return retRepo;
    }

   @Override
    public InputStream getContent(String path) throws ContentNotFoundException {
        ContentRepository repo = lookupRepo(path);
        return repo.getContent(path);
    }

    @Override
    public boolean contentExists(String path) {
       ContentRepository repo = lookupRepo(path);
        return repo.contentExists(path);
     }

    @Override
    public boolean writeContent(String path, InputStream content) {
       ContentRepository repo = lookupRepo(path);
        return repo.writeContent(path, content);
    }

    @Override
    public boolean createFolder(String path, String name) {
       ContentRepository repo = lookupRepo(path);
        return repo.createFolder(path, name);
    }

    @Override
    public boolean deleteContent(String path) {
       ContentRepository repo = lookupRepo(path);
        return repo.deleteContent(path);
    }

    @Override
    public boolean copyContent(String fromPath, String toPath) {
        ContentRepository fromRepo = lookupRepo(fromPath);
        ContentRepository toRepo = lookupRepo(toPath);

        if(fromRepo != toRepo) {
            logger.error("From repository for path '{0}' and To repository for path '{1}' do not match", fromPath, toPath);
            throw new RuntimeException("cross repo copy operation not supported");  
        }

        return fromRepo.copyContent(fromPath, toPath);
    }

    @Override
    public boolean moveContent(String fromPath, String toPath) {
        ContentRepository fromRepo = lookupRepo(fromPath);
        ContentRepository toRepo = lookupRepo(toPath);

        if(fromRepo != toRepo) {
            logger.error("From repository for path '{0}' and To repository for path '{1}' do not match", fromPath, toPath);
            throw new RuntimeException("cross repo move operation not supported"); 
        }

        return fromRepo.moveContent(fromPath, toPath);
    }

    @Override
    public boolean moveContent(String fromPath, String toPath, String newName) {
        ContentRepository fromRepo = lookupRepo(fromPath);
        ContentRepository toRepo = lookupRepo(toPath);

        if(fromRepo != toRepo) {
            logger.error("From repository for path '{0}' and To repository for path '{1}' do not match", fromPath, toPath);
            throw new RuntimeException("cross repo move operation not supported");
        }

        return fromRepo.moveContent(fromPath, toPath, newName);
    }

    @Override
    public RepositoryItem[] getContentChildren(String path) {
       ContentRepository repo = lookupRepo(path);
        return repo.getContentChildren(path);
    }

    @Override
    public VersionTO[] getContentVersionHistory(String path) {
       ContentRepository repo = lookupRepo(path);
        return repo.getContentVersionHistory(path);
    }

    @Override
    public String createVersion(String path, boolean majorVersion) {
        ContentRepository repo = lookupRepo(path);
        return repo.createVersion(path, majorVersion);
    }

    @Override
    public String createVersion(String path, String comment, boolean majorVersion) {
        ContentRepository repo = lookupRepo(path);
        return repo.createVersion(path, comment, majorVersion);
    }

    @Override
    public boolean revertContent(String path, String label, boolean major, String comment) {
        ContentRepository repo = lookupRepo(path);
        return repo.revertContent(path, label, major, comment);
    }

    @Override
    public void lockItem(String site, String path) {
       ContentRepository repo = lookupRepo(path);
       repo.lockItem(site, path);
    }

    @Override
    public void unLockItem(String site, String path) {
       ContentRepository repo = lookupRepo(path);
        repo.unLockItem(site, path);
    }

    @Override
    public Date getModifiedDate(String path) {
        ContentRepository repo = lookupRepo(path);
        return repo.getModifiedDate(path);
    }
}