/*
 * Copyright (C) 2007-2018 Crafter Software Corporation. All rights reserved.
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
 *
 */

package org.craftercms.studio.impl.v2.service.cluster;

import org.craftercms.studio.api.v1.dal.SiteFeed;
import org.craftercms.studio.api.v1.dal.SiteFeedMapper;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.api.v2.dal.Cluster;
import org.craftercms.studio.api.v2.service.cluster.StudioNodeSyncTask;

import java.util.List;

public class StudioNodeSyncTaskImpl implements StudioNodeSyncTask {

    private static final Logger logger = LoggerFactory.getLogger(StudioNodeSyncTaskImpl.class);

    protected String site;
    protected List<Cluster> clusterNodes;
    protected ContentRepository contentRepository;

    @Override
    public void execute() {
        boolean siteCheck = checkIfSiteRepoExists();
        if (!siteCheck) {
            createSolrIndex();
            createSiteFromRemote();
            create
        }
    }

    private boolean checkIfSiteRepoExists() {
        return false;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public List<Cluster> getClusterNodes() {
        return clusterNodes;
    }

    public void setClusterNodes(List<Cluster> clusterNodes) {
        this.clusterNodes = clusterNodes;
    }

    public ContentRepository getContentRepository() {
        return contentRepository;
    }

    public void setContentRepository(ContentRepository contentRepository) {
        this.contentRepository = contentRepository;
    }
}
