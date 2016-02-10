/*******************************************************************************
 * Crafter Studio Web-content authoring solution
 *     Copyright (C) 2007-2016 Crafter Software Corporation.
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

import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;

import org.craftercms.studio.api.v1.repository.ContentRepository;


/**
 * Easily register any repository with the mapper
 * @author russdanner
 *
 */
public class MappedContentRepositoryRegistration {

    private static final Logger logger = LoggerFactory.getLogger(MappedContentRepositoryRegistration.class);

    private MappedContentRepository repository; 
    public MappedContentRepository getMappedContentRepository() { return repository; }
    public void setMappedContentRepository(MappedContentRepository repo) { repository = repo; }

    private ContentRepository targetRepository; 
    public ContentRepository getContentRepository() { return targetRepository; }
    public void setContentRepository(ContentRepository repo) { targetRepository = repo; }

    private String repoType; 
    public String getRepositoryType() { return repoType; }
    public void setRepositoryType(String type) { repoType = type; }

    public void registerRepository() {
        repository.registerRepository(repoType, targetRepository);
    }
}
