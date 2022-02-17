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

package org.craftercms.studio.impl.v2.upgrade.operations.plugin;

import java.beans.ConstructorProperties;
import java.util.Arrays;

import org.craftercms.commons.plugin.PluginDescriptorReader;
import org.craftercms.commons.plugin.model.Asset;
import org.craftercms.commons.plugin.model.BlueprintDescriptor;
import org.craftercms.commons.plugin.model.Contact;
import org.craftercms.commons.plugin.model.CrafterCmsEditions;
import org.craftercms.commons.plugin.model.Developer;
import org.craftercms.commons.plugin.model.Link;
import org.craftercms.commons.plugin.model.Media;
import org.craftercms.commons.plugin.model.Plugin;
import org.craftercms.commons.plugin.model.PluginDescriptor;
import org.craftercms.commons.plugin.model.Version;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;

import static java.util.Collections.singletonList;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.craftercms.commons.plugin.model.PluginTypes.BLUEPRINT;

/**
 * @author joseross
 */
public class DescriptorV2UpgradeOperation extends AbstractPluginDescriptorUpgradeOperation {

    @ConstructorProperties({"studioConfiguration", "descriptorReader"})
    public DescriptorV2UpgradeOperation(StudioConfiguration studioConfiguration,
                                        PluginDescriptorReader descriptorReader) {
        super(studioConfiguration, descriptorReader);
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void doPluginDescriptorUpdates(final PluginDescriptor descriptor) {
        BlueprintDescriptor.Blueprint blueprint = descriptor.getBlueprint();
        Plugin plugin = new Plugin();

        plugin.setType(BLUEPRINT);
        plugin.setId(blueprint.getId());
        plugin.setName(blueprint.getName());
        plugin.setDescription(blueprint.getDescription());
        plugin.setTags(Arrays.asList(blueprint.getTags().split(",")));
        plugin.setSearchEngine(blueprint.getSearchEngine());

        Version pluginVersion = new Version();
        pluginVersion.setMajor(blueprint.getVersion().getMajor());
        pluginVersion.setMinor(blueprint.getVersion().getMinor());
        pluginVersion.setPatch(blueprint.getVersion().getPatch());
        plugin.setVersion(pluginVersion);

        Link license = new Link();
        license.setName(blueprint.getLicense().getName());
        license.setUrl(blueprint.getLicense().getUrl());
        plugin.setLicense(license);

        Link website = new Link();
        website.setName(blueprint.getWebsite().getName());
        website.setUrl(blueprint.getWebsite().getUrl());
        plugin.setWebsite(website);

        Media media = new Media();
        if (nonNull(blueprint.getMedia()) && isNotEmpty(blueprint.getMedia().getScreenshots())) {
            media.setScreenshots(blueprint.getMedia().getScreenshots().stream()
                .map(oldScreenshot -> {
                    Asset screenshot = new Asset();
                    screenshot.setTitle(oldScreenshot.getScreenshot().getTitle());
                    screenshot.setDescription(oldScreenshot.getScreenshot().getDescription());
                    screenshot.setUrl(oldScreenshot.getScreenshot().getUrl());
                    return screenshot;
                })
                .collect(toList())
            );
        }
        if (nonNull(blueprint.getMedia()) && isNotEmpty(blueprint.getMedia().getVideos())) {
            media.setVideos(blueprint.getMedia().getVideos().stream()
                .map(oldVideo -> {
                    Asset video = new Asset();
                    video.setTitle(oldVideo.getVideo().getTitle());
                    video.setDescription(oldVideo.getVideo().getDescription());
                    video.setUrl(oldVideo.getVideo().getUrl());
                    return video;
                })
                .collect(toList())
            );
        }
        plugin.setMedia(media);

        plugin.setCrafterCmsVersions(blueprint.getCraftercmsVersionsSupported().stream()
                .map(oldVersion -> {
                    String[] values = oldVersion.getVersion().split("\\.");
                    Version version = new Version();
                    version.setMajor(Integer.parseInt(values[0]));
                    version.setMinor(Integer.parseInt(values[1]));
                    version.setPatch(Integer.parseInt(values[2]));
                    return version;
                })
                .collect(toList())
        );

        Developer developer = new Developer();
        if (nonNull(blueprint.getMedia()) && nonNull(blueprint.getMedia().getDeveloper()) &&
            isNotEmpty(blueprint.getMedia().getDeveloper().getPeople())) {
            developer.setPeople(blueprint.getMedia().getDeveloper().getPeople().stream()
                .map(oldDev -> {
                    Contact dev = new Contact();
                    dev.setName(oldDev.getPerson().getName());
                    dev.setEmail(oldDev.getPerson().getEmail());
                    dev.setUrl(oldDev.getPerson().getUrl());
                    return dev;
                })
                .collect(toList())
            );
        }
        if (nonNull(blueprint.getMedia()) && nonNull(blueprint.getMedia().getDeveloper()) &&
            nonNull(blueprint.getMedia().getDeveloper().getCompany())) {
            Contact company = new Contact();
            company.setName(blueprint.getMedia().getDeveloper().getCompany().getName());
            company.setEmail(blueprint.getMedia().getDeveloper().getCompany().getEmail());
            company.setUrl(blueprint.getMedia().getDeveloper().getCompany().getUrl());
            developer.setCompany(company);
        }
        plugin.setDeveloper(developer);

        plugin.setCrafterCmsEditions(singletonList(CrafterCmsEditions.COMMUNITY));

        descriptor.setPlugin(plugin);
        descriptor.setBlueprint(null);
    }

}
