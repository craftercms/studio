/*
 * Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.craftercms.studio.impl.v2.deployment;

import org.apache.commons.collections4.CollectionUtils;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v2.deployment.Deployer;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.client.RestClientException;

import java.util.List;

/**
 * Composite {@link Deployer} that calls {@link #createTargets(String)} and {@link #deleteTargets(String)}
 * in multiple {@link Deployer}s.
 *
 * <p>On create, if one of the {@code Deployer} fails, the targets that were created successfully previously are
 * roll-backed (with {@link #deleteTargets(String)}</p>
 */
public class CompositeDeployer implements Deployer {

    private final static Logger logger = LoggerFactory.getLogger(AbstractDeployer.class);

    private List<Deployer> deployers;

    @Required
    public void setDeployers(List<Deployer> deployers) {
        this.deployers = deployers;
    }

    @Override
    public void createTargets(String site) throws RestClientException {
        if (CollectionUtils.isNotEmpty(deployers)) {
            try {
                for (Deployer deployer : deployers) {
                    deployer.createTargets(site);
                }
            } catch (Exception e) {
                // Rollback any created targets if one of them fails
                for (Deployer deployer : deployers) {
                    try {
                        deployer.deleteTargets(site);
                    } catch (Exception ex) {
                        logger.debug("Error while rolling back targets for site {0}: {1}", site, ex);
                    }
                }

                throw e;
            }
        }
    }

    @Override
    public void deleteTargets(String site) throws RestClientException {
        if (CollectionUtils.isNotEmpty(deployers)) {
            for (Deployer deployer : deployers) {
                deployer.deleteTargets(site);
            }
        }
    }

}
