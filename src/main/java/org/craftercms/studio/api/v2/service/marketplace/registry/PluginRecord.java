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
package org.craftercms.studio.api.v2.service.marketplace.registry;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.craftercms.commons.plugin.model.Version;

import java.time.Instant;
import java.util.List;

/**
 * Holds the data for an installed plugin
 *
 * @author joseross
 * @since 4.0.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PluginRecord {

    /**
     * The plugin id
     */
    private String id;

    /**
     * The plugin version
     */
    private Version version;

    /**
     * The plugin type
     */
    private String type;

    /**
     * The plugin url
     */
    private String pluginUrl;

    /**
     * The date when the plugin was installed
     */
    private Instant installationDate;

    /**
     * The list of files installed for the plugin
     */
    private List<FileRecord> files;

    private List<ConfigRecord> config;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Version getVersion() {
        return version;
    }

    public void setVersion(Version version) {
        this.version = version;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPluginUrl() {
        return pluginUrl;
    }

    public void setPluginUrl(String pluginUrl) {
        this.pluginUrl = pluginUrl;
    }

    public Instant getInstallationDate() {
        return installationDate;
    }

    public void setInstallationDate(Instant installationDate) {
        this.installationDate = installationDate;
    }

    public List<FileRecord> getFiles() {
        return files;
    }

    public void setFiles(List<FileRecord> files) {
        this.files = files;
    }

    public List<ConfigRecord> getConfig() {
        return config;
    }

    public void setConfig(List<ConfigRecord> config) {
        this.config = config;
    }

}
