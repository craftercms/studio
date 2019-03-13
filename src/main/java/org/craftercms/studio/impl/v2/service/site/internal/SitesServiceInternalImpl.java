/*
 * Copyright (C) 2007-2019 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.impl.v2.service.site.internal;

import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.api.v1.repository.RepositoryItem;
import org.craftercms.studio.api.v1.util.StudioConfiguration;
import org.craftercms.studio.api.v2.dal.BlueprintDescriptor;
import org.craftercms.studio.api.v2.service.site.internal.SitesServiceInternal;
import org.yaml.snakeyaml.Yaml;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.craftercms.studio.api.v1.util.StudioConfiguration.BLUE_PRINTS_PATH;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.REPO_BLUEPRINTS_DESCRIPTOR_FILENAME;

public class SitesServiceInternalImpl implements SitesServiceInternal {

    private final static Logger logger = LoggerFactory.getLogger(SitesServiceInternalImpl.class);

    private ContentRepository contentRepository;
    private StudioConfiguration studioConfiguration;

    @Override
    public List<BlueprintDescriptor> getAvailbleBlueprints() {
        RepositoryItem[] blueprintsFolders =
                contentRepository.getContentChildren("", studioConfiguration.getProperty(BLUE_PRINTS_PATH));
        List<BlueprintDescriptor> toRet = new ArrayList<BlueprintDescriptor>();
        for (RepositoryItem folder : blueprintsFolders) {
            if (folder.isFolder) {
                try {
                    Path descriptorPath = Paths.get(studioConfiguration.getProperty(StudioConfiguration.REPO_BASE_PATH),
                            studioConfiguration.getProperty(StudioConfiguration.GLOBAL_REPO_PATH),folder.path,
                            folder.name,
                            studioConfiguration.getProperty(REPO_BLUEPRINTS_DESCRIPTOR_FILENAME)).toAbsolutePath();
                    if (Files.exists(descriptorPath)) {
                        FileReader fr = new FileReader(descriptorPath.toString());
                        Yaml yaml = new Yaml();
                        BlueprintDescriptor bdp = yaml.loadAs(fr, BlueprintDescriptor.class);
                        if (bdp != null) {
                            toRet.add(bdp);
                        }
                    }
                } catch (FileNotFoundException e) {
                    logger.error("Error while getting descriptor for blueprint " + folder.name, e);
                }
            }
        }

        return toRet;
    }

    @Override
    public String getBlueprintLocation(String blueprintId) {
        RepositoryItem[] blueprintsFolders =
                contentRepository.getContentChildren("", studioConfiguration.getProperty(BLUE_PRINTS_PATH));
        String toRet = StringUtils.EMPTY;
        for (RepositoryItem folder : blueprintsFolders) {
            if (folder.isFolder) {
                try {
                    Path descriptorPath = Paths.get(studioConfiguration.getProperty(StudioConfiguration.REPO_BASE_PATH),
                            studioConfiguration.getProperty(StudioConfiguration.GLOBAL_REPO_PATH),folder.path,
                            folder.name,
                            studioConfiguration.getProperty(REPO_BLUEPRINTS_DESCRIPTOR_FILENAME)).toAbsolutePath();
                    if (Files.exists(descriptorPath)) {
                        FileReader fr = new FileReader(descriptorPath.toString());
                        Yaml yaml = new Yaml();
                        BlueprintDescriptor bdp = yaml.loadAs(fr, BlueprintDescriptor.class);
                        if (bdp != null && bdp.getBlueprint().getId().equals(blueprintId)) {
                            toRet = descriptorPath.getParent().toAbsolutePath().toString();
                        }
                    }
                } catch (FileNotFoundException e) {
                    logger.error("Error while getting descriptor for blueprint " + folder.name, e);
                }
            }
        }

        return toRet;
    }

    public ContentRepository getContentRepository() {
        return contentRepository;
    }

    public void setContentRepository(ContentRepository contentRepository) {
        this.contentRepository = contentRepository;
    }

    public StudioConfiguration getStudioConfiguration() {
        return studioConfiguration;
    }

    public void setStudioConfiguration(StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }
}
